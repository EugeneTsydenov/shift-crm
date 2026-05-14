package org.example.controller;

import java.util.List;

import org.example.model.dto.seller.SellerRequest;
import org.example.model.dto.seller.SellerResponse;
import org.example.model.dto.seller.SellerUpdateRequest;
import org.example.service.SellerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping
    public ResponseEntity<List<SellerResponse>> findAll() {
        return ResponseEntity.ok(sellerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SellerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(sellerService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SellerResponse> create(@Valid @RequestBody SellerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sellerService.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SellerResponse> update(@PathVariable Long id,
            @RequestBody SellerUpdateRequest request) {
        return ResponseEntity.ok(sellerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sellerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}