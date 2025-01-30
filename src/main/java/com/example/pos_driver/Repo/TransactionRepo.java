package com.example.pos_driver.Repo;

import com.example.pos_driver.Model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepo  extends JpaRepository<Transaction, UUID> {

    Transaction findTopByOrderByIdDesc();


    @Query("SELECT t FROM Transaction t WHERE t.msg_type = :msgType ORDER BY t.created_date DESC")
    Page<Transaction> findLatestTransactionByMsgType(@Param("msgType") String msgType, Pageable pageable);

}
