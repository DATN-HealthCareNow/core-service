package com.healthcarenow.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionStatusResponse {

    private String plan; // FREE | PREMIUM

    @JsonProperty("is_premium")
    private boolean isPremium;

    @JsonProperty("subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @JsonProperty("ai_chat_daily_tokens_used")
    private int aiChatDailyTokensUsed;

    @JsonProperty("ai_chat_daily_token_limit")
    private int aiChatDailyTokenLimit;

    @JsonProperty("ai_meals_generated_today")
    private int aiMealsGeneratedToday;

    @JsonProperty("ai_meals_daily_limit")
    private int aiMealsDailyLimit;

    @JsonProperty("ai_predict_used_today")
    private int aiPredictUsedToday;

    @JsonProperty("ai_predict_daily_limit")
    private int aiPredictDailyLimit;

    @JsonProperty("ai_insights_used_today")
    private int aiInsightsUsedToday;

    @JsonProperty("ai_insights_daily_limit")
    private int aiInsightsDailyLimit;

    @JsonProperty("medical_scans_total")
    private int medicalScansTotal;

    @JsonProperty("medical_scans_limit")
    private int medicalScansLimit;
}
