package ru.chaplyginma;

import ru.chaplyginma.server.EchoServer;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        EchoServer echoServerThread = new EchoServer(11111);

        echoServerThread.start();

        Thread.sleep(20000);
        echoServerThread.stopServer();

        echoServerThread.join();
    }
}