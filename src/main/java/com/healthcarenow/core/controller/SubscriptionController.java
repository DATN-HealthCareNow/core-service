package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.CreateOrderRequest;
import com.healthcarenow.core.dto.CreateOrderResponse;
import com.healthcarenow.core.dto.SubscriptionStatusResponse;
import com.healthcarenow.core.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * GET /api/v1/subscription/status
     * Returns current plan + daily usage quotas.
     */
    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusResponse> getStatus(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(subscriptionService.getStatus(userId));
    }

    /**
     * POST /api/v1/subscription/create-order
     * Creates a PayOS payment link for Premium upgrade.
     */
    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @AuthenticationPrincipal String userId,
            @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(subscriptionService.createOrder(userId, request));
    }

    /**
     * GET /api/v1/subscription/verify/{orderCode}
     * Poll order status (for when user closes WebView before webhook arrives).
     */
    @GetMapping("/verify/{orderCode}")
    public ResponseEntity<CreateOrderResponse> verifyOrder(
            @AuthenticationPrincipal String userId,
            @PathVariable long orderCode) {
        return ResponseEntity.ok(subscriptionService.verifyOrder(userId, orderCode));
    }

    /**
     * POST /api/v1/subscription/webhook/payos
     * PayOS webhook callback — NO authentication required.
     * Secured by HMAC signature verification in the service layer.
     */
    @PostMapping("/webhook/payos")
    public ResponseEntity<Map<String, String>> handlePayosWebhook(@RequestBody Map<String, Object> webhookData) {
        try {
            subscriptionService.handleWebhook(webhookData);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("[Subscription] Webhook processing error", e);
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/subscription/manual-upgrade
     * FOR TESTING ONLY — upgrades user to Premium without payment.
     * Use this in Postman to test the upgrade flow.
     */
    @PostMapping("/manual-upgrade")
    public ResponseEntity<Map<String, String>> manualUpgrade(@AuthenticationPrincipal String userId) {
        subscriptionService.manualUpgrade(userId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "User upgraded to PREMIUM",
                "user_id", userId
        ));
    }

    /**
     * POST /api/v1/subscription/check-quota
     * Check if user can use a specific AI feature.
     * featureType: AI_CHAT_TOKEN, AI_MEALS, AI_PREDICT, MEDICAL_SCAN
     */
    @PostMapping("/check-quota")
    public ResponseEntity<Map<String, Object>> checkQuota(
            @AuthenticationPrincipal String userId,
            @RequestParam String featureType) {
        boolean allowed = subscriptionService.checkAndIncrementQuota(userId, featureType);
        return ResponseEntity.ok(Map.of(
                "allowed", allowed,
                "feature", featureType,
                "user_id", userId
        ));
    }
}
