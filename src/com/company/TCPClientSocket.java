package com.company;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class TCPClientSocket extends SocketOverUDP implements Runnable {
    private int seqNumber;
    private int ackNumber;
    private final int timeout = 1000;
    private final Map<TCPSegment, Integer> tcpSegments = new HashMap<>();
    private final int serverPort;

    public TCPClientSocket(double loss, int receiverPort, int serverPort) throws SocketException {
        super(loss, receiverPort);
        this.serverPort = serverPort;
        seqNumber = new Random().nextInt(1000);
    }

    public TCPSegment reliableReceive() throws IOException, ClassNotFoundException {
        long start = System.currentTimeMillis();
        long currentTime;

        while (true) {
            TCPSegment segment = receiveOverUPD();
            if (segment != null) {
                int segmentACK = segment.getAckNumber();
                if (segmentACK > ackNumber) {
                    tcpSegments.entrySet().removeIf(tuple -> tuple.getKey().getSeqNumber() < segmentACK);
                    ackNumber = segmentACK;
                    return segment;
                }
            }
            else {
                currentTime = System.currentTimeMillis() - start;
                for (TCPSegment tcpSegment : tcpSegments.keySet()) {
                    if (currentTime >= timeout) {
                        tcpSegments.put(tcpSegment, timeout);
                        sendOverUDP(tcpSegment, serverPort);
                    }
                }
            }
        }
    }

    public void handshake() throws IOException, ClassNotFoundException {
        TCPSegment SYN = new TCPSegment(
                0,
                seqNumber,
                0,
                false,
                true,
                false);
        sendOverUDP(SYN, serverPort);
        System.out.println("Client send " + SYN);

        TCPSegment SYN_ACK = receiveOverUPD();
        if (SYN_ACK.isSyn() && SYN_ACK.isAck() && SYN_ACK.getAckNumber() == seqNumber + 1) {
            seqNumber = SYN_ACK.getAckNumber();
            ackNumber = SYN_ACK.getSeqNumber() + 1;
            TCPSegment ACK = new TCPSegment(
                    0,
                    seqNumber,
                    ackNumber,
                    true,
                    false,
                    false);
            sendOverUDP(ACK, serverPort);
            System.out.println("Client send " + ACK);
        }
        else {
            throw new SocketException();
        }
    }

    public void closeConnection() throws IOException, ClassNotFoundException {
        TCPSegment FIN = new TCPSegment(
                0,
                seqNumber,
                ackNumber,
                false,
                false,
                true
        );
        sendOverUDP(FIN, serverPort);
        System.out.println("Client send " + FIN);

        TCPSegment ackFIN = receiveOverUPD();
        TCPSegment serverFIN = receiveOverUPD();

        if (ackFIN.isAck() && serverFIN.isFyn()) {
            seqNumber = serverFIN.getAckNumber();
            ackNumber = serverFIN.getSeqNumber() + 1;
            TCPSegment ACK = new TCPSegment(
                    0,
                    seqNumber,
                    ackNumber,
                    true,
                    false,
                    false
            );
            sendOverUDP(ACK, serverPort);
            System.out.println("Client send " + ACK);
        }
        else {
            throw new SocketException();
        }
    }

    @Override
    public void run() {
        try {
            handshake();

            String[] packets = {"first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth"};

            seqNumber += 1;
            ackNumber = 0;
            for (String packet : packets ) {
                TCPSegment segment = new TCPSegment(
                        packet,
                        packet.length(),
                        seqNumber,
                        ackNumber,
                        false,
                        false,
                        false);
                tcpSegments.put(segment, timeout);
                sendOverUDP(segment, serverPort);
                System.out.println("Client sent: " + segment);
                TCPSegment segmentACK = reliableReceive();
                System.out.println("Client received: " + segmentACK);
                seqNumber += packet.length();
                ackNumber = segmentACK.getSeqNumber() + 1;
            }


            System.out.println("=====================================================================================");
            closeConnection();


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
