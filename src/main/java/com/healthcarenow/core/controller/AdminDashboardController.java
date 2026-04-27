package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.admin.AdminDashboardOverviewResponse;
import com.healthcarenow.core.model.mongo.Session;
import com.healthcarenow.core.model.mongo.User;
import com.healthcarenow.core.repository.mongo.ArticleRepository;
import com.healthcarenow.core.repository.mongo.SessionRepository;
import com.healthcarenow.core.repository.mongo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AdminDashboardController {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final ArticleRepository articleRepository;

    @GetMapping("/overview")
    public ResponseEntity<AdminDashboardOverviewResponse> getOverview() {
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

        List<AdminDashboardOverviewResponse.OnlineUser> onlineUsers = activeSessions.stream()
                .map(session -> {
                    User user = userById.get(session.getUserId());
                    Session.DeviceInfo deviceInfo = session.getDeviceInfo();
                    return AdminDashboardOverviewResponse.OnlineUser.builder()
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

        long totalArticles = articleRepository.count();

        AdminDashboardOverviewResponse response = AdminDashboardOverviewResponse.builder()
                .stats(AdminDashboardOverviewResponse.Stats.builder()
                        .totalUsers(totalUsers)
                        .activeUsers(activeUsers)
                        .newRegistrations(newRegistrations)
                        .onlineUsers(onlineUsers.size())
                        .totalArticles(totalArticles)
                        .build())
                .onlineUsers(onlineUsers)
                .build();

        return ResponseEntity.ok(response);
    }
}
