package com.company;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.Random;

public class SocketOverUDP {
    private final Random random = new Random();
    private final double loss;
    private DatagramSocket socket;

    public SocketOverUDP(double loss, int port) throws SocketException {
        this.loss = loss;
        try {
            socket = new DatagramSocket(port, InetAddress.getLocalHost());
        }
        catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        socket.setSoTimeout(1000);

    }

    public void sendOverUDP(TCPSegment segment, int port) {
        if (random.nextDouble() > loss | segment.isSyn() | segment.isAck() | segment.isFyn()) {
            byte[] bytes = segment.serializeSegment();
            try {
                DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getLocalHost(), port);
                socket.send(datagramPacket);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Segment " + segment + " was lost");
        }
    }

    public TCPSegment receiveOverUPD() throws IOException, ClassNotFoundException {
        byte[] buf = new byte[1024];
        TCPSegment segment;
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(datagramPacket);
        }
        catch (SocketTimeoutException e) {
            return null;
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buf);
        ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(byteArrayInputStream));
        segment = (TCPSegment) objectInputStream.readObject();
        objectInputStream.close();

        return segment;
    }
}
