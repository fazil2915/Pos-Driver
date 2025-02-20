package com.example.pos_driver.Controller;

import com.example.pos_driver.Model.LogConfig;
import com.example.pos_driver.Service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController

@RequestMapping("/api")
public class LogsController {


    @Autowired
    private LogService logService;

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
    public String reloadLogConfig(@RequestBody LogConfig request) {
        logService.reloadConfiguration(request.getLogDir(), request.getLogSize(), request.getConsoleEnabled());
        return "✅ Log configuration updated and stored in DB!";
    }

}
