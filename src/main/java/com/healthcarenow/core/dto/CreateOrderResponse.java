package com.healthcarenow.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrderResponse {

    @JsonProperty("order_code")
    private long orderCode;

    @JsonProperty("checkout_url")
    private String checkoutUrl;

    @JsonProperty("qr_code")
    private String qrCode;

    private String status;
}
