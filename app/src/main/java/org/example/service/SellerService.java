package org.example.service;

import java.time.LocalDateTime;
import java.util.List;

import org.example.exception.SellerNotFoundException;
import org.example.model.dto.seller.SellerRequest;
import org.example.model.dto.seller.SellerResponse;
import org.example.model.dto.seller.SellerUpdateRequest;
import org.example.model.entity.Seller;
import org.example.model.mapper.SellerMapper;
import org.example.repository.SellerRepository;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class SellerService {
    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    @Transactional(readOnly = true)
    public List<SellerResponse> findAll() {
        return sellerRepository
                .findAll()
                .stream()
                .map(SellerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SellerResponse findById(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new SellerNotFoundException(id));

        return SellerMapper.toResponse(seller);
    }

    @Transactional
    public SellerResponse create(SellerRequest request) {
        Seller seller = SellerMapper.toEntity(request);

        sellerRepository.save(seller);
        return SellerMapper.toResponse(seller);
    }

    @Transactional
    public SellerResponse update(Long id, SellerUpdateRequest request) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new SellerNotFoundException(id));

        if (request.name() != null) {
            seller.setName(request.name());
        }

        if (request.contactInfo() != null) {
            seller.setContactInfo(request.contactInfo());
        }

        sellerRepository.save(seller);
        return SellerMapper.toResponse(seller);
    }

    @Transactional
    public void delete(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new SellerNotFoundException(id));

        seller.setDeletedAt(LocalDateTime.now());
        sellerRepository.save(seller);
    }
}
