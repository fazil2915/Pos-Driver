package com.example.pos_driver.Service;

import com.example.pos_driver.Repo.HsmCommand;
import com.example.pos_driver.Utils.Utils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class HsmConnection implements AutoCloseable {

    private final String ip;
    private final int port;
    private final Socket connection;
    private BufferedOutputStream outStream;
    private DataInputStream dis;
    private static final byte[] DEFAULT_HEADER_LENGTH = {0x00, 0x00};

    public HsmConnection(String ip, int port) throws UnknownHostException, IOException {
        this.ip = ip;
        this.port = port;
        this.connection = new Socket(ip, port);
    }

    public void sendCommand(HsmCommand command) throws IOException {
        if (connection.isConnected()) {
            System.out.println("--- Connection received from HSM @ " + ip + ":" + port + " ---");
            outStream = new BufferedOutputStream(connection.getOutputStream());
            byte[] dataToSend = Utils.concatenateByteArrays(DEFAULT_HEADER_LENGTH, command.build());
            outStream.write(send(dataToSend));
            outStream.flush();
        } else {
            System.out.println("No existing connection for HSM!");
        }
    }

    public byte[] getResponse() throws IOException {
        byte[] resp = null;
        if (connection.isConnected()) {
            dis = new DataInputStream(connection.getInputStream());
            int rspLen = dis.readUnsignedShort();
            if (rspLen > 0) {
                resp = new byte[rspLen];
                dis.readFully(resp, 0, rspLen);
            } else {
                resp = null;
            }
            System.out.println("--- RESPONSE received from HSM --- ***" + new String(resp) + "***");
        } else {
            System.out.println("No existing connection for HSM!");
        }
        return resp;
    }

    @Override
    public void close() {
        try {
            if (outStream != null) {
                outStream.close();
            }
            if (dis != null) {
                dis.close();
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] send(byte[] data) {
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

        return result;
    }
}
