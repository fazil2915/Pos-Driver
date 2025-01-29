package com.example.pos_driver.Repo;

public interface HsmCommand {

    byte[] build();
    void parse(byte[] responseData);
}

