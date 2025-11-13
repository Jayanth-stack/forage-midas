package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class TransactionReceiver {
    private final List<Transaction> received = new CopyOnWriteArrayList<>();

    public void add(Transaction t) {
        received.add(t);
    }

    public List<Transaction> getAll() {
        return received;
    }

    public void clear() {
        received.clear();
    }
}
