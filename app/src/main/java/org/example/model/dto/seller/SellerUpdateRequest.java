package org.example.model.dto.seller;

public record SellerUpdateRequest(
    String name,
    String contactInfo
) {}