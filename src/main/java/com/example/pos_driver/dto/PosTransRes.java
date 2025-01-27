package com.example.pos_driver.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PosTransRes {
    private String message;
    private String isTerminalValid;
    private String isPinValid;
}
