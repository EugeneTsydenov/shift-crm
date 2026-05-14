package org.example.model.dto.analytics;

import java.time.LocalDateTime;

public record BestPeriodResponse(LocalDateTime from, LocalDateTime to, int transactionCount) {
}