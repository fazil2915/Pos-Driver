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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import postilion.realtime.sdk.util.XPostilion;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api")
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

    @PostMapping("/posDriver")
    public ResponseEntity<?> checkTerminal(@RequestBody DriverRequest driver) throws IOException, XPostilion {
        logger.info("Received request to check terminal for serial number: {}", driver.getSl_no());

        if (driver.getSl_no() == null || driver.getSl_no().trim().isEmpty()) {
            logger.warn("Serial number is missing in the request.");
            return ResponseEntity.ok(new PosTransRes("Serial number is required.", "false", "false"));
        }

        if (driver.getPin() == null || driver.getPin().trim().isEmpty()) {
            logger.warn("Pin number is missing in the request.");
            return ResponseEntity.ok(new PosTransRes("Pin number is required.", "false", "false"));
        }

        String isTerminalValid = cardService.verifyTransaction(driver.getSl_no());
        String isPinValid = pinDecryption.pinDecrypting(driver.getPin());
        driver.setDecodedPin(isPinValid);
        if (Objects.equals(isTerminalValid, "true")) {
            logger.info("Entered HSM phase..");
            String pin = hsmService.communicateWithHSM(driver,driver.getPin());
            logger.debug("Encrypted pin: {}", pin);
            driver.setHsmPin(pin);
            if(!(driver.getNew_pin() == null)){
                String newPinBlock = hsmService.communicateWithHSM(driver,driver.getNew_pin());
                driver.setDecodedNewPin(newPinBlock);
            }
            byte[] isoMsg = iso8583Service.createIso8583Message(driver, pin);
            logger.info("Iso message created");
            if (isoMsg == null) {
                return ResponseEntity.ok(new PosTransRes("Error in ISO message creation", "false", "false"));
            }

            byte[] switchResponse = switchService.connectToSwitch(isoMsg, driver);
            if (switchResponse == null) {
                return ResponseEntity.ok(new PosTransRes("Socket connection failed.", "false", "false"));
            }

            String receiveResponse = iso8583Service.setResponse(switchResponse, driver);
            logger.info("iso response :" + receiveResponse);
            return ResponseEntity.ok(receiveResponse);
        }

        return (ResponseEntity<?>) ResponseEntity.ok(new PosTransRes("Verification failed.", "false", "false"));
    }

    private static final String LOGS_DIRECTORY = "logs"; // Adjust if needed

    // Get a list of all available log files
    @GetMapping("/logs")
    public List<String> listLogFiles() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(LOGS_DIRECTORY), "*.log")) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    // Get logs for a specific date (format: YYYY-MM-DD)
    @GetMapping("/logs/{date}")
    public List<String> getLogsByDate(@PathVariable String date) throws IOException {
        String logFileName = "application-" + date + ".log";
        Path logFilePath = Paths.get(LOGS_DIRECTORY, logFileName);
        Path logFilePath2 = Paths.get(LOGS_DIRECTORY, "application.log");

        if ((!Files.exists(logFilePath)) && (!Files.exists(logFilePath2))) {
            throw new IOException("Log file for " + date + " not found.");
        }
        System.out.println("logFileName: "+ logFilePath);
        if (logFilePath.toString().equals("logs\\application-application.log")) {
            logFileName = "application.log";
             logFilePath = Paths.get(LOGS_DIRECTORY, logFileName);

        }

        // Read the log file, filter by .Service. or .Controller., and insert line
        // breaks after specific messages
        return Files.lines(logFilePath)
                .filter(line -> line.contains(".Service.") || line.contains(".Controller."))
                .map(line -> {
                    // Insert a line break after specific patterns
                    if (line.contains("Switch connection failed:")) {
                        return line + "\n"; // Add a line break after SwitchService message
                    } else if (line.contains(".Controller.DriverController")) {
                        return line + "\n"; // Add a line break after DriverController message
                    }
                    return line; // No change for other lines
                })
                .flatMap(line -> {
                    // Here we split the log line to ensure it gets a line break after the matched
                    // conditions
                    return Stream.of(line.split("\n"));
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/test2")
    public String testApi(@RequestBody DriverRequest driverRequest) {
        return "";
    }
}
