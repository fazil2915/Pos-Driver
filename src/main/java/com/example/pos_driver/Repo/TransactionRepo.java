package com.example.pos_driver.Repo;

import com.example.pos_driver.Model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepo  extends JpaRepository<Transaction, UUID> {

    Transaction findTopByOrderByIdDesc();
}
