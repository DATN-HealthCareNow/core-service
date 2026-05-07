package com.healthcarenow.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateOrderRequest {

    private String plan = "PREMIUM";

    private int amount = 10000; // VND

    private String description = "Healthcare Now Premium";

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("cancel_url")
    private String cancelUrl;
}
