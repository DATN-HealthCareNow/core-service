package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.AuthRequest;
import com.healthcarenow.core.dto.AuthResponse;
import com.healthcarenow.core.exception.BadRequestException;
import com.healthcarenow.core.exception.UnauthorizedException;
import com.healthcarenow.core.config.JwtTokenProvider;
import com.healthcarenow.core.model.mongo.PatientProfile;
import com.healthcarenow.core.model.mongo.Role;
import com.healthcarenow.core.model.mongo.Session;
import com.healthcarenow.core.model.mongo.User;
import com.healthcarenow.core.repository.mongo.PatientProfileRepository;
import com.healthcarenow.core.repository.mongo.SessionRepository;
import com.healthcarenow.core.repository.mongo.UserRepository;
import com.healthcarenow.core.utils.IdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PatientProfileRepository patientProfileRepository;
  private final SessionRepository sessionRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider; // Injected

  public AuthResponse register(AuthRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BadRequestException("Email already in use");
    }

    // 1. Create User (Mongo)
    User user = new User();
    user.setId(IdUtils.generateId());
    user.setEmail(request.getEmail());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole(Role.USER);
    user.setStatus("ACTIVE");
    userRepository.save(user);

    // 2. Create Profile (Mongo)
    PatientProfile profile = new PatientProfile();
    profile.setId(user.getId());
    profile.setUserId(user.getId());
    profile.setFullName(request.getFullName());
    profile.setCreatedAt(LocalDateTime.now());
    profile.setUpdatedAt(LocalDateTime.now());

    PatientProfile.PrivacySettings settings = new PatientProfile.PrivacySettings();
    settings.setDataSharing(true);
    profile.setPrivacySettings(settings);
    patientProfileRepository.save(profile);

    // 3. Create Session with standard JWT
    return createSession(user);
  }

  public AuthResponse login(AuthRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    return createSession(user);
  }

  public AuthResponse googleLogin(AuthRequest request) {
    try {
      GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
          .build();

      GoogleIdToken idToken = verifier.verify(request.getIdToken());
      if (idToken != null) {
          GoogleIdToken.Payload payload = idToken.getPayload();
          String email = payload.getEmail();
          String name = (String) payload.get("name");
          if (name == null || name.isEmpty()) {
              name = "User";
          }

          User user = userRepository.findByEmail(email).orElse(null);
          if (user == null) {
              user = new User();
              user.setId(IdUtils.generateId());
              user.setEmail(email);
              user.setPasswordHash(passwordEncoder.encode(IdUtils.generateId())); // Random pass
              user.setRole(Role.USER);
              user.setStatus("ACTIVE");
              userRepository.save(user);

              PatientProfile profile = new PatientProfile();
              profile.setId(user.getId());
              profile.setUserId(user.getId());
              profile.setFullName(name);
              profile.setCreatedAt(LocalDateTime.now());
              profile.setUpdatedAt(LocalDateTime.now());
              PatientProfile.PrivacySettings settings = new PatientProfile.PrivacySettings();
              settings.setDataSharing(true);
              profile.setPrivacySettings(settings);
              patientProfileRepository.save(profile);
          }
          return createSession(user);
      } else {
          throw new UnauthorizedException("Invalid Google token");
      }
    } catch (Exception e) {
      throw new UnauthorizedException("Google Auth Failed: " + e.getMessage());
    }
  }

  private AuthResponse createSession(User user) {
    // Generate Token using JwtTokenProvider
    String token = tokenProvider.generateToken(user.getId());

    Session session = new Session();
    session.setUserId(user.getId());
    session.setTokenHash(token);
    session.setCreatedAt(LocalDateTime.now());
    session.setExpiresAt(LocalDateTime.now().plusDays(7));
    session.setRevoked(false);

    sessionRepository.save(session);

    return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole().name());
  }
}
