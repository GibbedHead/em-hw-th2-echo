package ru.chaplyginma.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * This class handles client interactions for the EchoServer.
 * Each instance is responsible for processing communication with a single client.
 */
public class ClientHandler implements Runnable {

    private static final String EXIT_COMMAND = "!exit";
    private static final String EXIT_RESPONSE = "Exit command received";
    private final Socket socket;
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    /**
     * Constructs a ClientHandler for the specified client socket.
     *
     * @param socket the client socket to handle
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * Runs the client handling logic in a separate thread.
     * This method processes messages received from the client.
     */
    @Override
    public void run() {
        logger.info("{}: Client thread started", Thread.currentThread().getName());
        try {
            socket.setSoTimeout(1);
        } catch (SocketException e) {
            logger.error("Socket exception during setting client socket timeout: {}", e.getMessage());
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            handleMessages(in, out);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                logger.info("{}: Closing client connection and stopping thread", Thread.currentThread().getName());
                socket.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void handleMessages(BufferedReader in, PrintWriter out) throws IOException {
        String inputLine;
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            try {
                inputLine = in.readLine();
                if (inputLine == null || EXIT_COMMAND.equals(inputLine)) {
                    break;
                }
                out.println(inputLine);
            } catch (SocketTimeoutException ignored) {
            }
        }
        if (EXIT_COMMAND.equals(inputLine)) {
            out.println(EXIT_RESPONSE);
            logger.info("{}: Client requested exit", Thread.currentThread().getName());
        }
    }
}
