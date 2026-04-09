package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.admin.DashboardOverviewResponse;
import com.healthcarenow.core.dto.admin.DashboardUserResponse;
import com.healthcarenow.core.model.mongo.PatientProfile;
import com.healthcarenow.core.model.mongo.Session;
import com.healthcarenow.core.model.mongo.User;
import com.healthcarenow.core.repository.mongo.PatientProfileRepository;
import com.healthcarenow.core.repository.mongo.SessionRepository;
import com.healthcarenow.core.repository.mongo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/internal/dashboard")
@RequiredArgsConstructor
public class DashboardInternalController {

  private static final String ACTIVE_STATUS = "ACTIVE";

  private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
  private final SessionRepository sessionRepository;

  @GetMapping("/overview")
  public ResponseEntity<DashboardOverviewResponse> getOverview() {
    List<User> users = userRepository.findAll();
    Map<String, User> userById = users.stream()
        .filter(user -> user.getId() != null)
        .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime newRegistrationCutoff = now.minusDays(30);

    List<Session> activeSessions = sessionRepository.findAll().stream()
        .filter(session -> !session.isRevoked())
        .filter(session -> session.getExpiresAt() == null || session.getExpiresAt().isAfter(now))
        .sorted(Comparator.comparing(Session::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
        .toList();

    List<DashboardOverviewResponse.OnlineUser> onlineUsers = activeSessions.stream()
        .map(session -> {
          User user = userById.get(session.getUserId());
          Session.DeviceInfo deviceInfo = session.getDeviceInfo();
          return DashboardOverviewResponse.OnlineUser.builder()
              .userId(session.getUserId())
              .email(user != null ? user.getEmail() : null)
              .status(user != null ? user.getStatus() : ACTIVE_STATUS)
              .location(deviceInfo != null && deviceInfo.getLocation() != null && !deviceInfo.getLocation().isBlank()
                  ? deviceInfo.getLocation()
                  : "Unknown location")
              .ipAddress(deviceInfo != null ? deviceInfo.getIpAddress() : null)
              .userAgent(deviceInfo != null ? deviceInfo.getUserAgent() : null)
              .connectedAt(session.getCreatedAt())
              .build();
        })
        .toList();

    long totalUsers = users.size();
    long activeUsers = users.stream()
        .filter(user -> ACTIVE_STATUS.equalsIgnoreCase(user.getStatus()))
        .count();
    long newRegistrations = users.stream()
        .filter(user -> user.getCreatedAt() != null && !user.getCreatedAt().isBefore(newRegistrationCutoff))
        .count();

    DashboardOverviewResponse response = DashboardOverviewResponse.builder()
        .stats(DashboardOverviewResponse.Stats.builder()
            .totalUsers(totalUsers)
            .activeUsers(activeUsers)
            .newRegistrations(newRegistrations)
            .onlineUsers(onlineUsers.size())
            .build())
        .onlineUsers(onlineUsers)
        .build();

    return ResponseEntity.ok(response);
  }

    @GetMapping("/users")
    public ResponseEntity<List<DashboardUserResponse>> getUsers() {
        List<User> users = userRepository.findAll();
        Map<String, PatientProfile> profileByUserId = patientProfileRepository.findAll().stream()
                .filter(profile -> profile.getUserId() != null)
                .collect(Collectors.toMap(PatientProfile::getUserId, Function.identity(), (left, right) -> left));

        Map<String, LocalDateTime> lastLoginByUserId = sessionRepository.findAll().stream()
                .filter(session -> session.getUserId() != null)
                .collect(Collectors.toMap(
                        Session::getUserId,
                        Session::getCreatedAt,
                        (first, second) -> second != null && (first == null || second.isAfter(first)) ? second : first));

        List<DashboardUserResponse> response = users.stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(user -> {
                    PatientProfile profile = profileByUserId.get(user.getId());
                    return DashboardUserResponse.builder()
                            .id(user.getId())
                            .fullName(profile != null && profile.getFullName() != null ? profile.getFullName() : user.getEmail())
                            .email(user.getEmail())
                            .role(user.getRole() != null ? user.getRole().name() : null)
                            .status(user.getStatus())
                            .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
                            .heightCm(profile != null ? profile.getHeightCm() : null)
                            .weightKg(profile != null ? profile.getWeightKg() : null)
                            .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                            .lastLogin(lastLoginByUserId.get(user.getId()))
                            .build();
                })
                .toList();

        return ResponseEntity.ok(response);
    }
}