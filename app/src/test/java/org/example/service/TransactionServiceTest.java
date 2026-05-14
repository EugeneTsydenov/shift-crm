package org.example.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.exception.SellerNotFoundException;
import org.example.exception.TransactionNotFoundException;
import org.example.model.dto.transaction.TransactionRequest;
import org.example.model.dto.transaction.TransactionResponse;
import org.example.model.entity.Seller;
import org.example.model.entity.Transaction;
import org.example.model.enums.PaymentType;
import org.example.repository.SellerRepository;
import org.example.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void findAll_returnsMappedList() {
        Transaction first = transaction(11L, seller(1L, "Ivan"), new BigDecimal("100.00"), PaymentType.CASH);
        Transaction second = transaction(12L, seller(2L, "Petr"), new BigDecimal("250.50"), PaymentType.CARD);
        when(transactionRepository.findAllActive()).thenReturn(List.of(first, second));

        List<TransactionResponse> responses = transactionService.findAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).Id()).isEqualTo(11L);
        assertThat(responses.get(0).sellerId()).isEqualTo(1L);
        assertThat(responses.get(0).amount()).isEqualByComparingTo("100.00");
        assertThat(responses.get(0).paymentType()).isEqualTo(PaymentType.CASH);
        assertThat(responses.get(1).Id()).isEqualTo(12L);
        assertThat(responses.get(1).sellerId()).isEqualTo(2L);
        assertThat(responses.get(1).amount()).isEqualByComparingTo("250.50");
        assertThat(responses.get(1).paymentType()).isEqualTo(PaymentType.CARD);
    }

    @Test
    void findAll_whenEmpty_returnsEmptyList() {
        when(transactionRepository.findAllActive()).thenReturn(List.of());

        List<TransactionResponse> responses = transactionService.findAll();

        assertThat(responses).isEmpty();
    }

    @Test
    void findAll_callsRepositoryWithCorrectMethod() {
        when(transactionRepository.findAllActive()).thenReturn(List.of());

        transactionService.findAll();

        verify(transactionRepository).findAllActive();
    }

    @Test
    void findAllBySellerId_returnsMappedList() {
        Transaction first = transaction(21L, seller(1L, "Ivan"), new BigDecimal("10.00"), PaymentType.TRANSFER);
        Transaction second = transaction(22L, seller(1L, "Ivan"), new BigDecimal("15.25"), PaymentType.CARD);
        when(sellerRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findAllBySellerId(1L)).thenReturn(List.of(first, second));

        List<TransactionResponse> responses = transactionService.findAllBySellerId(1L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).sellerId()).isEqualTo(1L);
        assertThat(responses.get(0).amount()).isEqualByComparingTo("10.00");
        assertThat(responses.get(0).paymentType()).isEqualTo(PaymentType.TRANSFER);
        assertThat(responses.get(1).sellerId()).isEqualTo(1L);
        assertThat(responses.get(1).amount()).isEqualByComparingTo("15.25");
        assertThat(responses.get(1).paymentType()).isEqualTo(PaymentType.CARD);
    }

    @Test
    void findAllBySellerId_whenSellerNotExists_throwsException() {
        when(sellerRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.findAllBySellerId(1L))
                .isInstanceOf(SellerNotFoundException.class)
                .hasMessageContaining("1");

        verify(transactionRepository, never()).findAllBySellerId(any());
    }

    @Test
    void findById_returnsResponse() {
        Transaction transaction = transaction(31L, seller(3L, "Maria"), new BigDecimal("99.99"), PaymentType.CASH);
        when(transactionRepository.findActiveById(31L)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.findById(31L);

        assertThat(response.Id()).isEqualTo(31L);
        assertThat(response.sellerId()).isEqualTo(3L);
        assertThat(response.amount()).isEqualByComparingTo("99.99");
        assertThat(response.paymentType()).isEqualTo(PaymentType.CASH);
        assertThat(response.transactionDate()).isNotNull();
    }

    @Test
    void findById_whenTransactionNotExists_throwsException() {
        when(transactionRepository.findActiveById(31L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.findById(31L))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("31");
    }

    @Test
    void create_savesAndReturnsResponse() {
        Seller seller = seller(5L, "Olga");
        TransactionRequest request = new TransactionRequest(5L, new BigDecimal("500.00"), PaymentType.CARD);
        when(sellerRepository.findById(5L)).thenReturn(Optional.of(seller));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.create(request);

        assertThat(response.sellerId()).isEqualTo(5L);
        assertThat(response.amount()).isEqualByComparingTo("500.00");
        assertThat(response.paymentType()).isEqualTo(PaymentType.CARD);
        assertThat(response.transactionDate()).isNotNull();
        verify(transactionRepository).save(any());
    }

    @Test
    void create_whenSellerNotExists_throwsException() {
        TransactionRequest request = new TransactionRequest(5L, new BigDecimal("500.00"), PaymentType.CARD);
        when(sellerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(request))
                .isInstanceOf(SellerNotFoundException.class)
                .hasMessageContaining("5");

        verify(transactionRepository, never()).save(any());
    }

    private static Seller seller(Long id, String name) {
        Seller seller = Seller.create(name, name.toLowerCase() + "@mail.ru");
        ReflectionTestUtils.setField(seller, "id", id);
        return seller;
    }

    private static Transaction transaction(Long id, Seller seller, BigDecimal amount, PaymentType paymentType) {
        Transaction transaction = Transaction.create(seller, amount, paymentType);
        ReflectionTestUtils.setField(transaction, "id", id);
        ReflectionTestUtils.setField(transaction, "transactionDate", LocalDateTime.now());
        return transaction;
    }
}
