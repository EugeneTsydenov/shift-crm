package org.example.model.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.model.enums.PaymentType;

public record TransactionResponse (
    Long Id,
    Long sellerId, 
    BigDecimal amount,
    PaymentType paymentType,
    LocalDateTime transactionDate
) {}
