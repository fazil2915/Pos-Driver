package com.example.pos_driver.Service;

import com.example.pos_driver.Model.CardData;
import com.example.pos_driver.Model.DriverRequest;
import com.example.pos_driver.Model.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class CardService {
    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    @Autowired
    private VitaService vitaService;

    public String verifyTransaction(String serialNumber) {
        logger.info("Starting transaction processing for serial number: " + serialNumber);

        Optional<Terminal> terminalOptional = vitaService.findTerminalBySerialNumber(serialNumber);

        if (terminalOptional.isPresent()) {
            Terminal terminal = terminalOptional.get();
            logger.info("Terminal found: " + terminal.toString());
            return "true";
//            return "Transaction processed for terminal: " + terminal.getSerialNumber();
        } else {
            logger.warn("No terminal found for serial number: " + serialNumber);
            return "false";
          //  return "Terminal not found for serial number: " + serialNumber;
        }
    }

}
