package ru.chaplyginma.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

class EchoServerTest {

    private EchoServer echoServer;
    private Thread serverThread;

    @BeforeEach
    public void setUp() throws Exception {
        echoServer = new EchoServer(12345);
        serverThread = new Thread(echoServer::start);
        serverThread.start();
        Thread.sleep(1000);
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    void testEcho() throws IOException {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String message = "Hello, EchoServer!";
            out.println(message);
            String response = in.readLine();

            assertThat(message).isEqualTo(response);
        }
    }

    @Test
    void testMaxConnections() throws IOException {
        int maxConnections = 10; // MAX_CONNECTIONS в EchoServer

        // Создаем максимальное количество подключений
        for (int i = 0; i < maxConnections; i++) {
            new Thread(() -> {
                try (Socket socket = new Socket("localhost", 12345)) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("Test message");
                    // Ожидаем ответа от сервера
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Проверяем, что 11-ое подключение будет отклонено
        try (Socket socket = new Socket("localhost", 12345)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("This should be rejected");

            String response = in.readLine();
            assertThat(response).isEqualTo("Max connections reached. Try again later.");
        } catch (IOException e) {
            // Ожидаем исключение, если соединение не удалось установить
            System.out.println("Connection rejected as expected.");
        }
    }
}