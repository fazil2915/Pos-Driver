package com.example.pos_driver.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DriverController {

    @GetMapping("/test")
    public String firstName(){
        return "test api";
    }

}
