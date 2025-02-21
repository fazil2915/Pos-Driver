package com.example.pos_driver.Controller;

import com.example.pos_driver.Model.DriverRequest;
import com.example.pos_driver.Service.*;
import com.example.pos_driver.dto.PosTransRes;
import com.example.pos_driver.Repo.PinDecryption;

import com.example.pos_driver.Service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import postilion.realtime.sdk.util.XPostilion;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController

@Tag(name = "Driver API", description = "Operations related to pos driver")
@RequestMapping("/api")
public class DriverController {
    @Autowired
    private ConfigurableEnvironment environment;

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

    @Autowired
    private HsmService hsmService;

    @Autowired
    private Iso8583Service iso8583Service;

    @Autowired
    private VitaService vitaService;

    @Autowired
    private CardService cardService;

    @Autowired
    private SwitchService switchService;

    private final PinDecryption pinDecryption;

    @Autowired
    public DriverController(PinDecryption pinDecryption) {
        this.pinDecryption = pinDecryption;
    }

    @Autowired
    public  NotificationService notificationService;



    @Operation(summary = "Process a transaction request.", description = "Receive transaction request from pos machine then validate and process the request and give response.")
    @PostMapping("/posDriver")
    public ResponseEntity<?> checkTerminal(@RequestBody DriverRequest driver) throws IOException, XPostilion {
        logger.info("====================================================");
        logger.info("Transaction is Started");
        logger.info("====================================================");
        logger.info("Received request to check terminal for serial number: {}", driver.getSl_no());
        logger.info("Received a request for {}",iso8583Service.getResponseMessage(driver.getIreq_transaction_type()));
        if (driver.getSl_no() == null || driver.getSl_no().trim().isEmpty()) {
            logger.warn("Serial number is missing in the request.");
            logger.info("====================================================");
            logger.info("End of Transaction");
            logger.info("=====================================================");
            return ResponseEntity.ok(new PosTransRes("Serial number is required.", "false", "false"));
        }

        if (driver.getPin() == null || driver.getPin().trim().isEmpty()) {
            logger.warn("Pin number is missing in the request.");
            logger.info("====================================================");
            logger.info("End of Transaction");
            logger.info("====================================================");
            return ResponseEntity.ok(new PosTransRes("Pin number is required.", "false", "false"));
        }

        String isTerminalValid = cardService.verifyTransaction(driver.getSl_no());
        String isPinValid = pinDecryption.pinDecrypting(driver.getPin());
        driver.setDecodedPin(isPinValid);
        if (Objects.equals(isTerminalValid, "true")) {
            logger.info("Entered HSM phase..");
            String pin = hsmService.communicateWithHSM(driver, driver.getPin());
            logger.debug("Encrypted pin: {}", pin);
            driver.setHsmPin(pin);
            if (!(driver.getNew_pin() == null)) {
                String newPinBlock = hsmService.communicateWithHSM(driver, driver.getNew_pin());
                driver.setDecodedNewPin(newPinBlock);
            }
            byte[] isoMsg = iso8583Service.createIso8583Message(driver, pin);
            notificationService.sendTransactionNotification("success");
            logger.info("Iso message created");
            if (isoMsg == null) {
                logger.info("====================================================");
                logger.info("End of Transaction");
                logger.info("====================================================");
                return ResponseEntity.ok(new PosTransRes("Error in ISO message creation", "false", "false"));
            }

            byte[] switchResponse = switchService.connectToSwitch(isoMsg, driver);
            if (switchResponse == null) {
                logger.info("====================================================");
                logger.info("End of Transaction");
                logger.info("====================================================");
                return ResponseEntity.ok(new PosTransRes("Socket connection failed.", "false", "false"));
            }

            String receiveResponse = iso8583Service.setResponse(switchResponse, driver);
            logger.info("iso response :" + receiveResponse);
            logger.info("====================================================");
            logger.info("End of Transaction");
            logger.info("====================================================");
            return ResponseEntity.ok(receiveResponse);
        }
        logger.info("\n\n");
        return (ResponseEntity<?>) ResponseEntity.ok(new PosTransRes("Verification failed.", "false", "false"));
    }



    @PostMapping("/test2")
    public String testApi(@RequestBody DriverRequest driverRequest) {
        return "";
    }

}
