package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.*;
import com.healthcarenow.core.exception.ResourceNotFoundException;
import com.healthcarenow.core.model.mongo.SubscriptionOrder;
import com.healthcarenow.core.model.mongo.User;
import com.healthcarenow.core.repository.mongo.SubscriptionOrderRepository;
import com.healthcarenow.core.repository.mongo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final UserRepository userRepository;
    private final SubscriptionOrderRepository orderRepository;
    private final StringRedisTemplate redisTemplate;
    private final NotificationProducer notificationProducer;

    @Value("${payos.client-id:}")
    private String payosClientId;

    @Value("${payos.api-key:}")
    private String payosApiKey;

    @Value("${payos.checksum-key:}")
    private String payosChecksumKey;

    private static final String PAYOS_API_URL = "https://api-merchant.payos.vn/v2/payment-requests";

    // ── FREE tier limits ──
    private static final int FREE_AI_CHAT_DAILY_TOKENS = 10;
    private static final int FREE_AI_MEALS_DAILY = 1;
    private static final int FREE_AI_PREDICT_DAILY = 1;   // Pulse Scan (user-initiated)
    private static final int FREE_AI_INSIGHTS_DAILY = 1;  // 7-day Health Insights
    private static final int FREE_MEDICAL_SCANS_TOTAL = 1;

    // ── PREMIUM tier limits ──
    private static final int PREMIUM_AI_CHAT_DAILY_TOKENS = 999999; // effectively unlimited
    private static final int PREMIUM_AI_MEALS_DAILY = 999;
    private static final int PREMIUM_AI_PREDICT_DAILY = 999;   // Pulse Scan
    private static final int PREMIUM_AI_INSIGHTS_DAILY = 999;  // 7-day Health Insights
    private static final int PREMIUM_MEDICAL_SCANS_TOTAL = 999;

    // ═══════════════════════════════════════════════════════════════════════
    // GET SUBSCRIPTION STATUS
    // ═══════════════════════════════════════════════════════════════════════

    public SubscriptionStatusResponse getStatus(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isPremium = "PREMIUM".equalsIgnoreCase(user.getSubscriptionPlan());

        int chatTokensUsed = getRedisCounter(getDailyKey("ai_chat_tokens", userId));
        int mealsUsed = getRedisCounter(getDailyKey("ai_meals", userId));
        int predictUsed = getRedisCounter(getDailyKey("ai_predict", userId));
        int insightsUsed = getRedisCounter(getDailyKey("ai_insights", userId));
        int scansTotal = getRedisCounter("medical_scans_total:" + userId);

        return SubscriptionStatusResponse.builder()
                .plan(isPremium ? "PREMIUM" : "FREE")
                .isPremium(isPremium)
                .subscriptionStartDate(user.getSubscriptionStartDate())
                .aiChatDailyTokensUsed(chatTokensUsed)
                .aiChatDailyTokenLimit(isPremium ? PREMIUM_AI_CHAT_DAILY_TOKENS : FREE_AI_CHAT_DAILY_TOKENS)
                .aiMealsGeneratedToday(mealsUsed)
                .aiMealsDailyLimit(isPremium ? PREMIUM_AI_MEALS_DAILY : FREE_AI_MEALS_DAILY)
                .aiPredictUsedToday(predictUsed)
                .aiPredictDailyLimit(isPremium ? PREMIUM_AI_PREDICT_DAILY : FREE_AI_PREDICT_DAILY)
                .aiInsightsUsedToday(insightsUsed)
                .aiInsightsDailyLimit(isPremium ? PREMIUM_AI_INSIGHTS_DAILY : FREE_AI_INSIGHTS_DAILY)
                .medicalScansTotal(scansTotal)
                .medicalScansLimit(isPremium ? PREMIUM_MEDICAL_SCANS_TOTAL : FREE_MEDICAL_SCANS_TOTAL)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CHECK FEATURE QUOTA (called by other services before AI operations)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Check if user can use a specific AI feature.
     * Returns true if allowed, false if quota exceeded.
     */
    public boolean checkAndIncrementQuota(String userId, String featureType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isPremium = "PREMIUM".equalsIgnoreCase(user.getSubscriptionPlan());

        switch (featureType) {
            case "AI_CHAT_TOKEN" -> {
                String key = getDailyKey("ai_chat_tokens", userId);
                int used = getRedisCounter(key);
                int limit = isPremium ? PREMIUM_AI_CHAT_DAILY_TOKENS : FREE_AI_CHAT_DAILY_TOKENS;
                if (used >= limit) return false;
                incrementDailyCounter(key, 1);
                return true;
            }
            case "AI_MEALS" -> {
                String key = getDailyKey("ai_meals", userId);
                int used = getRedisCounter(key);
                int limit = isPremium ? PREMIUM_AI_MEALS_DAILY : FREE_AI_MEALS_DAILY;
                if (used >= limit) return false;
                incrementDailyCounter(key, 1);
                return true;
            }
            case "AI_INSIGHTS" -> {
                String key = getDailyKey("ai_insights", userId);
                int used = getRedisCounter(key);
                int limit = isPremium ? PREMIUM_AI_INSIGHTS_DAILY : FREE_AI_INSIGHTS_DAILY;
                if (used >= limit) return false;
                incrementDailyCounter(key, 1);
                return true;
            }
            case "AI_PREDICT" -> {
                String key = getDailyKey("ai_predict", userId);
                int used = getRedisCounter(key);
                int limit = isPremium ? PREMIUM_AI_PREDICT_DAILY : FREE_AI_PREDICT_DAILY;
                if (used >= limit) return false;
                incrementDailyCounter(key, 1);
                return true;
            }
            case "MEDICAL_SCAN" -> {
                int used = getRedisCounter("medical_scans_total:" + userId);
                int limit = isPremium ? PREMIUM_MEDICAL_SCANS_TOTAL : FREE_MEDICAL_SCANS_TOTAL;
                if (used >= limit) return false;
                // Total counter: no daily expiry
                incrementCounter("medical_scans_total:" + userId, 1);
                return true;
            }
            default -> {
                log.warn("Unknown feature type: {}", featureType);
                return true;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CREATE PAYMENT ORDER
    // ═══════════════════════════════════════════════════════════════════════

    public CreateOrderResponse createOrder(String userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if ("PREMIUM".equalsIgnoreCase(user.getSubscriptionPlan())) {
            throw new IllegalStateException("User is already a Premium member");
        }

        long orderCode = System.currentTimeMillis() / 1000; // unique enough for demo

        // Build PayOS request body
        Map<String, Object> payosBody = new TreeMap<>();
        payosBody.put("orderCode", orderCode);
        payosBody.put("amount", request.getAmount());
        payosBody.put("description", "HCN Premium");
        payosBody.put("cancelUrl", request.getCancelUrl() != null ? request.getCancelUrl() : "https://healthcarenow.app/cancel");
        payosBody.put("returnUrl", request.getReturnUrl() != null ? request.getReturnUrl() : "https://healthcarenow.app/success");

        // Generate checksum: HMAC_SHA256(checksumKey, amount=X&cancelUrl=X&description=X&orderCode=X&returnUrl=X)
        String checksumData = "amount=" + request.getAmount()
                + "&cancelUrl=" + payosBody.get("cancelUrl")
                + "&description=" + payosBody.get("description")
                + "&orderCode=" + orderCode
                + "&returnUrl=" + payosBody.get("returnUrl");

        String signature = hmacSHA256(payosChecksumKey, checksumData);
        payosBody.put("signature", signature);

        log.info("[Subscription] Creating PayOS order: code={}, amount={}, user={}", orderCode, request.getAmount(), userId);

        // Call PayOS API
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", payosClientId);
        headers.set("x-api-key", payosApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payosBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(PAYOS_API_URL, HttpMethod.POST, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !"00".equals(String.valueOf(body.get("code")))) {
                String errMsg = body != null ? String.valueOf(body.get("desc")) : "Unknown PayOS error";
                log.error("[Subscription] PayOS error: {}", errMsg);
                throw new RuntimeException("PayOS error: " + errMsg);
            }

            Map<String, Object> data = (Map<String, Object>) body.get("data");

            String checkoutUrl = String.valueOf(data.get("checkoutUrl"));
            String qrCode = String.valueOf(data.get("qrCode"));

            // Save order to DB
            SubscriptionOrder order = new SubscriptionOrder();
            order.setUserId(userId);
            order.setOrderCode(orderCode);
            order.setPlan(request.getPlan());
            order.setAmount(request.getAmount());
            order.setStatus("PENDING");
            order.setCheckoutUrl(checkoutUrl);
            order.setQrCode(qrCode);
            order.setCreatedAt(LocalDateTime.now());
            order.setExpiredAt(LocalDateTime.now().plusMinutes(15));
            orderRepository.save(order);

            log.info("[Subscription] Order created: code={}, checkoutUrl={}", orderCode, checkoutUrl);

            return CreateOrderResponse.builder()
                    .orderCode(orderCode)
                    .checkoutUrl(checkoutUrl)
                    .qrCode(qrCode)
                    .status("PENDING")
                    .build();

        } catch (Exception e) {
            log.error("[Subscription] Failed to create PayOS order", e);
            throw new RuntimeException("Failed to create payment: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HANDLE PAYOS WEBHOOK
    // ═══════════════════════════════════════════════════════════════════════

    public void handleWebhook(Map<String, Object> webhookData) {
        log.info("[Subscription] Received webhook: {}", webhookData);

        // PayOS webhook structure: { code, desc, data: { orderCode, amount, ... }, signature }
        String code = String.valueOf(webhookData.get("code"));
        if (!"00".equals(code)) {
            log.warn("[Subscription] Webhook with non-success code: {}", code);
            return;
        }

        Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
        if (data == null) {
            log.warn("[Subscription] Webhook data is null");
            return;
        }

        long orderCode = Long.parseLong(String.valueOf(data.get("orderCode")));

        // Verify signature
        String receivedSignature = String.valueOf(webhookData.get("signature"));
        // For demo: skip strict signature verification but log it
        log.info("[Subscription] Webhook signature: {}", receivedSignature);

        SubscriptionOrder order = orderRepository.findByOrderCode(orderCode)
                .orElse(null);

        if (order == null) {
            log.warn("[Subscription] Order not found: {}", orderCode);
            return;
        }

        if ("PAID".equals(order.getStatus())) {
            log.info("[Subscription] Order already processed: {}", orderCode);
            return;
        }

        // Update order
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        if (data.get("paymentMethod") != null) {
            order.setPaymentMethod(String.valueOf(data.get("paymentMethod")));
        }
        if (data.get("transactionId") != null) {
            order.setTransactionId(String.valueOf(data.get("transactionId")));
        }
        orderRepository.save(order);

        // Upgrade user
        upgradeUser(order.getUserId(), order.getPlan());

        log.info("[Subscription] ✅ User {} upgraded to {} via order {}", order.getUserId(), order.getPlan(), orderCode);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VERIFY ORDER STATUS (polling from mobile)
    // ═══════════════════════════════════════════════════════════════════════

    public CreateOrderResponse verifyOrder(String userId, long orderCode) {
        SubscriptionOrder order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("Order does not belong to this user");
        }

        return CreateOrderResponse.builder()
                .orderCode(order.getOrderCode())
                .checkoutUrl(order.getCheckoutUrl())
                .qrCode(order.getQrCode())
                .status(order.getStatus())
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MANUAL UPGRADE (for testing via Postman without PayOS)
    // ═══════════════════════════════════════════════════════════════════════

    public void manualUpgrade(String userId) {
        upgradeUser(userId, "PREMIUM");
        log.info("[Subscription] Manual upgrade for user: {}", userId);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private void upgradeUser(String userId, String plan) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setSubscriptionPlan(plan);
        user.setSubscriptionStartDate(LocalDateTime.now());
        userRepository.save(user);

        // Send realtime notification via RabbitMQ → Notification Service → WebSocket
        try {
            NotificationEvent event = new NotificationEvent();
            event.setUserId(userId);
            event.setEventId("SUBSCRIPTION_UPGRADED");
            event.setEventType("SUBSCRIPTION_UPGRADED");
            event.setTitle("🎉 Nâng cấp thành công!");
            event.setContent("Bạn đã là thành viên Premium. Tận hưởng tất cả tính năng AI không giới hạn!");
            notificationProducer.sendNotification(event);
        } catch (Exception e) {
            log.warn("[Subscription] Failed to send upgrade notification", e);
        }
    }

    private String getDailyKey(String feature, String userId) {
        return feature + ":" + userId + ":" + LocalDate.now();
    }

    private int getRedisCounter(String key) {
        String val = redisTemplate.opsForValue().get("sub:" + key);
        return val != null ? Integer.parseInt(val) : 0;
    }

    private void incrementDailyCounter(String key, int amount) {
        String redisKey = "sub:" + key;
        redisTemplate.opsForValue().increment(redisKey, amount);
        // Set TTL to 48 hours for date-suffixed keys (long enough to survive the day)
        redisTemplate.expire(redisKey, 48, TimeUnit.HOURS);
    }

    private void incrementCounter(String key, int amount) {
        String redisKey = "sub:" + key;
        redisTemplate.opsForValue().increment(redisKey, amount);
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }
}
