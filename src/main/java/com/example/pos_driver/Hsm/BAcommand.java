package com.example.pos_driver.Hsm;

import com.example.pos_driver.Repo.HsmCommand;
import com.example.pos_driver.Service.HsmConnection;
import com.example.pos_driver.Utils.DataValidator;

import java.io.IOException;
import java.net.UnknownHostException;

public class BAcommand implements HsmCommand {
	
	String pin;
	String accountNumber;
	String encryptedPin;
	
	public BAcommand(String pin, String accountNumber) {
		this.pin = pin;
		this.accountNumber = accountNumber;
		if(!DataValidator.isValidPin(pin)) {
			throw new NullPointerException("Invalid Pin data");
		}
		if(!DataValidator.isValidAccountNumber(accountNumber)) {
			throw new NullPointerException("Invalid Account number data");
		}
	}
	
	public BAcommand(BAcommandBuilder builder) {
		this(builder.pin, builder.accountNumber);
	}	

	@Override
	public byte[] build() {
		StringBuilder commandBA = new StringBuilder();
		commandBA.append("BA")
				 .append(pin)
				 .append(accountNumber);
		return (commandBA.toString()).getBytes();
	}
	
	@Override
	public void parse(byte[] responseData) {
		if(responseData == null) throw new NullPointerException();
		String responseStr = new String(responseData);
		encryptedPin = responseStr.substring(6, responseStr.length());
	}
	
	public String getEncryptedPin() {
		return this.encryptedPin;
	}
	
	public static class BAcommandBuilder {
		private String pin;
		private String accountNumber;
		
		public BAcommandBuilder() {
			
		}
		
		public BAcommandBuilder withPin(String pin) {
			this.pin = pin;
			return this;
		}
		
		public BAcommandBuilder withAccountNumber(String accountNumber) {
			this.accountNumber = accountNumber;
			return this;
		}
		
		public BAcommand build() {
			return new BAcommand(this);
		}
		
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		
		String ip = "192.168.100.63";
        int port = 9005;
        HsmConnection hsmCon = new HsmConnection(ip, port);
		
        BAcommand baCommand = new BAcommandBuilder()
                .withPin("5822")
                .withAccountNumber(DataValidator.makeAccountNumberFromPan("4048345005560466"))
                .build();
        
        System.out.println("Command bytes : " + new String(baCommand.build()));
        hsmCon.sendCommand(baCommand);
        baCommand.parse(hsmCon.getResponse());
        String encryptedPin = baCommand.getEncryptedPin();
        System.out.println("Enc pin : " + encryptedPin);
        hsmCon.close();

	}

//	@Override
//	public String toString() {
//		return "JGCommand{ JGU"+key+"01"+accountNumber+encPin+" }";
//	}

}

