package org.example.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.model.enums.PaymentType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller seller;

    private BigDecimal amount;
    private PaymentType paymentType;
    private LocalDateTime transactionDate;

    protected Transaction() {
    }

    public static Transaction create(Seller seller, BigDecimal amount, PaymentType paymentType) {
        Transaction transaction = new Transaction();
        transaction.seller = seller;
        transaction.amount = amount;
        transaction.paymentType = paymentType;
        transaction.transactionDate = LocalDateTime.now();
        return transaction;
    }

    public Long getId() {
        return id;
    }

    public Seller getSeller() {
        return seller;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
}
