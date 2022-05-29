package com.company;

import java.io.*;

public class TCPSegment implements Serializable {
    private String data;
    private final int length;
    private final int seqNumber;
    private final int ackNumber;
    private final boolean ack;
    private final boolean syn;
    private final boolean fyn;

    public TCPSegment(String data, int length, int seqNumber, int ackNumber, boolean ack, boolean syn, boolean fyn) {
        this.data = data;
        this.length = length;
        this.seqNumber = seqNumber;
        this.ackNumber = ackNumber;
        this.ack = ack;
        this.syn = syn;
        this.fyn = fyn;
    }

    public TCPSegment(int length, int seqNumber, int ackNumber, boolean ack, boolean syn, boolean fyn) {
        this.length = length;
        this.seqNumber = seqNumber;
        this.ackNumber = ackNumber;
        this.ack = ack;
        this.syn = syn;
        this.fyn = fyn;
    }

    public byte[] serializeSegment() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(byteArrayOutputStream));
            outputStream.writeObject(this);
            outputStream.flush();
            outputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public String toString() {
        return "TCPSegment{" +
                "data='" + data + '\'' +
                ", length=" + length +
                ", seqNumber=" + seqNumber +
                ", ackNumber=" + ackNumber +
                ", ack=" + ack +
                ", syn=" + syn +
                ", fyn=" + fyn +
                '}';
    }

    public String getData() {
        return data;
    }

    public int getLength() {
        return length;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public int getAckNumber() {
        return ackNumber;
    }

    public boolean isAck() {
        return ack;
    }

    public boolean isSyn() {
        return syn;
    }

    public boolean isFyn() {
        return fyn;
    }
}
