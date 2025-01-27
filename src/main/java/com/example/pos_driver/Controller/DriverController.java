package com.example.pos_driver.Controller;

import com.example.pos_driver.Model.DriverRequest;
import com.example.pos_driver.Service.HsmService;
import com.example.pos_driver.dto.PosTransRes;
import com.example.pos_driver.Repo.PinDecryption;
import com.example.pos_driver.Service.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

    @Autowired
    HsmService hsmService;
    @Autowired
    private CardService cardService;

//    @Autowired
    private PinDecryption pinDecryption;

    @Autowired
    public DriverController(PinDecryption pinDecryption) {
        this.pinDecryption = pinDecryption;
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public ResponseEntity<PosTransRes> checkTerminal(@RequestBody DriverRequest driver) {
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
            if(isTerminalValid == "true"){
                logger.info("entered hsm phase..");
                hsmService.communicateWithHSM(driver);
            }

            // Create Response Message
            String message = ("true".equals(isTerminalValid) && "true".equals(isPinValid))
                    ? "Terminal and PIN verification successful."
                    : "Terminal or PIN verification failed.";

            // Create and Return Response
            PosTransRes response = new PosTransRes(message, isTerminalValid, isPinValid);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("An error occurred while processing the request.", e);
            return new ResponseEntity<>(
                    new PosTransRes("Internal server error. Please try again later.", "false", "false"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
