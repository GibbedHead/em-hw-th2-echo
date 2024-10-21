package ru.chaplyginma;

import ru.chaplyginma.client.EchoClient;
import ru.chaplyginma.exception.SocketException;
import ru.chaplyginma.exception.StreamException;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final String host = "127.0.0.1";
        final int port = 11111;
        final String exitCommand = "!exit";
        final String exitResponse = "Exit command received";

        try (
                EchoClient client = new EchoClient();
                Scanner scanner = new Scanner(System.in)
        ) {
            client.start(host, port);

            String userInput;

            System.out.printf("Type messages for the server ('%s' to exit): ", exitCommand);

            while (true) {
                if (scanner.hasNextLine()) {
                    userInput = scanner.nextLine();

                    String response = client.sendMessage(userInput);

                    if (response == null) {
                        System.out.println("Server connection closed. Exiting");
                        return;
                    }

                    System.out.printf("Server response: %s%n", response);

                    if (exitResponse.equals(response)) {
                        System.out.println("Exiting");
                        return;
                    }
                    System.out.printf("Type messages for the server ('%s' to exit): ", exitCommand);
                }

            }

        } catch (SocketException | StreamException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}