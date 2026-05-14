package org.example.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.example.exception.SellerNotFoundException;
import org.example.exception.TransactionNotFoundException;
import org.example.model.dto.transaction.TransactionResponse;
import org.example.model.enums.PaymentType;
import org.example.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void findAll_returnsOk() throws Exception {
        when(transactionService.findAll()).thenReturn(List.of(
                new TransactionResponse(1L, 2L, new BigDecimal("100.00"), PaymentType.CASH,
                        LocalDateTime.of(2026, 5, 1, 10, 0))));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sellerId").value(2));

        verify(transactionService).findAll();
    }

    @Test
    void findById_returnsOk() throws Exception {
        when(transactionService.findById(1L)).thenReturn(
                new TransactionResponse(1L, 2L, new BigDecimal("100.00"), PaymentType.CASH,
                        LocalDateTime.of(2026, 5, 1, 10, 0)));

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellerId").value(2));

        verify(transactionService).findById(1L);
    }

    @Test
    void findById_whenMissing_returnsNotFound() throws Exception {
        when(transactionService.findById(1L)).thenThrow(new TransactionNotFoundException(1L));

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Transaction not found with id: 1"));
    }

    @Test
    void findAllBySellerId_returnsOk() throws Exception {
        when(transactionService.findAllBySellerId(2L)).thenReturn(List.of(
                new TransactionResponse(1L, 2L, new BigDecimal("100.00"), PaymentType.CASH,
                        LocalDateTime.of(2026, 5, 1, 10, 0))));

        mockMvc.perform(get("/api/transactions/seller/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sellerId").value(2));

        verify(transactionService).findAllBySellerId(2L);
    }

    @Test
    void findAllBySellerId_whenSellerMissing_returnsNotFound() throws Exception {
        when(transactionService.findAllBySellerId(2L)).thenThrow(new SellerNotFoundException(2L));

        mockMvc.perform(get("/api/transactions/seller/2"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Seller not found with id: 2"));
    }

    @Test
    void create_returnsCreated() throws Exception {
        when(transactionService.create(any())).thenReturn(
                new TransactionResponse(1L, 2L, new BigDecimal("100.00"), PaymentType.CARD,
                        LocalDateTime.of(2026, 5, 1, 10, 0)));

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {"sellerId":2,"amount":100.00,"paymentType":"CARD"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sellerId").value(2));

        verify(transactionService).create(any());
    }

    @Test
    void create_whenBodyInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {"amount":-1,"paymentType":"CARD"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("sellerId: Seller id is required")))
                .andExpect(content().string(containsString("amount: Amount must be positive")));
    }
}