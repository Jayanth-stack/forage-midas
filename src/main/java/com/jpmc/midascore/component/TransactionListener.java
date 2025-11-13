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

    public TransactionListener(TransactionReceiver receiver) {
        this.receiver = receiver;
    }

    @KafkaListener(topics = "${general.kafka-topic}", containerFactory = "transactionKafkaListenerContainerFactory")
    public void listen(Transaction transaction) {
        logger.info("Received transaction: {}", transaction);
        receiver.add(transaction);
    }
}
