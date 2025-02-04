package com.example.pos_driver.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverRequest {
    private String sl_no;
    private String amount;
    private String pan;
    private String pin;
    private String track2;
    private String date;
    private String ireq_transaction_type;
    private String icc_req_data;
    private String decodedPin;
    private String stan;
    private String hsmPin;
    private String new_pin;
    private String decodedNewPin;
}
