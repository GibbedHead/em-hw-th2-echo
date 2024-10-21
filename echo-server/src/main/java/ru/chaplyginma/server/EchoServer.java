package ru.chaplyginma.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.chaplyginma.handler.ClientHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EchoServer {

    private static final int MAX_CONNECTIONS = 10;
    private final int port;
    private ThreadPoolExecutor pool;
    private final Logger logger = LoggerFactory.getLogger(EchoServer.class);
    private boolean running = true;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() {
        pool = new ThreadPoolExecutor(
                0,
                MAX_CONNECTIONS,
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>()
        );

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(1000);
            logger.info("EchoServer listening on port {}", port);

            for(;;) {
                if (pool.getActiveCount() <= MAX_CONNECTIONS) {
                    try {
                        handleClient(serverSocket.accept());
                    } catch (SocketTimeoutException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            running = false;
            logger.info("EchoServer stopping");
            pool.shutdown();
            try {
                if (!pool.awaitTermination(30, TimeUnit.MINUTES)) {
                    pool.shutdownNow();
                    logger.warn("Some client tasks were not complete in 30 minutes");
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleClient(Socket socket) throws IOException {
        if (pool.isShutdown()) {
            return;
        }
        try {
            pool.execute(new ClientHandler(socket));
            logger.info("Pool size is {} / {}", pool.getPoolSize(), MAX_CONNECTIONS);
        } catch (RejectedExecutionException e) {
            logger.info("EchoServer max connections reached. Reject connection");
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("Max connections reached. Try again later.");
            socket.close();
        }
    }

    public void stop() {
        running = false;
        logger.info("Exit command received");
    }
}
