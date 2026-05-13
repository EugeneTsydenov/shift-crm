package org.example.model.dto.transaction;

import java.math.BigDecimal;

import org.example.model.enums.PaymentType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionRequest(
    @NotNull(message = "Seller id is required")
    Long sellerId,

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive") 
    BigDecimal amount,

    @NotNull(message = "Payment type is required")
    PaymentType paymentType
) {}