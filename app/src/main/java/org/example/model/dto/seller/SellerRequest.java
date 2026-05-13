package org.example.model.dto.seller;

import jakarta.validation.constraints.NotBlank;

public record SellerRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Contact Info is required")
    String contactInfo
) {}