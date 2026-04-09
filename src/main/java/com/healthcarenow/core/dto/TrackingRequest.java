package com.healthcarenow.core.dto;

import lombok.Data;

@Data
public class TrackingRequest {
    private Double lat;
    private Double lng;
    private String status;
}
