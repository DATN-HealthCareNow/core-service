package com.healthcarenow.core.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminDashboardOverviewResponse {
    private Stats stats;
    private List<OnlineUser> onlineUsers;

    @Data
    @Builder
    public static class Stats {
        private long totalUsers;
        private long activeUsers;
        private long newRegistrations;
        private long onlineUsers;
        private long totalArticles;
    }

    @Data
    @Builder
    public static class OnlineUser {
        private String userId;
        private String email;
        private String status;
        private String location;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime connectedAt;
    }
}
