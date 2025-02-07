package com.example.pos_driver.Service;

import com.example.pos_driver.Model.DriverRequest;

import com.example.pos_driver.Model.Terminal;
import com.example.pos_driver.Repo.TransactionRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Transform;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class Iso8583Service {

    private static final Logger logger = LoggerFactory.getLogger(Iso8583Service.class);


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
    try{
        logger.info("--- CREATING ISO MESSAGE ---");
        Terminal terminal = vitaService.findTerminalBySerialNumber(driverRequest.getSl_no()).get();

        Iso8583Post result = new Iso8583Post();
        
        if(Objects.equals(driverRequest.getIreq_transaction_type(), "92")){
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
            result.putField(Iso8583Post.Bit._004_AMOUNT_TRANSACTION,"00000000");
        }else{
            result.putMsgType(Iso8583Post.MsgType._0200_TRAN_REQ);
            result.putField(Iso8583Post.Bit._004_AMOUNT_TRANSACTION, driverRequest.getAmount() + "00");
        }

        result.putField(Iso8583Post.Bit._002_PAN, driverRequest.getPan());
        result.putField(Iso8583Post.Bit._003_PROCESSING_CODE, driverRequest.getIreq_transaction_type() + "0000");
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
        byte[] ISOMsg = result.toMsg();
        byte[] isoMessageWithHeader = createIsoMessageWithHeader(ISOMsg);
        byte[] isoMsgWithIcc = processIsoMessageWithIcc(isoMessageWithHeader, driverRequest.getIcc_req_data());
        // String formattedHex = formatHexString(DatatypeConverter.printHexBinary(isoMsgWithIcc));
        // System.out.println("Formatted Hex:\n" + formattedHex);
        logger.info("ISO MESSAGE: {}",formatData(isoMsgWithIcc));
        return isoMsgWithIcc;
    }catch(Exception e){
        logger.error("--- ERROR DURING CREATING ISO MESSAGE ---");
        e.printStackTrace();
        throw new RuntimeException("Error creating ISO message", e);
    }
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
//        System.out.println("RESPONSE ISO: "+response_Result);
        logger.info("RESPONSE : {} ",response_Result);
        if(!transactionRepo.existsByStan(response_Result.getField(Iso8583Post.Bit._011_SYSTEMS_TRACE_AUDIT_NR))){
            return "Received STAN is not in the table  ";
        }

        transactionService.createTransaction(driverRequest,response_Result);
        String responseCode = getResponseMessage(response_Result.getResponseCode());
        String msg_type = response_Result.getMessageType();
        String cardHolderName = response_Result.getPrivField(Iso8583Post.PrivBit._017_CARDHOLDER_INFO);
        String stmt = response_Result.getField(Iso8583Post.Bit._048_ADDITIONAL_DATA);
        String tranID = response_Result.getField(Iso8583Post.Bit._011_SYSTEMS_TRACE_AUDIT_NR);
        String field_54 = response_Result.getField(Iso8583Post.Bit._054_ADDITIONAL_AMOUNTS);

        Map<String, String> resp = new HashMap<>();
        resp.put("response", responseCode);


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


    public String formatData(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        int lines = data.length / 16;

        for (int j = 0; j <= lines; j++) {
            int startLen = sb.length();
            sb.append(String.format("%08X:", pos)).append("  ");

            for (int k = 0; pos + k < data.length && k < 16; k++) {
                sb.append(String.format("%02X ", data[pos + k]));
                if (k == 7) sb.append(" ");
            }

            while (sb.length() - startLen < 61) {
                sb.append(" ");
            }

            for (int k = 0; pos + k < data.length && k < 16; k++) {
                char c = (char) data[pos + k];
                sb.append((c < 32 || c > 126) ? '.' : c);
            }

            sb.append(System.lineSeparator());
            pos += 16;
        }
        return sb.toString();
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



    public  String getResponseMessage(String responseCode) {
        String message;
        switch (responseCode) {

            case "00":

                message = "Transaction Successfull";

                break;

            case "01":

                message = "Refer to card issuer";

                break;

            case "02":

                message = "Refer to card issuer, special condition";

                break;

            case "03":

                message = "Invalid merchant";

                break;

            case "04":

                message = "Pick-up card";

                break;

            case "05":

                message = "Do not honor";

                break;

            case "06":

                message = "Error";

                break;

            case "07":

                message = "Pick-up card, special condition";

                break;

            case "08":

                message = "Honor with identification";

                break;

            case "09":

                message = "Request in progress";

                break;

            case "10":

                message = "Approved, partial";

                break;

            case "11":

                message = "Approved, VIP";

                break;

            case "12":

                message = "Invalid transaction";

                break;

            case "13":

                message = "Invalid amount";

                break;

            case "14":

                message = "Invalid card number";

                break;

            case "15":

                message = "No such issuer";

                break;

            case "16":

                message = "Approved, update track 3";

                break;

            case "17":

                message = "Customer cancellation";

                break;

            case "18":

                message = "Customer dispute";

                break;

            case "19":

                message = "Re-enter transaction";

                break;

            case "20":

                message = "Invalid response";

                break;

            case "21":

                message = "No action taken";

                break;

            case "22":

                message = "Suspected malfunction";

                break;

            case "23":

                message = "Unacceptable transaction fee";

                break;

            case "24":

                message = "File update not supported";

                break;

            case "25":

                message = "Unable to locate record";

                break;

            case "26":

                message = "Duplicate record";

                break;

            case "27":

                message = "File update field edit error";

                break;

            case "28":

                message = "File update file locked";

                break;

            case "29":

                message = "File update failed";

                break;

            case "30":

                message = "Format error";

                break;

            case "31":

                message = "Bank not supported";

                break;

            case "32":

                message = "Completed partially";

                break;

            case "33":

                message = "Expired card, pick-up";

                break;

            case "34":

                message = "Suspected fraud, pick-up";

                break;

            case "35":

                message = "Contact acquirer, pick-up";

                break;

            case "36":

                message = "Restricted card, pick-up";

                break;

            case "37":

                message = "Call acquirer security, pick-up";

                break;

            case "38":

                message = "PIN tries exceeded, pick-up";

                break;

            case "39":

                message = "No credit account";

                break;

            case "40":

                message = "Function not supported";

                break;

            case "41":

                message = "Lost card, pick-up";

                break;

            case "42":

                message = "No universal account";

                break;

            case "43":

                message = "Stolen card, pick-up";

                break;

            case "44":

                message = "No investment account";

                break;

            case "45":

                message = "Account closed";

                break;

            case "46":

                message = "Identification required";

                break;

            case "47":

                message = "Identification cross-check required";

                break;

            case "48":

                message = "No customer record";

                break;

            case "49":

            case "50":

                message = "Reserved for future Realtime use";

                break;

            case "51":

                message = "Not sufficient funds";

                break;

            case "52":

                message = "No check account";

                break;

            case "53":

                message = "No savings account";

                break;

            case "54":

                message = "Expired card";

                break;

            case "55":

                message = "Incorrect PIN";

                break;

            case "56":

                message = "No card record";

                break;

            case "57":

                message = "Transaction not permitted to cardholder";

                break;

            case "58":

                message = "Transaction not permitted on terminal";

                break;

            case "59":

                message = "Suspected fraud";

                break;

            case "60":

                message = "Contact acquirer";

                break;

            case "61":

                message = "Exceeds withdrawal limit";

                break;

            case "62":

                message = "Restricted card";

                break;

            case "63":

                message = "Security violation";

                break;

            case "64":

                message = "Original amount incorrect";

                break;

            case "65":

                message = "Exceeds withdrawal frequency";

                break;

            case "66":

                message = "Call acquirer security";

                break;

            case "67":

                message = "Hard capture";

                break;

            case "68":

                message = "Response received too late";

                break;

            case "69":

                message = "Advice received too late";

                break;

            case "70":

            case "71":

            case "72":

            case "73":

            case "74":

                message = "Reserved for future Realtime use";

                break;

            case "75":

                message = "PIN tries exceeded";

                break;

            case "76":

                message = "Reserved for future Realtime use";

                break;

            case "77":

                message = "Intervene, bank approval required";

                break;

            case "78":

                message = "Intervene, bank approval required for partial amount";

                break;

            case "79":

            case "80":

            case "81":

            case "82":

            case "83":

            case "84":

            case "85":

            case "86":

            case "87":

            case "88":

            case "89":

                message = "Reserved for client-specific use (declined)";

                break;

            case "90":

                message = "Cut-off in progress";

                break;

            case "91":

                message = "Issuer or switch inoperative";

                break;

            case "92":

                message = "Routing error";

                break;

            case "93":

                message = "Violation of law";

                break;

            case "94":

                message = "Duplicate transaction";

                break;

            case "95":

                message = "Reconcile error";

                break;

            case "96":

                message = "System malfunction";

                break;

            case "97":

                message = "Reserved for future Realtime use";

                break;

            case "98":

                message = "Exceeds cash limit";

                break;

            case "99":

                message = "Reserved for future Realtime use";

                break;

            case "A1":

                message = "ATC not incremented";

                break;

            case "A2":

                message = "ATC limit exceeded";

                break;

            case "A3":

                message = "ATC configuration error";

                break;

            case "A4":

                message = "CVR check failure";

                break;

            case "A5":

                message = "CVR configuration error";

                break;

            case "A6":

                message = "TVR check failure";

                break;

            case "A7":

                message = "TVR configuration error";

                break;

            case "C0":

                message = "Unacceptable PIN";

                break;

            case "C1":

                message = "PIN Change failed";

                break;

            case "C2":

                message = "PIN Unblock failed";

                break;

            case "D1":

                message = "MAC Error";

                break;

            case "E1":

                message = "Prepay error";

                break;

            default:

                message = "Transaction Failed";

                break;

        }



        return message;

    }










}


