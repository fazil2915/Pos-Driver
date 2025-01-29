package com.example.pos_driver.Controller;

import com.example.pos_driver.Model.DriverRequest;
import com.example.pos_driver.Model.Terminal;
import com.example.pos_driver.Service.HsmService;
import com.example.pos_driver.Service.Iso8583Service;
import com.example.pos_driver.Service.VitaService;
import com.example.pos_driver.dto.PosTransRes;
import com.example.pos_driver.Repo.PinDecryption;
import com.example.pos_driver.Service.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import postilion.realtime.sdk.util.XPostilion;

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
            String msg= "";
            if(Objects.equals(isTerminalValid, "true")){
                logger.info("entered hsm phase..");
                String pin = hsmService.communicateWithHSM(driver);
                logger.debug("Encrypted pin: "+pin);
             byte[] IsoMsg = iso8583Service.createIso8583Message(driver,pin);
             if(IsoMsg == null){
                 return ResponseEntity.status(404).body("Error in Iso message Creation") ;
             }

                System.out.println("Message Received");


                Socket socket = null;
                BufferedOutputStream outStream = null;
                DataInputStream dis = null;
                    Terminal terminal = vitaService.findTerminalBySerialNumber(driver.getSl_no()).get();
                    String host =terminal.getSwitchs().getIp();
                    String port=terminal.getSwitchs().getPort();
                    socket = new Socket(host, Integer.parseInt(port));
                    outStream = new BufferedOutputStream(socket.getOutputStream());
                    dis = new DataInputStream(socket.getInputStream());

                    logger.info("HSM Connected to {}:{}", host, port);
                    logger.debug("message socket {}", socket);

                    // Convert the string message to raw bytes using ISO-8859-1 encoding
//                    byte[] messageBytes = message.getBytes(StandardCharsets.ISO_8859_1);
//                    logger.debug("Message bytes (raw): {}", javax.xml.bind.DatatypeConverter.printHexBinary(messageBytes));

//                    logger.debug("message byte :"+messageBytes);
//                    outStream.write();
//                    outStream.flush();

//                    logger.info("Sent to HSM: {}", javax.xml.bind.DatatypeConverter.printHexBinary(messageBytes));


//                    int responseLength = dis.readUnsignedShort();
//
//                    logger.debug("response Length "+responseLength);
//                    if (responseLength > 0) {
//                        byte[] response = new byte[responseLength];
//                        dis.readFully(response, 0, responseLength);
//                        String responseString = new String(response);
//                        logger.info("Response from HSM: {}", responseString);
//                        return responseString;
//













            String message = ("true".equals(isTerminalValid) && "true".equals(isPinValid))
                    ? "Terminal and PIN verification successful."
                    : "Terminal or PIN verification failed.";
            }
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


    @PostMapping("/test2")
    public String testApi(@RequestBody DriverRequest driverRequest) throws XPostilion, IOException {
//        return iso8583Service.createIso8583Message(driverRequest, "2343223423");
        return "";
    }
}
