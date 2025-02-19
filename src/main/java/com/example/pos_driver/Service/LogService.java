package com.example.pos_driver.Service;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;

@Service
public class LogService {

    private static String lastUsedLogDir = null;

    public static void reloadConfiguration(String customLogDir) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            context.reset(); // Clear existing log settings

            // Inject dynamic properties
            context.putProperty("LOG_HISTORY", System.getProperty("log.history", "10")); // Default 10
            context.putProperty("LOG_SIZE", System.getProperty("log.size", "20MB")); // Default 20MB
            context.putProperty("LOG_LEVEL", System.getProperty("log.level", "INFO")); // Default INFO

            // Set log level dynamically
            context.getLogger("ROOT").setLevel(Level.toLevel(System.getProperty("log.level", "INFO")));

            // Handle console logging enable/disable
            boolean consoleLoggingEnabled = Boolean.parseBoolean(System.getProperty("log.console.enabled", "true"));
            Appender consoleAppender = context.getLogger("ROOT").getAppender("CONSOLE");

            if (consoleAppender instanceof ConsoleAppender) {
                consoleAppender.stop(); // Stop appender before modifying it
                if (!consoleLoggingEnabled) {
                    context.getLogger("ROOT").detachAppender(consoleAppender);
                    System.out.println("❌ Console logging is disabled.");
                } else {
                    context.getLogger("ROOT").addAppender(consoleAppender);
                    System.out.println("✅ Console logging is enabled.");
                }
            }

            // Get project root directory
            String projectRoot = System.getProperty("user.dir");

            // Default log directory inside the project root
            String defaultLogDir = projectRoot + "/logs";
            String logDir = defaultLogDir;

            // Override if custom directory is provided and valid
            if (customLogDir != null && !customLogDir.trim().isEmpty() && !customLogDir.equals(lastUsedLogDir)) {
                File customDir = new File(projectRoot, customLogDir);
                if (customDir.exists() || customDir.mkdirs()) {
                    logDir = customDir.getAbsolutePath();
                } else {
                    System.err.println("⚠️ Failed to use custom log directory. Using default: " + defaultLogDir);
                    logDir = defaultLogDir;
                }
            }

            // Avoid redundant directory creation check
            if (!logDir.equals(lastUsedLogDir)) {
                File logDirectory = new File(logDir);
                System.out.println("Log directory path: " + logDirectory.getAbsolutePath());

                if (!logDirectory.exists()) {
                    boolean created = logDirectory.mkdirs();
                    if (created) {
                        System.out.println("✅ Log directory created at: " + logDir);
                    } else {
                        System.err.println("❌ Failed to create log directory: " + logDir);
                        System.err.println("Can write to project root? " + new File(projectRoot).canWrite());
                    }
                } else {
                    System.out.println("ℹ️ Log directory already exists.");
                }
                lastUsedLogDir = logDir;
            }

            // Load logback-spring.xml
            URL logbackConfig = LogService.class.getClassLoader().getResource("logback-spring.xml");
            if (logbackConfig != null) {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                configurator.doConfigure(logbackConfig);
                System.out.println("✅ Logback configuration reloaded successfully.");
            } else {
                System.err.println("❌ logback-spring.xml not found in resources!");
            }

            // Print errors/warnings if any
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Overloaded method to use the default log directory
    public static void reloadConfiguration() {
        reloadConfiguration(null);
    }
}
