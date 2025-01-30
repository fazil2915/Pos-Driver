package com.example.pos_driver.Service;


import com.example.pos_driver.Controller.DriverController;
import com.example.pos_driver.Model.DriverRequest;
import com.example.pos_driver.Model.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

@Service
public class SwitchService {
    private static final Logger logger = LoggerFactory.getLogger(SwitchService.class);

    @Autowired
    private VitaService vitaService;


    public byte[] sendIsoMsgToSwitch(DriverRequest driver, byte[] IsoMsg) {
        try {
            Socket socket = null;
            BufferedOutputStream outStream = null;
            DataInputStream dis = null;
            Terminal terminal = vitaService.findTerminalBySerialNumber(driver.getSl_no()).get();
            String host = terminal.getSwitchs().getIp();
            String port = terminal.getSwitchs().getPort();
            socket = new Socket(host, Integer.parseInt(port));
            if (socket.isConnected()) {
                logger.info("Switch Connected to {}:{}", host, port);
                logger.debug("message socket {}", socket);
                outStream = new BufferedOutputStream(socket.getOutputStream());
                outStream.write(send(IsoMsg));
                System.out.println("message sent is message is : \n" + formatData(IsoMsg));
                outStream.flush();
            }
            int responseLength;
            byte[] response = new byte[0];
            if (socket.isConnected()) {
                dis = new DataInputStream(socket.getInputStream());
                responseLength = dis.readUnsignedShort();
                logger.debug("response Length " + responseLength);
                if (responseLength > 0) {
                    logger.info("--- RESPONSE recieved ---");
                    response = new byte[responseLength];
                    dis.readFully(response, 0, responseLength);
                    System.out.println("response is : \n" + formatData(response));
                } else {
                    logger.info("no response");
                    response = null;
                }
            } else {
                logger.info("Socket not connected");
                return null;
            }
            byte[] responseWithoutHeader = new byte[response.length - 5];
            System.arraycopy(response, 5, responseWithoutHeader, 0, responseWithoutHeader.length);
            System.out.println("response is : \n" + formatData(responseWithoutHeader));
            return responseWithoutHeader;
        } catch (IOException e) {
            throw new RuntimeException("Error In switch connection: " + e.getMessage());
        }
    }

    public String formatData(byte[] data) {
        StringBuffer sb = new StringBuffer();
        int pos = 0;
        int lines = data.length / 16;

        for (int j = 0; j <= lines; j++) {
            int start_len = sb.length();
            String line_no = "00000000" + Integer.toHexString(pos);
            int len = line_no.length();
            line_no = line_no.substring(len - 8, len - 4) + ":" + line_no.substring(len - 4, len);

            sb.append(line_no);
            sb.append("  ");

            for (int k = 0; pos + k < data.length && k < 16; k++) {
                int i = Byte.toUnsignedInt(data[pos + k]);
                if (i < 16) {
                    sb.append("0");
                }

                sb.append(Integer.toHexString(i).toUpperCase());
                if (k == 7) {
                    sb.append("  ");
                } else {
                    sb.append(" ");
                }
            }

            int line_len = sb.length() - start_len;

            int no_spaces = 61 - line_len;

            for (int k = 0; k < no_spaces; k++) {
                sb.append(" ");
            }

            for (int k = 0; pos + k < data.length && k < 16; k++) {
                if (k == 8) {
                    sb.append(" ");
                }

                int i = Byte.toUnsignedInt(data[pos + k]);
                if (i < 32 || i > 126) {
                    sb.append(".");
                } else {
                    char c = (char) i;
                    sb.append(c);
                }
            }

            if (j != lines) {
                sb.append(System.lineSeparator());
            }

            pos += 16;
        }

        return sb.toString();
    }

    public byte[] send(byte[] data) {
        logger.debug("Sending data of length " + data.length);
        byte[] result = new byte[data.length + 2];
        if (data.length > 0) {
            System.arraycopy(data, 0, result, 2, data.length);
        }
        int val = data.length;
        int pos = 2 - 1;
        while (val > 0) {
            int mod = val % 256;
            result[pos] = (byte) (mod & 0x000000FF);
            pos--;
            val /= 256;
        }
        int debugDataLen = result.length < 8 ? result.length : 8;
        byte[] debugData = new byte[debugDataLen];
        System.arraycopy(result, 0, debugData, 0, debugData.length);
        return result;
    }
}
