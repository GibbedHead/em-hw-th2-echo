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

public class EchoServer extends Thread {

    private static final int MAX_CONNECTIONS = 10;
    private final int port;
    private final Logger logger = LoggerFactory.getLogger(EchoServer.class);
    private ThreadPoolExecutor pool;
    private boolean running = true;

    public EchoServer(int port) {
        super("EchoServerThread");
        this.port = port;
    }

    @Override
    public void run() {
        startServer();
    }

    public void startServer() {
        pool = createThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("EchoServer listening on port {}", port);

            while (running) {
                handleClient(serverSocket);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            shutdown();
        }
    }

    private void handleClient(ServerSocket serverSocket) throws IOException {
        if (pool.isShutdown()) {
            return;
        }

        Socket socket = null;
        serverSocket.setSoTimeout(100);
        try {
            socket = serverSocket.accept();
        } catch (SocketTimeoutException ignored) {
        }

        if (socket == null) {
            return;
        }

        try {
            pool.execute(new ClientHandler(socket));
            logger.info("Pool size is {} / {}", pool.getPoolSize(), MAX_CONNECTIONS);
        } catch (RejectedExecutionException e) {
            if (socket.isConnected()) {
                logger.info("EchoServer max connections reached. Reject connection from: {}", socket.getRemoteSocketAddress());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Max connections reached. Try again later.");
                socket.close();
            }
        }
    }

    private void shutdown() {
        logger.info("EchoServer stopping");

        pool.shutdownNow();
        try {
            if (!pool.awaitTermination(100, TimeUnit.SECONDS)) {
                logger.warn("Some client tasks were not closed in 1 second");
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            running = false;
        }
    }

    private ThreadPoolExecutor createThreadPool() {
        return new ThreadPoolExecutor(
                0,
                MAX_CONNECTIONS,
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>()
        );
    }

    public void stopServer() {
        shutdown();
    }
}
