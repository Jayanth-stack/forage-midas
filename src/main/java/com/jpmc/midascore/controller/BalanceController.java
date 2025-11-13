package com.jpmc.midascore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.foundation.Balance;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;
import com.jpmc.midascore.entity.UserRecord;

@RestController
public class BalanceController {
    
    private final UserRepository userRepository;

    public BalanceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam(value = "userId") Long userId) {
        Optional<UserRecord> userRecord = userRepository.findById(userId);
        
        if (userRecord.isPresent()) {
            float balanceAmount = userRecord.get().getBalance();
            return new Balance(balanceAmount);
        } else {
            return new Balance(0.0f);
        }
    }
}
