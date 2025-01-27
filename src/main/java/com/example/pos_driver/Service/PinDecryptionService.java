package com.example.pos_driver.Service;


import com.example.pos_driver.Repo.PinDecryption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

@Service
public class PinDecryptionService implements PinDecryption {

    @Value("${pin.encryption.key}")
    private String keyHex;


    @Override
    public String pinDecrypting(String pinblock) {
        try {
            // Convert the hex key to a byte array
            byte[] keyData = hexStringToByteArray(keyHex);

            // Convert the hex pinblock to a byte array
            byte[] encryptedData = hexStringToByteArray(pinblock);

            // Initialize the 3DES key
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            SecretKey key = keyFactory.generateSecret(new DESedeKeySpec(keyData));

            // Configure the cipher for decryption
            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key);

            // Perform decryption
            byte[] decryptedData = cipher.doFinal(encryptedData);

            // Convert the decrypted data to a hexadecimal string
            String decryptedDataStr = byteArrayToHexString(decryptedData);

            // Extract and decode the PIN
            char paddingCharacter = 'F'; // Define the padding character
            String decodedPin = decodePinBlock(decryptedDataStr, paddingCharacter);

            System.out.println("Decoded PIN: " + decodedPin);

            // Check if the decoded PIN is valid
            if (decodedPin != null && !decodedPin.isEmpty()) {
                return decodedPin; // Successfully decrypted PIN
            } else {
                return "false"; // Decryption failed
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "false"; // Return "false" in case of an error
        }
    }

    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private String byteArrayToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    private String decodePinBlock(String decryptedDataStr, char paddingCharacter) {
        // Logic to extract and decode the PIN based on the padding character
        String decodedPin = decryptedDataStr.replace(String.valueOf(paddingCharacter), "").substring(2, 6);
        return decodedPin;
    }
}
