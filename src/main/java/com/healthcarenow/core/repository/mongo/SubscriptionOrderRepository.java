package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.SubscriptionOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionOrderRepository extends MongoRepository<SubscriptionOrder, String> {

    Optional<SubscriptionOrder> findByOrderCode(long orderCode);

    List<SubscriptionOrder> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<SubscriptionOrder> findFirstByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
}
