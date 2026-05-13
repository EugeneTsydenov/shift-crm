package org.example.service;

import java.util.List;

import org.example.exception.SellerNotFoundException;
import org.example.exception.TransactionNotFoundException;
import org.example.model.dto.transaction.TransactionRequest;
import org.example.model.dto.transaction.TransactionResponse;
import org.example.model.entity.Seller;
import org.example.model.entity.Transaction;
import org.example.model.mapper.TransactionMapper;
import org.example.repository.SellerRepository;
import org.example.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final SellerRepository sellerRepository;

    public TransactionService(TransactionRepository transactionRepository, SellerRepository sellerRepository) {
        this.transactionRepository = transactionRepository;
        this.sellerRepository = sellerRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll() {
        return transactionRepository
                .findAll()
                .stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAllBySellerId(Long sellerId) {
        if (!sellerRepository.existsById(sellerId)) {
            throw new SellerNotFoundException(sellerId);
        }

        return transactionRepository
            .findAllBySellerId(sellerId)
            .stream()
            .map(TransactionMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        Transaction transaction = transactionRepository
            .findById(id)
            .orElseThrow(() -> new TransactionNotFoundException(id));

        return TransactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Seller seller = sellerRepository
            .findById(request.sellerId())
            .orElseThrow(() -> new SellerNotFoundException(request.sellerId()));

        Transaction transaction = Transaction.create(seller, request.amount(), request.paymentType());

        Transaction saved = transactionRepository.save(transaction);
        return TransactionMapper.toResponse(saved);
    }
}
