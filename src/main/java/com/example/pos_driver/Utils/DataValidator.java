package com.example.pos_driver.Utils;

public class DataValidator {
	static String DEFAULT_DECIMALISATION = "0123456789012345";
	
	
	public static boolean isValidAccountNumber(String accountNumber) {
		if(accountNumber == null || accountNumber.length() != 12) return false;
		return true;
	}
	
	public static boolean isValidPin(String pin) {
		if(pin == null || pin.length() != 4) return false;
		return true;
	}
	
	public static boolean isValidEncryptedPin(String encPin) {
		if(encPin == null || encPin.length() != 5) return false;
		return true;
	}
	
	public static boolean isValidDoubleLengthKey(String key) {
		if(key == null || key.length() != 32) return false;
		return true;
	}
	
	public static boolean isValidPinBlockFormatCodeLength(String val) {
		if(val == null || val.length() != 2) return false;
		return true;
	}
	
	public static boolean isNotNull(String val) {
		if(val == null) return false;
		return true;
	}
	
	public static String getDefaultDecimalisation() {
		return new String(DEFAULT_DECIMALISATION);
	}
	
	public static String makeAccountNumberFromPan(String pan) {
		return pan.substring(3, 15);
	}
	
	public static String makePinValidationDataForPan(String pan) {
		return pan.substring(0, 10) + "N" + pan.substring(pan.length()-1);
	}

	public static void main(String[] args) {
		System.out.println("pin val data : " +
	DataValidator.makePinValidationDataForPan("7788990000000005"));

	}

}
