package ru.chaplyginma.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ClientHandlerTest {

    OutputStream outputStream;
    private Socket mockSocket;
    private ClientHandler clientHandler;

    @BeforeEach
    void setUp() throws IOException {
        mockSocket = mock(Socket.class);

        outputStream = new ByteArrayOutputStream();
        PrintWriter mockWriter = new PrintWriter(outputStream, true);
        given(mockSocket.getOutputStream())
                .willReturn(new OutputStream() {
                    @Override
                    public void write(int b) {
                        mockWriter.write(b);
                    }

                    @Override
                    public void flush() {
                        mockWriter.flush();
                    }
                });

        clientHandler = new ClientHandler(mockSocket);
    }

    @Test
    @DisplayName("Test normal echo response")
    void givenNormalSockedInput_whenRun_thenSameStringInResponse() throws IOException {
        String testString = "Hello World!";

        InputStream inputStream = new ByteArrayInputStream(testString.getBytes());
        given(mockSocket.getInputStream())
                .willReturn(inputStream);

        clientHandler.run();

        String output = outputStream.toString();
        assertThat(testString).isEqualTo(output.trim());
    }

    @Test
    @DisplayName("Test exit command response")
    void givenExitCommand_whenRun_thenRespondWithExitMessage() throws IOException {
        String exitCommand = "!exit";

        InputStream inputStream = new ByteArrayInputStream((exitCommand + "\n").getBytes());
        given(mockSocket.getInputStream())
                .willReturn(inputStream);

        clientHandler.run();

        String output = outputStream.toString().trim();
        assertThat(output).contains("Exit command received");
    }
}