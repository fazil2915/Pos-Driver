package com.example.pos_driver.Service;

import com.example.pos_driver.Hsm.BAcommand;
import com.example.pos_driver.Hsm.JGCommand;
import com.example.pos_driver.Model.DriverRequest;
import com.example.pos_driver.Model.Terminal;
import com.example.pos_driver.Repo.PinDecryption;
import com.example.pos_driver.Utils.DataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Optional;

@Service
public class HsmService {

    private final PinDecryption pinDecryption;

    @Autowired
    private VitaService vitaService;

    private static final Logger logger = LoggerFactory.getLogger(HsmService.class);

    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=Aptpos;user=sa;password=password@123";

    @Autowired
    public HsmService(PinDecryption pinDecryption) {
        this.pinDecryption = pinDecryption;
    }

    public String communicateWithHSM(DriverRequest driverRequest, String pinBlock) throws IOException {
//
        String pan = driverRequest.getPan();
        logger.debug("Command sending to hsm for clear pin..");
        String pin = pinDecryption.pinDecrypting(pinBlock);

        // Fetch terminal and HSM details
        Optional<Terminal> terminalOptional = vitaService.findTerminalBySerialNumber(driverRequest.getSl_no());
        if (!terminalOptional.isPresent()) {
            logger.error("Terminal not found for serial number: {}", driverRequest.getSl_no());
            return null;
        }
        Terminal terminal = terminalOptional.get();
        String hsmHost = terminal.getHsm().getIp();
        String hsmPort = terminal.getHsm().getPort();

        System.out.println("Host : "+ hsmHost+" Port : "+hsmPort);
        HsmConnection hsmCon = new HsmConnection(hsmHost, Integer.parseInt(hsmPort));

        String  encryptedPin = sendBaCommand(pin,pan,hsmCon);
        String KEY= vitaService.getKeyValue("POS_ZMK");
        String encryptedPinafterJG = sendJgCommand(KEY,pan,encryptedPin,hsmCon);

        return encryptedPinafterJG;
    }


    private String sendBaCommand(String pin, String  pan, HsmConnection hsmCon){
        try {

            logger.info("Sending BA command to HSM.");
            BAcommand baCommand = new BAcommand.BAcommandBuilder()
                    .withPin(pin)
                    .withAccountNumber(DataValidator.makeAccountNumberFromPan(pan))
                    .build();

            System.out.println("BACommand : " + baCommand.toString());


            hsmCon.sendCommand(baCommand);
            baCommand.parse(hsmCon.getResponse());
            String encryptedPin = baCommand.getEncryptedPin();
            logger.info("Response BB received  from  HSM.");
            return encryptedPin;

        }catch (IOException e){
            hsmCon.close();
            logger.info("Error during sending BA command to HSM.");
            throw new RuntimeException("Error during Ba Command: "+e.getMessage());
        }
    }

    private String sendJgCommand(String key,String pan,String encryptedPin,HsmConnection hsmCon){

        try{
            logger.info("Sending JG command to HSM.");
            JGCommand jgCommand = new JGCommand.JGCommandBuilder()
                    .withKey(key)
                    .withAccountNumber(DataValidator.makeAccountNumberFromPan(pan))
                    .withEncPin(encryptedPin)
                    .build();

            System.out.println("JGCommand : " + jgCommand.toString());

            hsmCon.sendCommand(jgCommand);
            jgCommand.parse(hsmCon.getResponse());

            String response = jgCommand.getResponse();
            System.out.println("HSM Response: " + response);

            String encryptedPinafterJG = jgCommand.getEncryptedPin();
            logger.info("Response JH received  from  HSM.");
            System.out.println("Encrypted PIN from Hsm: " + encryptedPinafterJG);
            return  encryptedPinafterJG;

        }catch (IOException e){
            hsmCon.close();
            logger.info("Error during sending JG command to HSM.");
            throw new RuntimeException("Error during Ba Command: "+e.getMessage());
        }

    }
    private String extractAmount(String pan) {
        if (pan == null || pan.length() < 13) {
            throw new IllegalArgumentException("Invalid PAN length");
        }
        return pan.substring(pan.length() - 13, pan.length() - 1);
    }

    private String extractPin(String hsmResponse) {
        if (hsmResponse == null || hsmResponse.length() < 5) {
            logger.error("Invalid HSM response for PIN extraction");
            return null;
        }
        return hsmResponse.substring(hsmResponse.length() - 5);
    }

    private String extractPinBlock(String hsmResponse) {
        if (hsmResponse == null || hsmResponse.length() < 16) {
            logger.error("Invalid HSM response for PIN block extraction");
            return null;
        }
        return hsmResponse.substring(hsmResponse.length() - 16);
    }

    private Optional<String> fetchKeyFromDatabase(String query) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return Optional.of(rs.getString(1));
            }

        } catch (SQLException e) {
            logger.error("Database query error: {}", e.getMessage(), e);
        }

        return Optional.empty();
    }
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        logger.debug("hex data "+data);
        return data;
    }


}
