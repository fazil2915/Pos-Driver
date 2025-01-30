package com.example.pos_driver.Controller;

import com.example.pos_driver.Model.DriverRequest;
import com.example.pos_driver.Model.Terminal;
import com.example.pos_driver.Service.*;
import com.example.pos_driver.dto.PosTransRes;
import com.example.pos_driver.Repo.PinDecryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import postilion.realtime.sdk.util.XPostilion;

import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RestController
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

    @Autowired
    private HsmService hsmService;


    @Autowired
    private SwitchService switchService;


    @Autowired
    private Iso8583Service iso8583Service;


    @Autowired
    private VitaService vitaService;
    @Autowired
    private CardService cardService;

    //    @Autowired
    private PinDecryption pinDecryption;

    @Autowired
    public DriverController(PinDecryption pinDecryption) {
        this.pinDecryption = pinDecryption;
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public ResponseEntity<?> checkTerminal(@RequestBody DriverRequest driver) {
        try {
            logger.info("Received request to check terminal for serial number: {}", driver.getSl_no());

            // Validate Serial Number
            if (driver.getSl_no() == null || driver.getSl_no().trim().isEmpty()) {
                logger.warn("Serial number is missing in the request.");
                return new ResponseEntity<>(
                        new PosTransRes("Serial number is required.", "false", "false"), HttpStatus.BAD_REQUEST);
            }

            // Validate PIN
            if (driver.getPin() == null || driver.getPin().trim().isEmpty()) {
                logger.warn("Pin number is missing in the request.");
                return new ResponseEntity<>(
                        new PosTransRes("Pin number is required.", "false", "false"), HttpStatus.BAD_REQUEST);
            }

            // Check Terminal and PIN Validity
            String isTerminalValid = cardService.verifyTransaction(driver.getSl_no());
            String isPinValid = pinDecryption.pinDecrypting(driver.getPin());
            String msg = "";
            if (Objects.equals(isTerminalValid, "true")) {
                logger.info("entered hsm phase..");
                String pinBlock = hsmService.communicateWithHSM(driver);
                logger.debug("Encrypted pin: " + pinBlock);
                byte[] IsoMsg = iso8583Service.createIso8583Message(driver, pinBlock);
                if (IsoMsg == null) {
                    return ResponseEntity.status(403).body(new PosTransRes("Error in Iso message Creation",isTerminalValid,isPinValid));
                }
                System.out.println(" ISO Message Received");

                byte[]  switchResponse = switchService.sendIsoMsgToSwitch(driver,IsoMsg);
                logger.info("Response after ISO send to switch : "+switchResponse);


                String lastRes = iso8583Service.setResponse(switchResponse);
                System.out.println("Response:  "+ lastRes);
                if (lastRes != null){
                    return ResponseEntity.status(200).body(lastRes);
                }else {
                    return ResponseEntity.status(403).body(new PosTransRes("Error in Iso message Creation",isTerminalValid,isPinValid));
                }



            }
            String message = ("true".equals(isTerminalValid) && "true".equals(isPinValid))
                    ? "Terminal and PIN verification successful."
                    : "Terminal or PIN verification failed.";
            // Create and Return Response
            PosTransRes response = new PosTransRes(msg, isTerminalValid, isPinValid);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("An error occurred while processing the request.", e);
            return new ResponseEntity<>(
                    new PosTransRes("Internal server error. Please try again later.", "false", "false"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
