package com.jpmc.midascore.repository;

import java.util.List;
import com.jpmc.midascore.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {
    List <TransactionRecord> findBySenderId(Long senderId);

    List <TransactionRecord> findByRecipientId(Long recipientId);
    
}
