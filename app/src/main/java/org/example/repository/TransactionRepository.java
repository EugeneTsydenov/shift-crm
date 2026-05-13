package org.example.repository;

import java.util.List;

import org.example.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllBySellerId(Long sellerId);
}
