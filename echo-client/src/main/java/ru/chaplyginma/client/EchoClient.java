package ru.chaplyginma.client;

import ru.chaplyginma.exception.SocketException;
import ru.chaplyginma.exception.StreamException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class EchoClient implements AutoCloseable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public void start(String host, int port) throws SocketException, StreamException {
        try {
            socket = new Socket(host, port);
            System.out.println("EchoClient started");
            System.out.println("Connected to " + host + ":" + port);
        } catch (UnknownHostException e) {
            throw new SocketException("Unknown host: %s".formatted(host), e);
        } catch (IOException e) {
            throw new SocketException("I/O exception: %s".formatted(e.getMessage()), e);
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new StreamException("Can't get input stream", e);
        }
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new StreamException("Can't get output stream", e);
        }
    }

    public void stop() throws StreamException, SocketException {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new SocketException("Can't close socket", e);
            }
        }
        if (in != null) {

            try {
                in.close();
            } catch (IOException e) {
                throw new StreamException("Can't close input stream", e);
            }
        }
        if (out != null) {
            out.close();
        }
    }

    public String sendMessage(String message) throws StreamException {
        out.println(message);
        out.flush();

        try {
            return in.readLine();
        } catch (IOException e) {
            throw new StreamException("Connection to server closed", e);
        }
    }

    @Override
    public void close() {
        try {
            stop();
        } catch (SocketException | StreamException e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("EchoClient closed");
        }

    }
}
