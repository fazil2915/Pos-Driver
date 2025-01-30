package com.example.pos_driver.Controller;

import com.example.pos_driver.Model.DriverRequest;
import com.example.pos_driver.Service.HsmService;
import com.example.pos_driver.Service.Iso8583Service;
import com.example.pos_driver.Service.VitaService;
import com.example.pos_driver.Service.SwitchService;
import com.example.pos_driver.dto.PosTransRes;
import com.example.pos_driver.Repo.PinDecryption;
import com.example.pos_driver.Service.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import postilion.realtime.sdk.util.XPostilion;

import java.io.IOException;
import java.util.Objects;

@RestController
public class DriverController {

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

    @PostMapping("/test")
    public PosTransRes checkTerminal(@RequestBody DriverRequest driver) throws IOException, XPostilion {
        logger.info("Received request to check terminal for serial number: {}", driver.getSl_no());

        if (driver.getSl_no() == null || driver.getSl_no().trim().isEmpty()) {
            logger.warn("Serial number is missing in the request.");
            return new PosTransRes("Serial number is required.", "false", "false");
        }

        if (driver.getPin() == null || driver.getPin().trim().isEmpty()) {
            logger.warn("Pin number is missing in the request.");
            return new PosTransRes("Pin number is required.", "false", "false");
        }

        String isTerminalValid = cardService.verifyTransaction(driver.getSl_no());
        String isPinValid = pinDecryption.pinDecrypting(driver.getPin());

        if (Objects.equals(isTerminalValid, "true")) {
            logger.info("Entered HSM phase..");
            String pin = hsmService.communicateWithHSM(driver);
            logger.debug("Encrypted pin: {}", pin);
            byte[] isoMsg = iso8583Service.createIso8583Message(driver, pin);
            logger.info("iso message");
            if (isoMsg == null) {
                return new PosTransRes("Error in ISO message creation", "false", "false");
            }

            byte[] switchResponse = switchService.connectToSwitch(isoMsg, driver);
            if (switchResponse == null) {
                return new PosTransRes("Socket connection failed.", "false", "false");
            }

            String receiveResponse = iso8583Service.setResponse(switchResponse,driver);
            logger.info("iso response :"+receiveResponse);
            return new PosTransRes("Transaction Verified & iso 210 response recieved!!.", isTerminalValid, isPinValid);
        }

        return new PosTransRes("Verification failed.", "false", "false");
    }

    @PostMapping("/test2")
    public String testApi(@RequestBody DriverRequest driverRequest) {
        return "";
    }
}
