package org.example.model.dto.seller;

import java.time.LocalDateTime;

public record SellerResponse(
    Long id, 
    String name, 
    String contactInfo, 
    LocalDateTime registrationDate
) {}