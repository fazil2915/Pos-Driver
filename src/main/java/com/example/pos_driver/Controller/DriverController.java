package com.example.pos_driver.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

    @GetMapping("/test")
    public String firstName() {
        logger.info("requesting to  pos readed");
        return "test api";
    }
}
