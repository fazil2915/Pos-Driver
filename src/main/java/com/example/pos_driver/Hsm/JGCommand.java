package com.example.pos_driver.Hsm;

import com.example.pos_driver.Repo.HsmCommand;
import com.example.pos_driver.Service.HsmConnection;
import com.example.pos_driver.Utils.DataValidator;

import java.io.IOException;
import java.net.UnknownHostException;

public class JGCommand implements HsmCommand {

    private final String key;
    private final String accountNumber;
    private final String encPin;
    private String response;
    private String encryptedPin; 

    public JGCommand(String key, String accountNumber, String encPin) {
//        if (!DataValidator.isValidKey(key)) {
//            throw new IllegalArgumentException("Invalid key format.");
//        }
//        if (!DataValidator.isValidAccountNumber(accountNumber)) {
//            throw new IllegalArgumentException("Invalid account number format.");
//        }
//        if (!DataValidator.isValidEncPin(encPin)) {
//            throw new IllegalArgumentException("Invalid encrypted PIN format.");
//        }

        this.key = key;
        this.accountNumber = accountNumber;
        this.encPin = encPin;
    }

    public static class JGCommandBuilder {
        private String key;
        private String accountNumber;
        private String encPin;

        public JGCommandBuilder withKey(String key) {
            this.key = key;
            return this;
        }

        public JGCommandBuilder withAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public JGCommandBuilder withEncPin(String encPin) {
            this.encPin = encPin;
            return this;
        }

        public JGCommand build() {
            return new JGCommand(key, accountNumber, encPin);
        }
    }

    @Override
    public byte[] build() {
        StringBuilder commandJG = new StringBuilder();
        commandJG.append("JG")  
                 .append("U")    
                 .append(key)  
                 .append("01")   
                 .append(accountNumber) 
                 .append(encPin); 
        return commandJG.toString().getBytes();
    }

    @Override
    public void parse(byte[] responseData) {
        if (responseData == null || responseData.length < 16) {
            throw new IllegalArgumentException("Invalid response data.");
        }

        response = new String(responseData);

        encryptedPin = response.substring(response.length() - 16);
    }

    public String getResponse() {
        return response;
    }

    public String getEncryptedPin() {
        return encryptedPin;
    }


    public static void main(String[] args) throws UnknownHostException, IOException {
        String ip = "192.168.100.63";
        int port = 9005;

        HsmConnection hsmCon = new HsmConnection(ip, port);

        try {
            BAcommand baCommand = new BAcommand.BAcommandBuilder()
                    .withPin("5822")
                    .withAccountNumber(DataValidator.makeAccountNumberFromPan("4048345005560466"))
                    .build();

            hsmCon.sendCommand(baCommand);
            baCommand.parse(hsmCon.getResponse());
            String encryptedPin = baCommand.getEncryptedPin();
            System.out.println("Enc pin : " + encryptedPin);

            JGCommand jgCommand = new JGCommandBuilder()
                    .withKey("BBF2F8F8498A2D6B02C5449834CF6D04")
                    .withAccountNumber(DataValidator.makeAccountNumberFromPan("4048345005560466"))
                    .withEncPin(encryptedPin)
                    .build();

            System.out.println("Command bytes: " + new String(jgCommand.build()));

            hsmCon.sendCommand(jgCommand);
            jgCommand.parse(hsmCon.getResponse());

            String response = jgCommand.getResponse();
            System.out.println("HSM Response: " + response);

            String encryptedPinafterJG = jgCommand.getEncryptedPin();
            System.out.println("Encrypted PIN: " + encryptedPinafterJG);

        } finally {
            hsmCon.close();
        }
    }

    @Override
    public String toString() {
        return "JGCommand{ JGU"+key+"01"+accountNumber+encPin+" }";
    }
}
