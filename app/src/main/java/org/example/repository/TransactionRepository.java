package org.example.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.model.entity.Transaction;
import org.example.model.projection.SellerTotalProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllBySellerId(Long sellerId);

    @Query("""
                SELECT t.seller.id AS id, t.seller.name AS name, SUM(t.amount) AS totalAmount
                FROM Transaction t
                WHERE t.transactionDate >= :from AND t.transactionDate <= :to
                AND t.seller.deletedAt IS NULL
                GROUP BY t.seller.id, t.seller.name
                ORDER BY SUM(t.amount) DESC
            """)
    List<SellerTotalProjection> findMostProductiveSeller(@Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
                SELECT t.seller.id AS id, t.seller.name AS name, SUM(t.amount) AS totalAmount
                FROM Transaction t
                WHERE t.transactionDate >= :from AND t.transactionDate <= :to
                AND t.seller.deletedAt IS NULL
                GROUP BY t.seller.id, t.seller.name
                HAVING SUM(t.amount) < :amount
            """)
    List<SellerTotalProjection> findSellersWithTotalBelow(@Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to, @Param("amount") BigDecimal amount);

    @Query("""
            SELECT t.transactionDate
            FROM Transaction t
            WHERE t.seller.id = :sellerId
            ORDER BY t.transactionDate asc
            """)
    List<LocalDateTime> findTransactionDatesBySellerId(@Param("sellerId") Long sellerId);
}
