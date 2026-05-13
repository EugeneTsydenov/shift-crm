package org.example.model.mapper;

import org.example.model.dto.transaction.TransactionResponse;
import org.example.model.entity.Transaction;

public class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(
            tx.getId(),
            tx.getSeller().getId(),
            tx.getAmount(),
            tx.getPaymentType(),
            tx.getTransactionDate());
    }
}
