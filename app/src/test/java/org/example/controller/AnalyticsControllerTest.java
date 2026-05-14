package org.example.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.example.exception.InvalidPeriodException;
import org.example.exception.NoTransactionsFoundException;
import org.example.model.dto.analytics.BestPeriodResponse;
import org.example.model.dto.analytics.SellerAnalyticsResponse;
import org.example.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    void getMostProductiveSeller_returnsOk() throws Exception {
        when(analyticsService.getMostProductiveSeller("month"))
                .thenReturn(new SellerAnalyticsResponse(1L, "Ivan", new BigDecimal("150.00")));

        mockMvc.perform(get("/api/analytics/most-productive/month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellerId").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"));

        verify(analyticsService).getMostProductiveSeller("month");
    }

    @Test
    void getMostProductiveSeller_whenInvalidPeriod_returnsBadRequest() throws Exception {
        when(analyticsService.getMostProductiveSeller("week"))
                .thenThrow(new InvalidPeriodException("week"));

        mockMvc.perform(get("/api/analytics/most-productive/week"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid period: week. Use day/month/quarter/year"));
    }

    @Test
    void getSellersWithTransactionsBelowAmount_returnsOk() throws Exception {
        when(analyticsService.getSellersWithTransactionsBelowAmount(any(), any(), any())).thenReturn(List.of(
                new SellerAnalyticsResponse(1L, "Ivan", new BigDecimal("100.00"))));

        mockMvc.perform(get("/api/analytics/sellers/below-amount")
                .param("amount", "200.00")
                .param("from", "2026-05-01 00:00:00")
                .param("to", "2026-05-31 23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sellerId").value(1));

        verify(analyticsService).getSellersWithTransactionsBelowAmount(
                new BigDecimal("200.00"),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59, 59));
    }

    @Test
    void getSellersWithTransactionsBelowAmount_whenParamMissing_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/analytics/sellers/below-amount")
                .param("from", "2026-05-01 00:00:00")
                .param("to", "2026-05-31 23:59:59"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Required parameter 'amount' is missing"));
    }

    @Test
    void getBestPeriodForSeller_returnsOk() throws Exception {
        when(analyticsService.getBestPeriodForSeller(1L, 30)).thenReturn(
                new BestPeriodResponse(LocalDateTime.of(2026, 5, 1, 10, 0),
                        LocalDateTime.of(2026, 5, 5, 10, 0), 3));

        mockMvc.perform(get("/api/analytics/best-period/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionCount").value(3));

        verify(analyticsService).getBestPeriodForSeller(1L, 30);
    }

    @Test
    void getBestPeriodForSeller_whenNoTransactions_returnsNotFound() throws Exception {
        when(analyticsService.getBestPeriodForSeller(1L, 30))
                .thenThrow(new NoTransactionsFoundException(1L));

        mockMvc.perform(get("/api/analytics/best-period/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No transactions found for seller with id: 1"));
    }
}