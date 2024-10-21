package ru.chaplyginma;

import ru.chaplyginma.server.EchoServer;

public class Main {
    public static void main(String[] args) {
        EchoServer echoServer = new EchoServer(11111);

        echoServer.start();
    }
}