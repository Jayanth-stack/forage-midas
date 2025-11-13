package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {
    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

    private final TransactionReceiver receiver;
    private final TransactionService transactionService;

    public TransactionListener(TransactionReceiver receiver, TransactionService transactionService) {
        this.receiver = receiver;
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "${general.kafka-topic}", containerFactory = "transactionKafkaListenerContainerFactory")
    public void listen(Transaction transaction) {
        logger.info("Received transaction: {}", transaction);
        receiver.add(transaction);
        
        // Process transaction with validation and persistence
        boolean processed = transactionService.processTransaction(transaction);
        if (processed) {
            logger.info("Transaction {} processed and persisted", transaction);
        } else {
            logger.warn("Transaction {} validation failed and was not persisted", transaction);
        }
    }
}
