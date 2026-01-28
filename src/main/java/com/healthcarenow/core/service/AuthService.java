package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.AuthRequest;
import com.healthcarenow.core.dto.AuthResponse;
import com.healthcarenow.core.exception.BadRequestException;
import com.healthcarenow.core.exception.ResourceNotFoundException;
import com.healthcarenow.core.exception.UnauthorizedException;
import com.healthcarenow.core.model.jpa.Role;
import com.healthcarenow.core.model.jpa.User;
import com.healthcarenow.core.model.mongo.PatientProfile;
import com.healthcarenow.core.model.mongo.Session;
import com.healthcarenow.core.repository.jpa.UserRepository;
import com.healthcarenow.core.repository.mongo.PatientProfileRepository;
import com.healthcarenow.core.repository.mongo.SessionRepository;
import com.healthcarenow.core.utils.IdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PatientProfileRepository patientProfileRepository;
  private final SessionRepository sessionRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public AuthResponse register(AuthRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BadRequestException("Email already in use");
    }

    // 1. Create User (Postgres)
    User user = new User();
    user.setId(IdUtils.generateId()); // Shared ID
    user.setEmail(request.getEmail());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    // user.setFullName(request.getFullName()); // Removed from User
    user.setRole(Role.USER);
    user.setStatus("ACTIVE"); // Set status
    userRepository.save(user);

    // 2. Create Profile (Mongo)
    PatientProfile profile = new PatientProfile();
    profile.setId(user.getId());
    profile.setUserId(user.getId());
    profile.setFullName(request.getFullName()); // Set here directly
    profile.setCreatedAt(java.time.LocalDateTime.now());
    profile.setUpdatedAt(java.time.LocalDateTime.now());
    // Init default settings
    PatientProfile.PrivacySettings settings = new PatientProfile.PrivacySettings();
    settings.setDataSharing(true);
    profile.setPrivacySettings(settings);
    patientProfileRepository.save(profile);

    // 3. Create Session
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

  public void logout(String token) {
    // In a real app, we would hash the token before finding it,
    // if we stored the hash. For now, we assume raw token lookup or hash logic
    // here.
    // Simplified:
    // sessionRepository.findByTokenHash(hash(token)).ifPresent(...)
  }

  private AuthResponse createSession(User user) {
    String token = UUID.randomUUID().toString();

    Session session = new Session();
    session.setUserId(user.getId());
    session.setTokenHash(token); // In prod, hash this!
    session.setCreatedAt(LocalDateTime.now());
    session.setExpiresAt(LocalDateTime.now().plusDays(30));
    session.setRevoked(false);

    sessionRepository.save(session);

    return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole().name());
  }
}
