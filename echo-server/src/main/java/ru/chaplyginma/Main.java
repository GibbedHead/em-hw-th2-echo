package ru.chaplyginma;

import ru.chaplyginma.server.EchoServer;

public class Main {
    public static void main(String[] args) {
        EchoServer echoServerThread = new EchoServer(11111);

        echoServerThread.start();
    }
}