package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.AuthRequest;
import com.healthcarenow.core.dto.AuthResponse;
import com.healthcarenow.core.dto.NotificationEvent;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PatientProfileRepository patientProfileRepository;
  private final SessionRepository sessionRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider; // Injected
  private final RedisTemplate<String, Object> redisTemplate;
  private final NotificationProducer notificationProducer;

  private static final String OTP_FORGOT_PASSWORD = "FORGOT_PASSWORD";
  private static final String OTP_CHANGE_PASSWORD = "CHANGE_PASSWORD";
  private static final Duration OTP_TTL = Duration.ofMinutes(5);

  private String otpKey(String purpose, String email) {
    return "auth:otp:" + purpose + ":" + email.toLowerCase().trim();
  }

  private String generateOtpCode() {
    int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
    return String.valueOf(code);
  }

  private void sendOtpEmail(User user, String otp, String purposeLabel) {
    NotificationEvent event = NotificationEvent.builder()
        .eventType("PASSWORD_OTP")
        .userId(user.getId())
        .priority("HIGH")
        .payload(Map.of(
            "email", user.getEmail(),
            "language", "vi",
            "otp_code", otp,
            "otp_expiry_minutes", String.valueOf(OTP_TTL.toMinutes()),
            "purpose", purposeLabel))
        .build();

    notificationProducer.sendNotification(event);
  }

  public void requestForgotPasswordOtp(String email) {
    if (!StringUtils.hasText(email)) {
      throw new BadRequestException("Email is required");
    }

    userRepository.findByEmail(email.trim())
        .ifPresent(user -> {
          String otp = generateOtpCode();
          redisTemplate.opsForValue().set(otpKey(OTP_FORGOT_PASSWORD, user.getEmail()), otp, OTP_TTL);
          sendOtpEmail(user, otp, "khôi phục mật khẩu");
        });
  }

  public void confirmForgotPassword(String email, String otp, String newPassword) {
    if (!StringUtils.hasText(email) || !StringUtils.hasText(otp) || !StringUtils.hasText(newPassword)) {
      throw new BadRequestException("Email, OTP and new password are required");
    }

    User user = userRepository.findByEmail(email.trim())
        .orElseThrow(() -> new BadRequestException("Invalid OTP or email"));

    String key = otpKey(OTP_FORGOT_PASSWORD, user.getEmail());
    Object storedOtpObj = redisTemplate.opsForValue().get(key);
    String storedOtp = storedOtpObj == null ? null : String.valueOf(storedOtpObj);

    if (!StringUtils.hasText(storedOtp) || !storedOtp.equals(otp.trim())) {
      throw new BadRequestException("Invalid OTP or email");
    }

    user.setPasswordHash(passwordEncoder.encode(newPassword.trim()));
    userRepository.save(user);
    redisTemplate.delete(key);
  }

  public void requestChangePasswordOtp(String email, String currentPassword) {
    if (!StringUtils.hasText(email) || !StringUtils.hasText(currentPassword)) {
      throw new BadRequestException("Email and current password are required");
    }

    User user = userRepository.findByEmail(email.trim())
        .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    String otp = generateOtpCode();
    redisTemplate.opsForValue().set(otpKey(OTP_CHANGE_PASSWORD, user.getEmail()), otp, OTP_TTL);
    sendOtpEmail(user, otp, "đổi mật khẩu");
  }

  public void confirmChangePassword(String email, String currentPassword, String otp, String newPassword) {
    if (!StringUtils.hasText(email) || !StringUtils.hasText(currentPassword)
        || !StringUtils.hasText(otp) || !StringUtils.hasText(newPassword)) {
      throw new BadRequestException("Email, current password, OTP and new password are required");
    }

    User user = userRepository.findByEmail(email.trim())
        .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    String key = otpKey(OTP_CHANGE_PASSWORD, user.getEmail());
    Object storedOtpObj = redisTemplate.opsForValue().get(key);
    String storedOtp = storedOtpObj == null ? null : String.valueOf(storedOtpObj);

    if (!StringUtils.hasText(storedOtp) || !storedOtp.equals(otp.trim())) {
      throw new BadRequestException("Invalid OTP");
    }

    user.setPasswordHash(passwordEncoder.encode(newPassword.trim()));
    userRepository.save(user);
    redisTemplate.delete(key);
  }

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
