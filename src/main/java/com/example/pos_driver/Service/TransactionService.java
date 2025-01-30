package com.example.pos_driver.Service;

import com.example.pos_driver.Model.DriverRequest;
import com.example.pos_driver.Model.Transaction;
import com.example.pos_driver.Repo.TerminalRepo;
import com.example.pos_driver.Repo.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.util.XPostilion;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransactionService {


    @Autowired
    private TerminalRepo terminalRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    private String STAN= "000001";

    public String generateStan(DriverRequest driverRequest, Iso8583Post IsoMsg) throws XPostilion {
        Pageable pageable = PageRequest.of(0, 1); // Page 0, 1 item per page
        Page<Transaction> transactions = transactionRepo.findLatestTransactionByMsgType("0200", pageable);
        if (transactions.hasContent()) {
            Transaction latestTransaction = transactions.getContent().get(0);
            int stan = Integer.parseInt(latestTransaction.getStan()) + 1;
            STAN = String.format("%06d", stan); // Format to 6 digits
        }
        createTransaction(driverRequest, STAN,IsoMsg);
        return STAN;
    }


    public void createTransaction(DriverRequest driverRequest , String stan,Iso8583Post IsoMsg) throws XPostilion {
        Transaction transaction= new Transaction();
        transaction.setMsg_type(IsoMsg.getMessageType());
        transaction.setStan(stan);
        transaction.setCreated_date(LocalDateTime.now());
        transactionRepo.save(transaction);
    }






}
