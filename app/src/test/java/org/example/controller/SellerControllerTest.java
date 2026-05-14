package org.example.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.example.exception.SellerNotFoundException;
import org.example.model.dto.seller.SellerResponse;
import org.example.model.dto.seller.SellerUpdateRequest;
import org.example.service.SellerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SellerController.class)
class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SellerService sellerService;

    @Test
    void findAll_returnsOk() throws Exception {
        when(sellerService.findAll()).thenReturn(List.of(
                new SellerResponse(1L, "Ivan", "ivan@mail.ru", LocalDateTime.of(2026, 5, 1, 10, 0))));

        mockMvc.perform(get("/api/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Ivan"))
                .andExpect(jsonPath("$[0].contactInfo").value("ivan@mail.ru"));

        verify(sellerService).findAll();
    }

    @Test
    void findById_returnsOk() throws Exception {
        when(sellerService.findById(1L)).thenReturn(
                new SellerResponse(1L, "Ivan", "ivan@mail.ru", LocalDateTime.of(2026, 5, 1, 10, 0)));

        mockMvc.perform(get("/api/sellers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.contactInfo").value("ivan@mail.ru"));

        verify(sellerService).findById(1L);
    }

    @Test
    void findById_whenSellerMissing_returnsNotFound() throws Exception {
        when(sellerService.findById(1L)).thenThrow(new SellerNotFoundException(1L));

        mockMvc.perform(get("/api/sellers/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Seller not found with id: 1"));
    }

    @Test
    void create_returnsCreated() throws Exception {
        when(sellerService.create(any())).thenReturn(
                new SellerResponse(1L, "Ivan", "ivan@mail.ru", LocalDateTime.of(2026, 5, 1, 10, 0)));

        mockMvc.perform(post("/api/sellers")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {"name":"Ivan","contactInfo":"ivan@mail.ru"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.contactInfo").value("ivan@mail.ru"));

        verify(sellerService).create(any());
    }

    @Test
    void create_whenBodyInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/sellers")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {"name":"","contactInfo":""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("name: Name is required")))
                .andExpect(content().string(containsString("contactInfo: Contact Info is required")));
    }

    @Test
    void update_returnsOk() throws Exception {
        when(sellerService.update(1L, new SellerUpdateRequest("Petr", "petr@mail.ru"))).thenReturn(
                new SellerResponse(1L, "Petr", "petr@mail.ru", LocalDateTime.of(2026, 5, 1, 10, 0)));

        mockMvc.perform(patch("/api/sellers/1")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {"name":"Petr","contactInfo":"petr@mail.ru"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Petr"))
                .andExpect(jsonPath("$.contactInfo").value("petr@mail.ru"));

        verify(sellerService).update(1L, new SellerUpdateRequest("Petr", "petr@mail.ru"));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        doNothing().when(sellerService).delete(1L);

        mockMvc.perform(delete("/api/sellers/1"))
                .andExpect(status().isNoContent());

        verify(sellerService).delete(1L);
    }
}