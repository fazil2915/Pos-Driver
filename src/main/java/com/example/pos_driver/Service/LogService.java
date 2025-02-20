package com.example.pos_driver.Service;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.util.StatusPrinter;
import com.example.pos_driver.Model.LogConfig;
import com.example.pos_driver.Repo.LogConfigRepo;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URL;
import java.util.Optional;

@Service
public class LogService {

    private static String lastUsedLogDir = null;

    @Autowired
    private LogConfigRepo logConfigRepo;

    @PostConstruct // Auto-run on server startup
    public void loadConfigOnStartup() {
        Optional<LogConfig> config =logConfigRepo.findById(1L); // Assume 1 record exists
        if (config.isPresent()) {
            LogConfig logConfig = config.get();
            reloadConfiguration(logConfig.getLogDir(), logConfig.getLogSize(), logConfig.getConsoleEnabled());
        } else {
            reloadConfiguration(null, null, null); // Load defaults if no config exists
        }
    }

    public void reloadConfiguration(String customLogDir, String customFileSize, Boolean consoleEnabled) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            context.reset(); // Clear existing log settings

            // Fetch from DB or use defaults
            String logSize = (customFileSize != null) ? customFileSize : "10MB";
            boolean consoleLoggingEnabled = (consoleEnabled != null) ? consoleEnabled : true;

            context.putProperty("LOG_SIZE", logSize);
            context.putProperty("LOG_LEVEL", System.getProperty("log.level", "INFO"));

            context.getLogger("ROOT").setLevel(Level.toLevel(System.getProperty("log.level", "INFO")));

            String logDir = customLogDir;
            if (logDir != null) {
                File logDirectory = new File(logDir);
                if (!logDirectory.exists()) {
                    System.out.println("🔍 Attempting to create log directory: " + logDir);
                    boolean dirCreated = logDirectory.mkdirs();
                    System.out.println(dirCreated);

                    if (!dirCreated) {
                        System.err.println("❌ Failed to create log directory: " + logDir + ". Falling back to default.");
                        logDir = System.getProperty("user.dir") + "/logs"; // Default to user home directory
                    } else {
                        System.out.println("✅ Log directory created: " + logDir);
                    }
                }

                // Check if directory is writable
                if (!logDirectory.canWrite()) {
                    System.err.println("❌ No write permission for " + logDir + ". Using default directory.");
                    logDir = System.getProperty("user.dir") + "/logs";
                }
            } else {
                logDir = System.getProperty("user.dir") + "/logs"; // Default location
            }

            context.putProperty("LOG_DIR", logDir);
            System.out.println("📁 Log directory set to: " + logDir);

            // Apply logback configuration
            URL logbackConfig = LogService.class.getClassLoader().getResource("logback-spring.xml");
            if (logbackConfig != null) {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                configurator.doConfigure(logbackConfig);
                System.out.println("✅ Logback configuration reloaded successfully.");
            } else {
                System.err.println("❌ logback-spring.xml not found in resources!");
            }

            StatusPrinter.printInCaseOfErrorsOrWarnings(context);

            // Save updated configuration in DB
            LogConfig config = new LogConfig();
            config.setId(1L); // Assume single configuration entry
            config.setLogDir(logDir);
            config.setLogSize(logSize);
            config.setConsoleEnabled(consoleEnabled);
            logConfigRepo.save(config);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
