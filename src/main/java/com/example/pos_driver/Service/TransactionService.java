package com.example.pos_driver.Service;

import com.example.pos_driver.Model.DriverRequest;
import com.example.pos_driver.Model.Terminal;
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
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Service
public class TransactionService {


    @Autowired
    private VitaService vitaService;

    @Autowired
    private TerminalRepo terminalRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    private String STAN= "000001";

    public String generateStan(){
        Pageable pageable = PageRequest.of(0, 1); // Page 0, 1 item per page
        Page<Transaction> transactions = transactionRepo.findLatestTransactionByMsgType( pageable);
        if (transactions.hasContent()) {
            Transaction latestTransaction = transactions.getContent().get(0);
            int stan = Integer.parseInt(latestTransaction.getStan()) + 1;
            STAN = String.format("%06d", stan);
        }
        return STAN;
    }


    public void createTransaction(DriverRequest driverRequest,Iso8583Post IsoMsg) throws XPostilion {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Terminal terminal = vitaService.findTerminalBySerialNumber(driverRequest.getSl_no()).get();

        Transaction transaction= new Transaction();

        transaction.setPan(driverRequest.getPan());
        transaction.setSlno(driverRequest.getSl_no());
        transaction.setAmount(driverRequest.getAmount());
        transaction.setTrack2(driverRequest.getTrack2());
        transaction.setIccData(driverRequest.getIcc_req_data());
        transaction.setPin(driverRequest.getDecodedPin());


        transaction.setSwitchKey(terminal.getSwitchs().getName());

        transaction.setTransactionDateTime(now.format(dateTimeFormatter));
        transaction.setDate(now.format(dateFormatter));
        transaction.setTime(now.format(timeFormatter));
        transaction.setRespCode(IsoMsg.getResponseCode());

        transaction.setMsgType(IsoMsg.getMessageType());

        transaction.setProcessingCode(IsoMsg.getField(Iso8583Post.Bit._003_PROCESSING_CODE));
        transaction.setCardAcceptorNameLocation(IsoMsg.getPrivField(Iso8583Post.PrivBit._017_CARDHOLDER_INFO));
        transaction.setStan(IsoMsg.getField(Iso8583Post.Bit._011_SYSTEMS_TRACE_AUDIT_NR));
        transaction.setCardAcceptorTerminalId(IsoMsg.getField(Iso8583Post.Bit._041_CARD_ACCEPTOR_TERM_ID));
        transaction.setCardAcceptorIdCode(IsoMsg.getField(Iso8583Post.Bit.CARD_ACCEPTOR_ID_CODE));

        transactionRepo.save(transaction);
    }
}
