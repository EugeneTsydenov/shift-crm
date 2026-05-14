package org.example.model.dto.analytics;

import java.math.BigDecimal;

public record SellerAnalyticsResponse(Long id, String name, BigDecimal totalAmount) {
}
