package org.example.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.example.exception.InvalidPeriodException;
import org.example.exception.NoTransactionsFoundException;
import org.example.model.dto.analytics.BestPeriodResponse;
import org.example.model.dto.analytics.SellerAnalyticsResponse;
import org.example.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    public AnalyticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    private record PeriodRange(LocalDateTime from, LocalDateTime to) {
    }

    @Transactional(readOnly = true)
    public SellerAnalyticsResponse getMostProductiveSeller(String period) {
        PeriodRange range = getPeriodRange(period);
        return transactionRepository
                .findMostProductiveSeller(range.from(), range.to())
                .stream()
                .findFirst()
                .map(p -> new SellerAnalyticsResponse(p.getId(), p.getName(), p.getTotalAmount()))
                .orElseThrow(() -> new NoTransactionsFoundException(period));
    }

    @Transactional(readOnly = true)
    public List<SellerAnalyticsResponse> getSellersWithTransactionsBelowAmount(BigDecimal amount, LocalDateTime from,
            LocalDateTime to) {
        return transactionRepository
                .findSellersWithTotalBelow(from, to, amount)
                .stream()
                .map(p -> new SellerAnalyticsResponse(p.getId(), p.getName(), p.getTotalAmount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public BestPeriodResponse getBestPeriodForSeller(Long sellerId, int windowDays) {
        List<LocalDateTime> dates = transactionRepository
                .findTransactionDatesBySellerId(sellerId);

        if (dates.isEmpty()) {
            throw new NoTransactionsFoundException(sellerId);
        }

        if (dates.size() == 1) {
            return new BestPeriodResponse(dates.get(0), dates.get(0), 1);
        }

        return findBestPeriod(dates, windowDays);
    }

    private BestPeriodResponse findBestPeriod(List<LocalDateTime> dates, int windowDays) {
        int left = 0;
        int bestCount = 0;

        LocalDateTime bestStart = dates.get(0);
        LocalDateTime bestEnd = dates.get(0);

        for (int right = 0; right < dates.size(); right++) {
            while (ChronoUnit.DAYS.between(dates.get(left).toLocalDate(),
                    dates.get(right).toLocalDate()) >= windowDays) {
                left++;
            }

            int currentCount = right - left + 1;
            if (currentCount > bestCount) {
                bestCount = currentCount;

                bestStart = dates.get(left);
                bestEnd = dates.get(right);
            }
        }

        return new BestPeriodResponse(bestStart, bestEnd, bestCount);
    }

    private PeriodRange getPeriodRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period.toLowerCase()) {
            case "day" ->
                new PeriodRange(now.toLocalDate().atStartOfDay(), now);
            case "month" ->
                new PeriodRange(now.withDayOfMonth(1).toLocalDate().atStartOfDay(), now);
            case "quarter" ->
                new PeriodRange(now.withMonth(((now.getMonthValue() - 1) / 3) * 3 + 1)
                        .withDayOfMonth(1)
                        .toLocalDate().atStartOfDay(), now);
            case "year" ->
                new PeriodRange(now.withDayOfYear(1).toLocalDate().atStartOfDay(), now);
            default ->
                throw new InvalidPeriodException(period);
        };
    }
}
