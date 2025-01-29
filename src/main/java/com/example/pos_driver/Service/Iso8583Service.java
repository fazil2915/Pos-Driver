package com.example.pos_driver.Service;

import com.example.pos_driver.Model.DriverRequest;

import com.example.pos_driver.Model.Terminal;
import com.example.pos_driver.Model.Transaction;
import com.example.pos_driver.Repo.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Transform;

import javax.xml.bind.DatatypeConverter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
public class Iso8583Service {



    @Autowired
    private  IccCardService iccCardService;
    @Autowired
    private TransactionRepo transactionRepo;
    @Autowired
    private VitaService vitaService;

    private static final String PACKAGER_FILE_PATH = "src/main/resources/pakeager.xml";  // Specify your pakeager.xml path

    // Method to generate ISO8583 message
    public byte[] createIso8583Message(DriverRequest driverRequest, String pin) throws IOException, XPostilion {




        Terminal terminal = vitaService.findTerminalBySerialNumber(driverRequest.getSl_no()).get();

        Iso8583Post result = new Iso8583Post();

        result.putMsgType(Iso8583Post.MsgType._0200_TRAN_REQ);
        result.putField(Iso8583Post.Bit._002_PAN, driverRequest.getPan());
        result.putField(Iso8583Post.Bit._003_PROCESSING_CODE,driverRequest.getIreq_transaction_type()+"0000");
        result.putField(Iso8583Post.Bit._004_AMOUNT_TRANSACTION,driverRequest.getAmount());
        result.putField(Iso8583Post.Bit._007_TRANSMISSION_DATE_TIME, getTransmissionDateTime());
        result.putField(Iso8583Post.Bit._011_SYSTEMS_TRACE_AUDIT_NR,generateSTAN());
        result.putField(Iso8583Post.Bit._012_TIME_LOCAL,getLocalTransactionTime());
        result.putField(Iso8583Post.Bit._013_DATE_LOCAL, getLocalTransactionDate());
        result.putField(Iso8583Post.Bit._023_CARD_SEQ_NR,"000");
        result.putField(Iso8583Post.Bit._035_TRACK_2_DATA,driverRequest.getTrack2());
//        result.putField(Iso8583Post.Bit._037_RETRIEVAL_REF_NR, "321420489260");//Naigurta
//        result.putField(Iso8583Post.Bit._018_MERCHANT_TYPE, terminal.getMerchant().getMerchantType());
        result.putField(Iso8583Post.Bit._041_CARD_ACCEPTOR_TERM_ID,terminal.getTerminalId());
        result.putField(Iso8583Post.Bit._042_CARD_ACCEPTOR_ID_CODE, terminal.getMerchant().getMerchantId());
//        result.putField(Iso8583Post.Bit._048_ADDITIONAL_DATA, "0010218923");//Naiguata
        result.putField(Iso8583Post.Bit._049_CURRENCY_CODE_TRAN, "928");
        result.putField(Iso8583Post.Bit._052_PIN_DATA, Transform.fromHexToBin(pin));
        result.putField(Iso8583Post.Bit._123_POS_DATA_CODE, "310101511336101");
        result.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200070000744892610802163636");//Naiguata
        result.putPrivField(Iso8583Post.PrivBit._009_ADDITIONAL_NODE_DATA, "0014Q31003226TRANRED140");//Naiguata
        result.putPrivField(Iso8583Post.PrivBit._010_CVV_2, "000");//Naiguata

        result.putPrivField(Iso8583Post.PrivBit._025_ICC_DATA,iccCardService.getTempIcc(driverRequest.getIcc_req_data()));
        System.out.println("ISO message : "+ result);
        byte [] ISOMsg = result.toMsg();

        String hexString = DatatypeConverter.printHexBinary(ISOMsg);
        System.out.println("ISO Hex message: " + hexString);

        Iso8583Post parsedMessage = new Iso8583Post();

        // Convert byte array back to an ISO8583 message object

        byte[] isoMessageWithHeader = createIsoMessageWithHeader(ISOMsg);
//        System.out.println("isoMessageWithHeader : "+  DatatypeConverter.printHexBinary(isoMessageWithHeader));
        byte[] isoMsgWithIcc = processIsoMessageWithIcc(isoMessageWithHeader, driverRequest.getIcc_req_data());
        System.out.println("isoMsgWithIcc: "+ DatatypeConverter.printHexBinary(isoMsgWithIcc));



        String formattedHex = formatHexString(DatatypeConverter.printHexBinary(isoMsgWithIcc));
        System.out.println("Formatted Hex:\n" + formattedHex);

        // Convert back to byte array
        byte[] byteArray = DatatypeConverter.parseHexBinary(hexString);

        System.out.println("Formatted Byte:\n" + DatatypeConverter.printHexBinary(byteArray));


        return ISOMsg;
    }




    public  String getTransmissionDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmmss");
        return LocalDateTime.now().format(formatter);
    }

    // Generate Local Time (hhmmss)
    public  String getLocalTransactionTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
        return LocalDateTime.now().format(formatter);
    }

    // Generate Local Date (MMDD)
    public  String getLocalTransactionDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd");
        return LocalDateTime.now().format(formatter);
    }

    // Generate System Trace Audit Number (STAN) - 6-digit unique ID
    public String generateSTAN() {
        System.out.println("Inside");
        String stan;
        Transaction newTransaction = new Transaction();
        if (transactionRepo.findAll().isEmpty()) {
            System.out.println("NO STAN CREATED ");
            stan = "000001"; // Initial value
            newTransaction.setStan(stan);
            newTransaction.setMsg_type("200");
            transactionRepo.save(newTransaction);
        } else {
            Transaction transaction = transactionRepo.findTopByOrderByIdDesc();
            int currentStan = Integer.parseInt(transaction.getStan());
            stan =  String.format("%06d", currentStan + 1);
            newTransaction.setStan(stan);
            newTransaction.setMsg_type("200");
            transactionRepo.save(newTransaction);
        }
        return stan;
    }



    public byte[] createIsoMessageWithHeader(byte[] byteMsg) {
        // Define the header bytes
        byte[] header = new byte[]{0x60, 0x00, 0x30, 0x00, 0x01};

        // Check if byteMsg is null or empty
        if (byteMsg == null || byteMsg.length == 0) {
            throw new IllegalArgumentException("Byte message cannot be null or empty");
        }

        // Create a new array to hold the header + message
        byte[] sendMsgWithHeader = new byte[header.length + byteMsg.length];

        // Copy header and message into the new array
        System.arraycopy(header, 0, sendMsgWithHeader, 0, header.length);
        System.arraycopy(byteMsg, 0, sendMsgWithHeader, header.length, byteMsg.length);

        return sendMsgWithHeader;
    }



    public byte[] processIsoMessageWithIcc(byte[] isoMessageWithHeader, String iccDataStr) {
        if (isoMessageWithHeader == null || iccDataStr == null) {
            throw new IllegalArgumentException("Input data cannot be null");
        }
        byte[] iccData = hexStringToByteArray(iccDataStr);


        System.out.println("hexStringToByteArray : "+iccData);

        int lengthWithoutIcc = isoMessageWithHeader.length - (iccDataStr.length() / 2);

        // Extract message without ICC data
        byte[] isoMessageWithoutIcc = new byte[lengthWithoutIcc];
        System.arraycopy(isoMessageWithHeader, 0, isoMessageWithoutIcc, 0, lengthWithoutIcc);

        // Prepare final message with ICC data
        byte[] isoMessageWithIcc = new byte[isoMessageWithoutIcc.length + iccData.length];
        System.arraycopy(isoMessageWithoutIcc, 0, isoMessageWithIcc, 0, isoMessageWithoutIcc.length);
        System.arraycopy(iccData, 0, isoMessageWithIcc, isoMessageWithoutIcc.length, iccData.length);

        return isoMessageWithIcc;
    }

    // Convert hex string to byte array
    private byte[] hexStringToByteArray(String data) {
        int len = data.length();
        byte[] result = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(data.charAt(i), 16) << 4)
                    + Character.digit(data.charAt(i + 1), 16));
        }

        return result;
    }


    public static String formatHexString(String hex) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            formatted.append(hex, i, i + 2).append(" ");
        }
        return formatted.toString().trim();
    }

}
