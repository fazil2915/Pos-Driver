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
        notificationService.sendTransactionNotification("success");
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
            logger.info("Iso message created");
            if (isoMsg == null) {
                logger.info("====================================================");
                logger.info("End of Transaction");
                logger.info("====================================================\n\n\n");
                return ResponseEntity.ok(new PosTransRes("Error in ISO message creation", "false", "false"));
            }

            byte[] switchResponse = switchService.connectToSwitch(isoMsg, driver);
            if (switchResponse == null) {
                logger.info("====================================================");
                logger.info("End of Transaction");
                logger.info("====================================================\n\n\n");
                return ResponseEntity.ok(new PosTransRes("Socket connection failed.", "false", "false"));
            }

            String receiveResponse = iso8583Service.setResponse(switchResponse, driver);
            logger.info("iso response :" + receiveResponse);
            logger.info("====================================================");
            logger.info("End of Transaction");
            logger.info("====================================================\n\n\n");
            return ResponseEntity.ok(receiveResponse);
        }
        logger.info("\n\n");
        return (ResponseEntity<?>) ResponseEntity.ok(new PosTransRes("Verification failed.", "false", "false"));
    }

    private static final String LOGS_DIRECTORY = "logs"; // Adjust if needed

    // Get a list of all available log files
    @Operation(summary = "Get name of all logs.", description = "Fetch file name of all logs in the pos driver.")
    @GetMapping("/logs")
    public List<String> listLogFiles() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(LOGS_DIRECTORY), "*.log")) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    // Get logs for a specific date (format: YYYY-MM-DD)4
    @Operation(summary = "Get specific log.", description = "Fetch specific log based on name.")
    @GetMapping("/logs/{date}")
    public List<String> getLogsByDate(@PathVariable String date) throws IOException {
        String logFileName = "application-" + date + ".log";
        Path logFilePath = Paths.get(LOGS_DIRECTORY, logFileName);
        Path logFilePath2 = Paths.get(LOGS_DIRECTORY, "application.log");

        if ((!Files.exists(logFilePath)) && (!Files.exists(logFilePath2))) {
            throw new IOException("Log file for " + date + " not found.");
        }
        System.out.println("logFileName: " + logFilePath);
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


    @PostMapping("/update/logSettings")
    public ResponseEntity<Map<String, String>> updateLoggingConfig(@RequestBody Map<String, String> config) {
        Map<String, String> response = new HashMap<>();

        config.forEach((key, value) -> {
            System.setProperty("log." + key, value);
            response.put("log." + key, value);
        });

        // Reload Logback configuration
        LogService.reloadConfiguration();

        logger.warn("Updated log settings: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update/consoleLogging")
    public ResponseEntity<Map<String, String>> updateConsoleLogging(@RequestParam boolean enableConsoleLogging) {
        
        System.setProperty("log.console.enabled", String.valueOf(enableConsoleLogging));

        // Reload Logback configuration
        LogService.reloadConfiguration();

        Map<String, String> response = new HashMap<>();
        response.put("log.console.enabled", String.valueOf(enableConsoleLogging));

        logger.info("Updated console logging setting: {}", enableConsoleLogging);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update/logLevel")
    public ResponseEntity<Map<String, String>> updateLogLevel(@RequestParam String level) {
        System.setProperty("log.level", level.toUpperCase());

        // Reload Logback configuration
        LogService.reloadConfiguration();

        Map<String, String> response = new HashMap<>();
        response.put("log.level", level.toUpperCase());

        logger.warn("Log level updated to {}", level.toUpperCase());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/test2")
    public String testApi(@RequestBody DriverRequest driverRequest) {
        return "";
    }

}
