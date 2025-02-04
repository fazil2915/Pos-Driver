package com.example.pos_driver.Service;

import com.example.pos_driver.Model.DriverRequest;

import com.example.pos_driver.Model.Terminal;
import com.example.pos_driver.Model.Transaction;
import com.example.pos_driver.Repo.TransactionRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class Iso8583Service {


    @Autowired
    private IccCardService iccCardService;
    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private VitaService vitaService;

    private static final String PACKAGER_FILE_PATH = "src/main/resources/pakeager.xml";  // Specify your pakeager.xml path

    // Method to generate ISO8583 message
    public byte[] createIso8583Message(DriverRequest driverRequest, String pin) throws IOException, XPostilion {


        Terminal terminal = vitaService.findTerminalBySerialNumber(driverRequest.getSl_no()).get();

        Iso8583Post result = new Iso8583Post();
        result.putMsgType(Iso8583Post.MsgType._0200_TRAN_REQ);

        if(Objects.equals(driverRequest.getIreq_transaction_type(), "92")){
            System.out.println("Success pin chnage");
            result.putMsgType(Iso8583Post.MsgType._0600_ADMIN_REQ);
            String newDecodedPin = driverRequest.getDecodedNewPin();
            if (newDecodedPin.length() < 48) {
                StringBuilder binaryBuilder = new StringBuilder();
                while (binaryBuilder.length() + newDecodedPin.length() < 96) {
                    binaryBuilder.append('0');
                }
                binaryBuilder.append(newDecodedPin);
                newDecodedPin = binaryBuilder.toString();
            }
            result.putField(Iso8583Post.Bit._053_SECURITY_INFO, Transform.fromHexToBin(newDecodedPin));
        }
        result.putField(Iso8583Post.Bit._002_PAN, driverRequest.getPan());
        result.putField(Iso8583Post.Bit._003_PROCESSING_CODE, driverRequest.getIreq_transaction_type() + "0000");
        result.putField(Iso8583Post.Bit._004_AMOUNT_TRANSACTION, driverRequest.getAmount() + "00");
        result.putField(Iso8583Post.Bit._007_TRANSMISSION_DATE_TIME, getTransmissionDateTime());


        result.putField(Iso8583Post.Bit._012_TIME_LOCAL, getLocalTransactionTime());
        result.putField(Iso8583Post.Bit._013_DATE_LOCAL, getLocalTransactionDate());
        result.putField(Iso8583Post.Bit._023_CARD_SEQ_NR, "000");
        result.putField(Iso8583Post.Bit._035_TRACK_2_DATA, driverRequest.getTrack2());
//        result.putField(Iso8583Post.Bit._037_RETRIEVAL_REF_NR, "321420489260");//Naigurta
//        result.putField(Iso8583Post.Bit._018_MERCHANT_TYPE, terminal.getMerchant().getMerchantType());
        result.putField(Iso8583Post.Bit._041_CARD_ACCEPTOR_TERM_ID, terminal.getTerminalId());
        result.putField(Iso8583Post.Bit._042_CARD_ACCEPTOR_ID_CODE, terminal.getMerchant().getMerchantId());
//        result.putField(Iso8583Post.Bit._048_ADDITIONAL_DATA, "0010218923");//Naiguata
        result.putField(Iso8583Post.Bit._049_CURRENCY_CODE_TRAN, "928");
        result.putField(Iso8583Post.Bit._052_PIN_DATA, Transform.fromHexToBin(pin));
        result.putField(Iso8583Post.Bit._123_POS_DATA_CODE, "310101511336101");
        result.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200070000744892610802163636");//Naiguata
        result.putPrivField(Iso8583Post.PrivBit._009_ADDITIONAL_NODE_DATA, "0014Q31003226TRANRED140");//Naiguata
        result.putPrivField(Iso8583Post.PrivBit._010_CVV_2, "000");//Naiguata
        result.putPrivField(Iso8583Post.PrivBit._025_ICC_DATA, iccCardService.getTempIcc(driverRequest.getIcc_req_data()));

        String stan = transactionService.generateStan();
        result.putField(Iso8583Post.Bit._011_SYSTEMS_TRACE_AUDIT_NR, stan);
        driverRequest.setStan(stan);
        transactionService.createTransaction(driverRequest,result);

        System.out.println("ISO message>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> : " + result);
        byte[] ISOMsg = result.toMsg();
        byte[] isoMessageWithHeader = createIsoMessageWithHeader(ISOMsg);
        byte[] isoMsgWithIcc = processIsoMessageWithIcc(isoMessageWithHeader, driverRequest.getIcc_req_data());
        String formattedHex = formatHexString(DatatypeConverter.printHexBinary(isoMsgWithIcc));
        System.out.println("Formatted Hex:\n" + formattedHex);
        return isoMsgWithIcc;
    }

    public String getTransmissionDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmmss");
        return LocalDateTime.now().format(formatter);
    }

    // Generate Local Time (hhmmss)
    public String getLocalTransactionTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
        return LocalDateTime.now().format(formatter);
    }

    // Generate Local Date (MMDD)
    public String getLocalTransactionDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd");
        return LocalDateTime.now().format(formatter);
    }

    // Generate System Trace Audit Number (STAN) - 6-digit unique ID
//    public String generateSTAN() {
//        System.out.println("Inside");
//        String stan;
//        Transaction newTransaction = new Transaction();
//        if (transactionRepo.findAll().isEmpty()) {
//            System.out.println("NO STAN CREATED ");
//            stan = "000001"; // Initial value
//            newTransaction.setStan(stan);
//            newTransaction.setMsg_type("200");
//            transactionRepo.save(newTransaction);
//        } else {
//            Transaction transaction = transactionRepo.findTopByOrderByIdDesc();
//            int currentStan = Integer.parseInt(transaction.getStan());
//            stan = String.format("%06d", currentStan + 1);
//            newTransaction.setStan(stan);
//            newTransaction.setMsg_type("200");
//            transactionRepo.save(newTransaction);
//        }
//        return stan;
//    }


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


        System.out.println("hexStringToByteArray : " + iccData);

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

    public String setResponse(byte[] result, DriverRequest driverRequest) throws XPostilion, JsonProcessingException {
        Iso8583Post response_Result = new Iso8583Post();

        response_Result.fromMsg(result);
        System.out.println("RESPONSE ISO: "+response_Result);
        if(!transactionRepo.existsByStan(response_Result.getField(Iso8583Post.Bit._011_SYSTEMS_TRACE_AUDIT_NR))){
            return "Received STAN is not in the table  ";
        }

        transactionService.createTransaction(driverRequest,response_Result);
        String responsecode = response_Result.getResponseCode();
        String msg_type = response_Result.getMessageType();
        String cardHolderName = response_Result.getPrivField(Iso8583Post.PrivBit._017_CARDHOLDER_INFO);
        String stmt = response_Result.getField(Iso8583Post.Bit._048_ADDITIONAL_DATA);
        String tranID = response_Result.getField(Iso8583Post.Bit._011_SYSTEMS_TRACE_AUDIT_NR);
        String field_54 = response_Result.getField(Iso8583Post.Bit._054_ADDITIONAL_AMOUNTS);
        System.out.println("msg_type: " + msg_type);
        System.out.println("Response Code: " + responsecode);
        System.out.println("Stan: " + tranID);
        System.out.println("Stmt: " + stmt);
        System.out.println("Field 54: " + field_54);
        System.out.println("Card holder name: " +cardHolderName);

        Map<String, String> resp = new HashMap<>();
        resp.put("response", responsecode);


        String jsonString = null;
        if(cardHolderName != null) {
            resp.put("card_holder_name", cardHolderName);
        }
        if(stmt!=null) {
            resp.put("statement", stmt);
        }
        if(tranID!=null) {
            resp.put("tran_id", tranID);
        }

        if(field_54 != null && field_54.length() == 40) {
            System.out.println("field_54 :: " + field_54);
            try {

                String part1 = field_54.substring(0, 20);
                String part2 = field_54.substring(20, 40);
                String ledgerBalance = part1.substring(8, 20);
                String availableBalance = part2.substring(8, 20);

                String formattedLedgerBalance = formatBalance(ledgerBalance);
                String formattedAvailableBalance = formatBalance(availableBalance);

                resp.put("ledger_balance", formattedLedgerBalance);
                resp.put("available_balance", formattedAvailableBalance);

                System.out.println("Ledger Balance: " + formattedLedgerBalance);
                System.out.println("Available Balance: " + formattedAvailableBalance);


            }  catch (Exception e) {
                throw  new RuntimeException("Error occurred: " + e.getMessage());
            }


        }
        ObjectMapper objectMapper = new ObjectMapper();
        jsonString = objectMapper.writeValueAsString(resp);
        System.out.println("Response : " + jsonString);

        return jsonString;
    }

    private  String formatBalance(String balance) {
        double value = Double.parseDouble(balance.replaceFirst("^0+(?!$)", "")) / 100.0;
        return String.format("%.2f", value);
    }




    public void createIso8583ErrorMessage(DriverRequest driverRequest) throws IOException, XPostilion {
        Terminal terminal = vitaService.findTerminalBySerialNumber(driverRequest.getSl_no()).get();

        Iso8583Post result = new Iso8583Post();
        result.putMsgType(Iso8583Post.MsgType._0210_TRAN_REQ_RSP);
        result.putField(Iso8583Post.Bit._002_PAN, driverRequest.getPan());
        result.putField(Iso8583Post.Bit._003_PROCESSING_CODE, driverRequest.getIreq_transaction_type() + "0000");
        result.putField(Iso8583Post.Bit._004_AMOUNT_TRANSACTION, driverRequest.getAmount() + "00");
        result.putField(Iso8583Post.Bit._007_TRANSMISSION_DATE_TIME, getTransmissionDateTime());
        result.putField(Iso8583Post.Bit._039_RSP_CODE,"91");
        result.putField(Iso8583Post.Bit._012_TIME_LOCAL, getLocalTransactionTime());
        result.putField(Iso8583Post.Bit._013_DATE_LOCAL, getLocalTransactionDate());
        result.putField(Iso8583Post.Bit._023_CARD_SEQ_NR, "000");
        result.putField(Iso8583Post.Bit._035_TRACK_2_DATA, driverRequest.getTrack2());
//        result.putField(Iso8583Post.Bit._037_RETRIEVAL_REF_NR, "321420489260");//Naigurta
//        result.putField(Iso8583Post.Bit._018_MERCHANT_TYPE, terminal.getMerchant().getMerchantType());
        result.putField(Iso8583Post.Bit._041_CARD_ACCEPTOR_TERM_ID, terminal.getTerminalId());
        result.putField(Iso8583Post.Bit._042_CARD_ACCEPTOR_ID_CODE, terminal.getMerchant().getMerchantId());
//        result.putField(Iso8583Post.Bit._048_ADDITIONAL_DATA, "0010218923");//Naiguata
        result.putField(Iso8583Post.Bit._049_CURRENCY_CODE_TRAN, "928");
        result.putField(Iso8583Post.Bit._052_PIN_DATA, Transform.fromHexToBin(driverRequest.getHsmPin()));
        result.putField(Iso8583Post.Bit._123_POS_DATA_CODE, "310101511336101");
        result.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200070000744892610802163636");//Naiguata
        result.putPrivField(Iso8583Post.PrivBit._009_ADDITIONAL_NODE_DATA, "0014Q31003226TRANRED140");//Naiguata
        result.putPrivField(Iso8583Post.PrivBit._010_CVV_2, "000");//Naiguata
        result.putPrivField(Iso8583Post.PrivBit._025_ICC_DATA, iccCardService.getTempIcc(driverRequest.getIcc_req_data()));
        result.putField(Iso8583Post.Bit._011_SYSTEMS_TRACE_AUDIT_NR, driverRequest.getStan());
        transactionService.createTransaction(driverRequest,result);
    }










}


