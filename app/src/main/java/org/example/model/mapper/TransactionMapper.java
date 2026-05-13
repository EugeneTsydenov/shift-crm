package org.example.model.mapper;

import org.example.model.dto.transaction.TransactionResponse;
import org.example.model.entity.Transaction;

public class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getSeller().getId(),
            transaction.getAmount(),
            transaction.getPaymentType(),
            transaction.getTransactionDate());
    }
}
