package org.example.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.example.exception.InvalidPeriodException;
import org.example.exception.NoTransactionsFoundException;
import org.example.model.dto.analytics.BestPeriodResponse;
import org.example.model.dto.analytics.SellerAnalyticsResponse;
import org.example.model.projection.SellerTotalProjection;
import org.example.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void getMostProductiveSeller_whenPeriodDay_returnsMappedResponse() {
        SellerTotalProjection projection = projection(1L, "Ivan", new BigDecimal("150.00"));
        when(transactionRepository.findMostProductiveSeller(any(), any())).thenReturn(List.of(projection));

        LocalDateTime before = LocalDateTime.now();
        SellerAnalyticsResponse response = analyticsService.getMostProductiveSeller("day");
        LocalDateTime after = LocalDateTime.now();

        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Ivan");
        assertThat(response.totalAmount()).isEqualByComparingTo("150.00");
        assertPeriodRange("day", before, after);
    }

    @Test
    void getMostProductiveSeller_whenPeriodMonth_returnsMappedResponse() {
        SellerTotalProjection projection = projection(2L, "Petr", new BigDecimal("250.00"));
        when(transactionRepository.findMostProductiveSeller(any(), any())).thenReturn(List.of(projection));

        LocalDateTime before = LocalDateTime.now();
        SellerAnalyticsResponse response = analyticsService.getMostProductiveSeller("month");
        LocalDateTime after = LocalDateTime.now();

        assertThat(response.sellerId()).isEqualTo(2L);
        assertThat(response.name()).isEqualTo("Petr");
        assertThat(response.totalAmount()).isEqualByComparingTo("250.00");
        assertPeriodRange("month", before, after);
    }

    @Test
    void getMostProductiveSeller_whenPeriodQuarter_returnsMappedResponse() {
        SellerTotalProjection projection = projection(3L, "Maria", new BigDecimal("300.00"));
        when(transactionRepository.findMostProductiveSeller(any(), any())).thenReturn(List.of(projection));

        LocalDateTime before = LocalDateTime.now();
        SellerAnalyticsResponse response = analyticsService.getMostProductiveSeller("quarter");
        LocalDateTime after = LocalDateTime.now();

        assertThat(response.sellerId()).isEqualTo(3L);
        assertThat(response.name()).isEqualTo("Maria");
        assertThat(response.totalAmount()).isEqualByComparingTo("300.00");
        assertPeriodRange("quarter", before, after);
    }

    @Test
    void getMostProductiveSeller_whenPeriodYear_returnsMappedResponse() {
        SellerTotalProjection projection = projection(4L, "Olga", new BigDecimal("450.00"));
        when(transactionRepository.findMostProductiveSeller(any(), any())).thenReturn(List.of(projection));

        LocalDateTime before = LocalDateTime.now();
        SellerAnalyticsResponse response = analyticsService.getMostProductiveSeller("year");
        LocalDateTime after = LocalDateTime.now();

        assertThat(response.sellerId()).isEqualTo(4L);
        assertThat(response.name()).isEqualTo("Olga");
        assertThat(response.totalAmount()).isEqualByComparingTo("450.00");
        assertPeriodRange("year", before, after);
    }

    @Test
    void getMostProductiveSeller_whenInvalidPeriod_throwsException() {
        assertThatThrownBy(() -> analyticsService.getMostProductiveSeller("week"))
                .isInstanceOf(InvalidPeriodException.class)
                .hasMessageContaining("week");

        verify(transactionRepository, never()).findMostProductiveSeller(any(), any());
    }

    @Test
    void getMostProductiveSeller_whenNoTransactions_throwsException() {
        when(transactionRepository.findMostProductiveSeller(any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> analyticsService.getMostProductiveSeller("day"))
                .isInstanceOf(NoTransactionsFoundException.class)
                .hasMessageContaining("day");
    }

    @Test
    void getSellersWithTransactionsBelowAmount_returnsMappedList() {
        SellerTotalProjection first = projection(1L, "Ivan", new BigDecimal("100.00"));
        SellerTotalProjection second = projection(2L, "Petr", new BigDecimal("200.00"));
        when(transactionRepository.findSellersWithTotalBelow(any(), any(), any())).thenReturn(List.of(first, second));

        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 31, 23, 59);

        List<SellerAnalyticsResponse> responses = analyticsService.getSellersWithTransactionsBelowAmount(
                new BigDecimal("300.00"), from, to);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).sellerId()).isEqualTo(1L);
        assertThat(responses.get(0).name()).isEqualTo("Ivan");
        assertThat(responses.get(0).totalAmount()).isEqualByComparingTo("100.00");
        assertThat(responses.get(1).sellerId()).isEqualTo(2L);
        assertThat(responses.get(1).name()).isEqualTo("Petr");
        assertThat(responses.get(1).totalAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    void getSellersWithTransactionsBelowAmount_whenNoMatches_returnsEmptyList() {
        when(transactionRepository.findSellersWithTotalBelow(any(), any(), any())).thenReturn(List.of());

        List<SellerAnalyticsResponse> responses = analyticsService.getSellersWithTransactionsBelowAmount(
                new BigDecimal("300.00"),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 31, 23, 59));

        assertThat(responses).isEmpty();
    }

    @Test
    void getBestPeriodForSeller_whenNoTransactions_throwsException() {
        when(transactionRepository.findTransactionDatesBySellerId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> analyticsService.getBestPeriodForSeller(1L, 3))
                .isInstanceOf(NoTransactionsFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getBestPeriodForSeller_whenSingleDate_returnsSinglePeriod() {
        LocalDateTime date = LocalDateTime.of(2026, 5, 10, 14, 30);
        when(transactionRepository.findTransactionDatesBySellerId(1L)).thenReturn(List.of(date));

        BestPeriodResponse response = analyticsService.getBestPeriodForSeller(1L, 3);

        assertThat(response.from()).isEqualTo(date);
        assertThat(response.to()).isEqualTo(date);
        assertThat(response.transactionCount()).isEqualTo(1);
    }

    @Test
    void getBestPeriodForSeller_whenMultipleDates_returnsBestPeriod() {
        LocalDateTime first = LocalDateTime.of(2026, 5, 1, 10, 0);
        LocalDateTime second = LocalDateTime.of(2026, 5, 2, 11, 0);
        LocalDateTime third = LocalDateTime.of(2026, 5, 5, 12, 0);
        when(transactionRepository.findTransactionDatesBySellerId(1L)).thenReturn(List.of(first, second, third));

        BestPeriodResponse response = analyticsService.getBestPeriodForSeller(1L, 3);

        assertThat(response.from()).isEqualTo(first);
        assertThat(response.to()).isEqualTo(second);
        assertThat(response.transactionCount()).isEqualTo(2);
    }

    private void assertPeriodRange(String period, LocalDateTime before, LocalDateTime after) {
        verify(transactionRepository).findMostProductiveSeller(
                org.mockito.ArgumentMatchers.argThat(from -> from.equals(expectedPeriodStart(period))),
                org.mockito.ArgumentMatchers.argThat(to -> !to.isBefore(before) && !to.isAfter(after)));
    }

    private static LocalDateTime expectedPeriodStart(String period) {
        LocalDateTime now = LocalDateTime.now();

        return switch (period) {
            case "day" -> now.toLocalDate().atStartOfDay();
            case "month" -> now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            case "quarter" -> now.withMonth(((now.getMonthValue() - 1) / 3) * 3 + 1)
                    .withDayOfMonth(1)
                    .toLocalDate()
                    .atStartOfDay();
            case "year" -> now.withDayOfYear(1).toLocalDate().atStartOfDay();
            default -> throw new IllegalArgumentException(period);
        };
    }

    private static SellerTotalProjection projection(Long id, String name, BigDecimal totalAmount) {
        return new SellerTotalProjection() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public BigDecimal getTotalAmount() {
                return totalAmount;
            }
        };
    }
}
