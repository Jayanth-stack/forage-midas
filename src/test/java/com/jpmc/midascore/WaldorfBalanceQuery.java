package com.jpmc.midascore;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import com.jpmc.midascore.repository.UserRepository;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class WaldorfBalanceQueryTests {
    static final Logger logger = LoggerFactory.getLogger(WaldorfBalanceQueryTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private UserRepository userRepository;

    @Test
    @SuppressWarnings("java:S2925")
    void query_waldorf_balance() throws InterruptedException {
        userPopulator.populate();
        String[] transactionLines = fileLoader.loadStrings("/test_data/mnbvcxz.vbnm");
        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }
        Thread.sleep(2000);

        // Query waldorf's balance (user ID 5)
        var waldorf = userRepository.findById(5);
        assertNotNull(waldorf);
        
        float balance = waldorf.getBalance();
        int roundedBalance = (int) balance;
        logger.info("========================================");
        logger.info("Waldorf's final balance: {}", balance);
        logger.info("Waldorf's balance (rounded down): {}", roundedBalance);
        logger.info("========================================");
    }
}
