package ru.chaplyginma.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private static final String EXIT_COMMAND = "!exit";
    private static final String EXIT_RESPONSE = "Exit command received";
    private final Socket socket;
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);


    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        logger.info("{}: Client thread started", Thread.currentThread().getName());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String inputLine;
            while ((inputLine = in.readLine()) != null && !EXIT_COMMAND.equals(inputLine) && !Thread.currentThread().isInterrupted()) {
                out.println(inputLine);
            }
            if (EXIT_COMMAND.equals(inputLine)) {
                out.println(EXIT_RESPONSE);
                logger.info("{}: Client requested exit", Thread.currentThread().getName());
            }
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
}
