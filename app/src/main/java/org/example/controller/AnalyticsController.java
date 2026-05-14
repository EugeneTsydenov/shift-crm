package org.example.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.example.model.dto.analytics.BestPeriodResponse;
import org.example.model.dto.analytics.SellerAnalyticsResponse;
import org.example.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/most-productive/{period}")
    public ResponseEntity<SellerAnalyticsResponse> getMostProductiveSeller(@PathVariable String period) {
        return ResponseEntity.ok(analyticsService.getMostProductiveSeller(period));
    }

    @GetMapping("/sellers/below-amount")
    public ResponseEntity<List<SellerAnalyticsResponse>> getSellersWithTransactionsBelowAmount(
            @RequestParam BigDecimal amount,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        return ResponseEntity.ok(
                analyticsService.getSellersWithTransactionsBelowAmount(amount, from, to));
    }

    @GetMapping("/best-period/{sellerId}")
    public ResponseEntity<BestPeriodResponse> getBestPeriodForSeller(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "30") int windowDays) {
        return ResponseEntity.ok(analyticsService.getBestPeriodForSeller(sellerId, windowDays));
    }
}