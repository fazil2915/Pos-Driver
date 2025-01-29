package com.example.pos_driver.Service;


import org.springframework.stereotype.Service;

@Service
public class IccCardService {

    public  String getTempIcc(String Icc) {
        // Calculate half the length of the Icc string
        int halfLength = Icc.length() / 2;
        // Generate a string of spaces with the length equal to half the Icc length
        String allSpacesString = new String(new char[halfLength]).replace('\0', ' ');

        return allSpacesString;
    }
}
