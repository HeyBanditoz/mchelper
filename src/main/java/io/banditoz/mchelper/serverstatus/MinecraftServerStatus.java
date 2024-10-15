package io.banditoz.mchelper.serverstatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Modified from https://gist.github.com/zh32/7190955 to support Jackson instead.
 *
 * @author zh32 <zh32 at zh32.de>
 */
public class MinecraftServerStatus {
    private final InetSocketAddress host;
    private final int timeout;
    private final ObjectMapper om;

    public MinecraftServerStatus(InetSocketAddress host, int timeout, ObjectMapper om) {
        this.host = host;
        this.timeout = timeout;
        this.om = om;
    }

    public InetSocketAddress getAddress() {
        return this.host;
    }

    int getTimeout() {
        return this.timeout;
    }

    public int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128) break;
        }
        return i;
    }

    public void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    /**
     * Fetch information about this server.
     *
     * @return A {@link StatusResponse} object containing information about this server.
     * @throws IOException If there was a problem connecting to the server or parsing the data.
     */
    public StatusResponse fetchData() throws IOException {
        Socket socket = null;
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        InputStream inputStream  = null;
        try {
            socket = new Socket();

            socket.setSoTimeout(this.timeout);
            socket.connect(host, timeout);

            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            inputStream = ByteStreams.limit(socket.getInputStream(), 512_000);

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream handshake = new DataOutputStream(b);
            handshake.writeByte(0x00); //packet id for handshake
            writeVarInt(handshake, 4); //protocol version
            writeVarInt(handshake, this.host.getHostString().length()); //host length
            handshake.writeBytes(this.host.getHostString()); //host string
            handshake.writeShort(host.getPort()); //port
            writeVarInt(handshake, 1); //state (1 for handshake)

            writeVarInt(dataOutputStream, b.size()); //prepend size
            dataOutputStream.write(b.toByteArray()); //write handshake packet


            dataOutputStream.writeByte(0x01); //size is only 1
            dataOutputStream.writeByte(0x00); //packet id for ping
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            int size = readVarInt(dataInputStream); //size of packet
            int id = readVarInt(dataInputStream); //packet id

            if (id == -1) {
                throw new IOException("Premature end of stream.");
            }

            if (id != 0x00) { //we want a status response
                throw new IOException("Invalid packetID (got " + id + ")");
            }
            int length = readVarInt(dataInputStream); //length of json string

            if (length == -1) {
                throw new IOException("Premature end of stream.");
            }

            if (length == 0) {
                throw new IOException("Invalid string length.");
            }

            byte[] in = new byte[length];
            dataInputStream.readFully(in);  //read json string
            String json = new String(in);

            long now = System.currentTimeMillis();
            dataOutputStream.writeByte(0x09); //size of packet
            dataOutputStream.writeByte(0x01); //0x01 for ping
            dataOutputStream.writeLong(now); //time!?

            readVarInt(dataInputStream);
            id = readVarInt(dataInputStream);
            if (id == -1) {
                throw new IOException("Premature end of stream.");
            }

            if (id != 0x01) {
                throw new IOException("Invalid packetID (got " + id + ")");
            }
            long pingtime = dataInputStream.readLong(); //read response

            StatusResponse response = om.readValue(json, StatusResponse.class);
            response.setTime((int) (now - pingtime));

            return response;
        } finally {
            if (socket != null) {
                socket.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
