package org.example.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.example.model.entity.Seller;
import org.example.model.entity.Transaction;
import org.example.model.enums.PaymentType;
import org.example.model.projection.SellerTotalProjection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@SuppressWarnings("null")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Test
    void findAllActive_returnsOnlyTransactionsForActiveSellers() {
        Seller activeSeller = sellerRepository.save(Seller.create("Ivan", "ivan@mail.ru"));
        Seller deletedSeller = sellerRepository.save(Seller.create("Petr", "petr@mail.ru"));
        deletedSeller.setDeletedAt(LocalDateTime.of(2026, 5, 10, 12, 0));
        sellerRepository.saveAndFlush(deletedSeller);

        Transaction activeTransaction = transactionRepository.save(transaction(activeSeller,
                new BigDecimal("100.00"), PaymentType.CASH, LocalDateTime.of(2026, 5, 1, 10, 0)));
        transactionRepository.save(transaction(deletedSeller,
                new BigDecimal("250.00"), PaymentType.CARD, LocalDateTime.of(2026, 5, 2, 10, 0)));

        List<Transaction> transactions = transactionRepository.findAllActive();

        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getId()).isEqualTo(activeTransaction.getId());
        assertThat(transactions.get(0).getSeller().getId()).isEqualTo(activeSeller.getId());
    }

    @Test
    void findActiveById_returnsTransactionOnlyForActiveSeller() {
        Seller activeSeller = sellerRepository.save(Seller.create("Ivan", "ivan@mail.ru"));
        Seller deletedSeller = sellerRepository.save(Seller.create("Petr", "petr@mail.ru"));
        deletedSeller.setDeletedAt(LocalDateTime.of(2026, 5, 10, 12, 0));
        sellerRepository.saveAndFlush(deletedSeller);

        Transaction activeTransaction = transactionRepository.save(transaction(activeSeller,
                new BigDecimal("100.00"), PaymentType.CASH, LocalDateTime.of(2026, 5, 1, 10, 0)));
        Transaction deletedTransaction = transactionRepository.save(transaction(deletedSeller,
                new BigDecimal("250.00"), PaymentType.CARD, LocalDateTime.of(2026, 5, 2, 10, 0)));

        assertThat(transactionRepository.findActiveById(activeTransaction.getId())).contains(activeTransaction);
        assertThat(transactionRepository.findActiveById(deletedTransaction.getId())).isEmpty();
    }

    @Test
    void findAllBySellerId_returnsAllTransactionsForSeller() {
        Seller seller = sellerRepository.save(Seller.create("Ivan", "ivan@mail.ru"));
        Transaction first = transactionRepository.save(transaction(seller,
                new BigDecimal("100.00"), PaymentType.CASH, LocalDateTime.of(2026, 5, 1, 10, 0)));
        Transaction second = transactionRepository.save(transaction(seller,
                new BigDecimal("250.00"), PaymentType.CARD, LocalDateTime.of(2026, 5, 2, 10, 0)));

        List<Transaction> transactions = transactionRepository.findAllBySellerId(seller.getId());

        assertThat(transactions).extracting(Transaction::getId).containsExactly(first.getId(), second.getId());
    }

    @Test
    void findMostProductiveSeller_returnsOrderedProjectionForPeriod() {
        Seller firstSeller = sellerRepository.save(Seller.create("Ivan", "ivan@mail.ru"));
        Seller secondSeller = sellerRepository.save(Seller.create("Petr", "petr@mail.ru"));

        transactionRepository.save(transaction(firstSeller,
                new BigDecimal("100.00"), PaymentType.CASH, LocalDateTime.of(2026, 5, 1, 10, 0)));
        transactionRepository.save(transaction(firstSeller,
                new BigDecimal("50.00"), PaymentType.CARD, LocalDateTime.of(2026, 5, 2, 10, 0)));
        transactionRepository.save(transaction(secondSeller,
                new BigDecimal("300.00"), PaymentType.TRANSFER, LocalDateTime.of(2026, 5, 3, 10, 0)));

        List<SellerTotalProjection> projections = transactionRepository.findMostProductiveSeller(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59));

        assertThat(projections).hasSize(2);
        assertThat(projections.get(0).getId()).isEqualTo(secondSeller.getId());
        assertThat(projections.get(0).getName()).isEqualTo("Petr");
        assertThat(projections.get(0).getTotalAmount()).isEqualByComparingTo("300.00");
        assertThat(projections.get(1).getId()).isEqualTo(firstSeller.getId());
        assertThat(projections.get(1).getName()).isEqualTo("Ivan");
        assertThat(projections.get(1).getTotalAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void findMostProductiveSeller_excludesDeletedSellers() {
        Seller activeSeller = sellerRepository.save(Seller.create("Ivan", "ivan@mail.ru"));
        Seller deletedSeller = sellerRepository.save(Seller.create("Petr", "petr@mail.ru"));
        deletedSeller.setDeletedAt(LocalDateTime.of(2026, 5, 10, 12, 0));
        sellerRepository.saveAndFlush(deletedSeller);

        transactionRepository.save(transaction(activeSeller,
                new BigDecimal("100.00"), PaymentType.CASH, LocalDateTime.of(2026, 5, 1, 10, 0)));
        transactionRepository.save(transaction(deletedSeller,
                new BigDecimal("500.00"), PaymentType.CARD, LocalDateTime.of(2026, 5, 2, 10, 0)));

        List<SellerTotalProjection> projections = transactionRepository.findMostProductiveSeller(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59));

        assertThat(projections).hasSize(1);
        assertThat(projections.get(0).getId()).isEqualTo(activeSeller.getId());
        assertThat(projections.get(0).getName()).isEqualTo("Ivan");
        assertThat(projections.get(0).getTotalAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void findSellersWithTotalBelow_returnsFilteredProjection() {
        Seller firstSeller = sellerRepository.save(Seller.create("Ivan", "ivan@mail.ru"));
        Seller secondSeller = sellerRepository.save(Seller.create("Petr", "petr@mail.ru"));

        transactionRepository.save(transaction(firstSeller,
                new BigDecimal("100.00"), PaymentType.CASH, LocalDateTime.of(2026, 5, 1, 10, 0)));
        transactionRepository.save(transaction(firstSeller,
                new BigDecimal("50.00"), PaymentType.CARD, LocalDateTime.of(2026, 5, 2, 10, 0)));
        transactionRepository.save(transaction(secondSeller,
                new BigDecimal("300.00"), PaymentType.TRANSFER, LocalDateTime.of(2026, 5, 3, 10, 0)));

        List<SellerTotalProjection> projections = transactionRepository.findSellersWithTotalBelow(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59),
                new BigDecimal("200.00"));

        assertThat(projections).hasSize(1);
        assertThat(projections.get(0).getId()).isEqualTo(firstSeller.getId());
        assertThat(projections.get(0).getName()).isEqualTo("Ivan");
        assertThat(projections.get(0).getTotalAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void findTransactionDatesBySellerId_returnsDatesSortedAsc() {
        Seller seller = sellerRepository.save(Seller.create("Ivan", "ivan@mail.ru"));

        transactionRepository.save(transaction(seller,
                new BigDecimal("100.00"), PaymentType.CASH, LocalDateTime.of(2026, 5, 3, 10, 0)));
        transactionRepository.save(transaction(seller,
                new BigDecimal("50.00"), PaymentType.CARD, LocalDateTime.of(2026, 5, 1, 10, 0)));
        transactionRepository.save(transaction(seller,
                new BigDecimal("75.00"), PaymentType.TRANSFER, LocalDateTime.of(2026, 5, 2, 10, 0)));

        List<LocalDateTime> dates = transactionRepository.findTransactionDatesBySellerId(seller.getId());

        assertThat(dates).containsExactly(
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 2, 10, 0),
                LocalDateTime.of(2026, 5, 3, 10, 0));
    }

    private static Transaction transaction(Seller seller, BigDecimal amount, PaymentType paymentType,
            LocalDateTime transactionDate) {
        Transaction transaction = Transaction.create(seller, amount, paymentType);
        ReflectionTestUtils.setField(transaction, "transactionDate", transactionDate);
        return transaction;
    }
}