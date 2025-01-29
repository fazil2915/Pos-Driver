package com.example.pos_driver.Service;

import com.example.pos_driver.Model.DriverRequest;

import com.example.pos_driver.Model.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.util.XPostilion;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class Iso8583Service {

    @Autowired
    private VitaService vitaService;

    private static final String PACKAGER_FILE_PATH = "src/main/resources/pakeager.xml";  // Specify your pakeager.xml path

    // Method to generate ISO8583 message
    public String createIso8583Message(DriverRequest driverRequest, String pin) throws IOException, XPostilion {

        Terminal terminal = vitaService.findTerminalBySerialNumber(driverRequest.getSl_no()).get();

        Iso8583Post result = new Iso8583Post();

        result.putMsgType(Iso8583Post.MsgType._0200_TRAN_REQ);
        result.putField(Iso8583Post.Bit._002_PAN, driverRequest.getPan());
        result.putField(Iso8583Post.Bit._003_PROCESSING_CODE,"000100");
        result.putField(Iso8583Post.Bit._004_AMOUNT_TRANSACTION,driverRequest.getAmount());
        result.putField(Iso8583Post.Bit._007_TRANSMISSION_DATE_TIME, getIso8583Timestamp());
        result.putField(Iso8583Post.Bit._041_CARD_ACCEPTOR_TERM_ID,terminal.getTerminalId());
        System.out.println("ISO message : "+ result);

        return null;
    }




    public String getIso8583Timestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmmss");
        return now.format(formatter);
    }
}
