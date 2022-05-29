package com.company;

import java.net.SocketException;

public class Main {
    public static void main(String[] args) throws SocketException {
        Thread server = new Thread(new TCPServerSocket(0, 8080, 8888));
        Thread client = new Thread(new TCPClientSocket(0.5, 8888, 8080));
        server.start();
        client.start();
    }
}
