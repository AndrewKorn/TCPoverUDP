package com.company;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;


public class TCPServerSocket extends SocketOverUDP implements Runnable {
    private int seqNumber;
    private int ackNumber;
    private final List<TCPSegment> tcpSegments = new ArrayList<>();
    private final int clientPort;

    public TCPServerSocket(double loss, int serverPort, int clientPort) throws SocketException {
        super(loss, serverPort);
        this.clientPort = clientPort;
    }

    public TCPSegment reliableReceive() throws IOException, ClassNotFoundException {
        while (true) {
            TCPSegment segment = receiveOverUPD();
            if (segment != null) {
                if (segment.getSeqNumber() != ackNumber) {
                    TCPSegment duplicatedAck = new TCPSegment(
                            0,
                            segment.getAckNumber(),
                            segment.getSeqNumber() + segment.getLength(),
                            true,
                            false,
                            false);
                    sendOverUDP(duplicatedAck, clientPort);
                    System.out.println("sending duplicated ACK" + duplicatedAck);
                }
                else {
                    boolean duplicate = false;

                    for (TCPSegment tcpSegment : tcpSegments) {
                        if (tcpSegment.getSeqNumber() == segment.getSeqNumber()) {
                            duplicate = true;
                            break;
                        }
                    }

                    int ack = ackNumber;
                    while (true) {
                        TCPSegment s = null;
                        for (TCPSegment tcpSegment : tcpSegments) {
                            if (tcpSegment.getSeqNumber() == ack) {
                                s = tcpSegment;
                                break;
                            }
                        }

                        if (s == null) {
                            break;
                        }
                        else {
                            ack = s.getSeqNumber() + s.getLength();
                            tcpSegments.remove(s);
                        }
                    }

                    if (!duplicate) {
                        tcpSegments.add(segment);
                        return segment;
                    }
                }
            }
        }
    }

    public void handshake() throws IOException, ClassNotFoundException {
        TCPSegment SYN = receiveOverUPD();
        if (SYN.isSyn() && !SYN.isAck()) {
            seqNumber = SYN.getAckNumber();
            ackNumber = SYN.getSeqNumber() + 1;
            TCPSegment SYN_ACK = new TCPSegment(
                    0,
                    seqNumber,
                    ackNumber,
                    true,
                    true,
                    false);
            sendOverUDP(SYN_ACK, clientPort);
            System.out.println("Server send " + SYN_ACK);
            receiveOverUPD();
            System.out.println("CONNECTION ESTABLISHED");
            System.out.println("=====================================================================================");
            ackNumber += 1;
        }
    }

    public void closeConnection() throws IOException, ClassNotFoundException {
        TCPSegment ackFIN = new TCPSegment(
                0,
                seqNumber,
                ackNumber,
                true,
                false,
                false
        );
        sendOverUDP(ackFIN, clientPort);
        System.out.println("Server send " + ackFIN);

        TCPSegment  FIN = new TCPSegment(
                0,
                seqNumber,
                ackNumber,
                false,
                false,
                true
        );
        sendOverUDP(FIN, clientPort);
        System.out.println("Server send " + FIN);

        receiveOverUPD();
        System.out.println("CONNECTION BREAK");
    }

    public void sendACK(TCPSegment segment) throws IOException {
        seqNumber = segment.getAckNumber();
        ackNumber = segment.getSeqNumber() + segment.getLength();

        TCPSegment ACK = new TCPSegment(
                0,
                seqNumber,
                ackNumber,
                true,
                false,
                false);
        sendOverUDP(ACK, clientPort);
    }

    @Override
    public void run() {
        try {
            handshake();

            TCPSegment segment;
            while (true) {
                segment = reliableReceive();
                if (!segment.isFyn()) {
                    System.out.println("Server got:" + segment.getData());
                    sendACK(segment);
                }
                else {
                    break;
                }
            }
            seqNumber = segment.getAckNumber();
            ackNumber = segment.getSeqNumber() + 1;
            closeConnection();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
