package com.example.pos_driver.Service;


import com.example.pos_driver.Model.DriverRequest;
import com.example.pos_driver.Model.Terminal;

import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.util.XPostilion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.xml.bind.DatatypeConverter;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import java.util.Optional;

@Service
public class SwitchService {


    @Autowired
    private VitaService vitaService;

    @Autowired
    private Iso8583Service iso8583Service;


    private static final Logger logger = LoggerFactory.getLogger(SwitchService.class);

    public byte[] connectToSwitch(byte[] isoMsg, DriverRequest driverRequest) throws XPostilion, IOException {
        Optional<Terminal> terminalOptional = vitaService.findTerminalBySerialNumber(driverRequest.getSl_no());
        if (!terminalOptional.isPresent() || terminalOptional.get().getSwitchs() == null) {
            logger.error("Switch details not found for terminal: {}", driverRequest.getSl_no());
            return null;
        }

        Terminal terminal = terminalOptional.get();
        String host = terminal.getSwitchs().getIp();
        int port;

        try {
            port = Integer.parseInt(terminal.getSwitchs().getPort());
        } catch (NumberFormatException e) {
            logger.error("Invalid port number for switch: {}", terminal.getSwitchs().getPort());
            return null;
        }

        try (Socket socket = new Socket(host, port);
             BufferedOutputStream outStream = new BufferedOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            logger.info("Connected to switch at {}:{}", host, port);
            outStream.write(send(isoMsg));
            outStream.flush();
            logger.info("Sent ISO message to switch");
            int responseLength = dis.readUnsignedShort();
            byte[] responseBytes = new byte[responseLength];
            dis.readFully(responseBytes);
            byte[] responseWithoutHeader = new byte[responseBytes.length - 5];
            System.arraycopy(responseBytes, 5, responseWithoutHeader, 0, responseWithoutHeader.length);
            logger.info("response is : \n" + formatData(responseWithoutHeader));
            return responseWithoutHeader;

        } catch (IOException e) {
           iso8583Service.createIso8583ErrorMessage(driverRequest);
            logger.error("Switch connection failed: ", e);
            return null;  // or handle failure as appropriate
        }
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

    public byte[] send(byte[] data) {
        logger.debug("Sending data of length {}", data.length);
        byte[] result = new byte[data.length + 2];
        System.arraycopy(data, 0, result, 2, data.length);
        result[0] = (byte) ((data.length >> 8) & 0xFF);
        result[1] = (byte) (data.length & 0xFF);
        return result;
    }

}
