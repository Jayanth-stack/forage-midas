package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class WilburBalanceQueryTests {
    static final Logger logger = LoggerFactory.getLogger(WilburBalanceQueryTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private UserRepository userRepository;

    @Test
    void query_wilbur_balance_after_transactions() throws InterruptedException {
        // Populate users
        userPopulator.populate();
        
        // Load and send all transactions
        String[] transactionLines = fileLoader.loadStrings("/test_data/alskdjfh.fhdjsk");
        logger.info("Processing {} transactions", transactionLines.length);
        
        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }
        
        // Wait for all transactions to be processed
        Thread.sleep(3000);
        
        // Query wilbur's balance (user ID 9)
        UserRecord wilbur = userRepository.findById(9);
        assertNotNull(wilbur, "Wilbur should exist in database");
        
        float balance = wilbur.getBalance();
        long roundedDown = Math.round(Math.floor(balance));
        
        logger.info("========================================================");
        logger.info("WILBUR BALANCE RESULT:");
        logger.info("Wilbur's final balance (float): {}", balance);
        logger.info("Wilbur's final balance (rounded down): {}", roundedDown);
        logger.info("========================================================");
    }
}
