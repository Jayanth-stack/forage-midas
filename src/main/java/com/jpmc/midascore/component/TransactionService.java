package com.jpmc.midascore.component;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;

@Service
public class TransactionService {
    static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;
    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Process a transaction: validate and persist it to the database.
     * Validates sender and recipient exist, and sender has sufficient balance.
     * If valid, calls the incentives API, updates balances, and records the transaction.
     * 
     * @param transaction the transaction to process
     * @return true if transaction was successfully processed, false if validation failed
     */
    @Transactional
    public boolean processTransaction(Transaction transaction) {
        logger.info("Processing transaction: {}", transaction);

        // Validate sender exists
        UserRecord senderRecord = userRepository.findById(transaction.getSenderId());
        if (senderRecord == null) {
            logger.warn("Transaction rejected: sender {} not found", transaction.getSenderId());
            return false;
        }

        // Validate recipient exists
        UserRecord recipientRecord = userRepository.findById(transaction.getRecipientId());
        if (recipientRecord == null) {
            logger.warn("Transaction rejected: recipient {} not found", transaction.getRecipientId());
            return false;
        }

        BigDecimal transactionAmount = BigDecimal.valueOf(transaction.getAmount());

        // Validate sender has sufficient balance
        if (senderRecord.getBalance() < transaction.getAmount()) {
            logger.warn("Transaction rejected: insufficient balance. Sender {} has {} but needs {} for transaction",
                    senderRecord.getId(), senderRecord.getBalance(), transaction.getAmount());
            return false;
        }

        // Call incentives API to get incentive amount
        float incentiveAmount = 0f;
        try {
            Incentive incentive = restTemplate.postForObject(INCENTIVE_API_URL, transaction, Incentive.class);
            if (incentive != null) {
                incentiveAmount = incentive.getAmount();
                logger.info("Incentive API returned: {}", incentiveAmount);
            }
        } catch (Exception e) {
            logger.warn("Failed to call incentives API: {}", e.getMessage());
            // Continue processing even if incentive API fails
        }

        // Update balances
        // Sender loses transaction amount
        float newSenderBalance = senderRecord.getBalance() - transaction.getAmount();
        // Recipient gains transaction amount + incentive
        float newRecipientBalance = recipientRecord.getBalance() + transaction.getAmount() + incentiveAmount;

        senderRecord.setBalance(newSenderBalance);
        recipientRecord.setBalance(newRecipientBalance);

        // Persist updated user balances
        userRepository.save(senderRecord);
        userRepository.save(recipientRecord);

        // Create and persist transaction record with incentive
        TransactionRecord transactionRecord = new TransactionRecord(senderRecord, recipientRecord, transactionAmount);
        transactionRecord.setIncentive(BigDecimal.valueOf(incentiveAmount));
        transactionRepository.save(transactionRecord);

        logger.info("Transaction processed successfully. Sender {} balance: {} -> {}, Recipient {} balance: {} -> {}, Incentive: {}",
                senderRecord.getId(), senderRecord.getBalance() + transaction.getAmount(), newSenderBalance,
                recipientRecord.getId(), recipientRecord.getBalance() - transaction.getAmount() - incentiveAmount, newRecipientBalance, incentiveAmount);

        return true;
    }
}
