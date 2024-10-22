package ru.chaplyginma.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

class EchoServerTest {

    private EchoServer echoServer;

    @BeforeEach
    public void setUp() throws Exception {
        echoServer = new EchoServer(12345);
        echoServer.start();
        Thread.sleep(100);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        echoServer.stopServer();
        echoServer.join();
    }

    @Test
    @DisplayName("Test sending and receiving message from server")
    void givenStatedServer_thenSocketConnectAndSendMessage_ThenReceiveSameMessage() throws IOException {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String message = "Hello, EchoServer!";
            out.println(message);
            String response = in.readLine();

            assertThat(message).isEqualTo(response);
        }
    }

}