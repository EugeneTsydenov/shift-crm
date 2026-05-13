package org.example.model.mapper;

import org.example.model.dto.seller.SellerRequest;
import org.example.model.dto.seller.SellerResponse;
import org.example.model.entity.Seller;

public class SellerMapper {

    private SellerMapper() {
    }

    public static SellerResponse toResponse(Seller seller) {
        return new SellerResponse(
            seller.getId(),
            seller.getName(),
            seller.getContactInfo(),
            seller.getRegistrationDate());
    }

    public static Seller toEntity(SellerRequest request) {
        return Seller.create(request.name(), request.contactInfo());
    }
}