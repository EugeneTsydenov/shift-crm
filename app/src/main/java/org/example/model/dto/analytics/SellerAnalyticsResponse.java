package org.example.model.dto.analytics;

import java.math.BigDecimal;

public record SellerAnalyticsResponse(Long sellerId, String name, BigDecimal totalAmount) {
}
