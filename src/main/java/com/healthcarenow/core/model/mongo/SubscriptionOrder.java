package com.healthcarenow.core.model.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "subscription_orders")
@Data
@NoArgsConstructor
public class SubscriptionOrder {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed(unique = true)
    private long orderCode; // PayOS order code (numeric)

    private String plan; // PREMIUM

    private int amount; // 10000 (VND)

    private String status; // PENDING, PAID, CANCELLED, EXPIRED

    private String checkoutUrl;

    private String qrCode;

    private String paymentMethod; // From PayOS callback

    private String transactionId; // From PayOS callback

    private LocalDateTime paidAt;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime expiredAt; // Order expiration (15 min from creation)
}
