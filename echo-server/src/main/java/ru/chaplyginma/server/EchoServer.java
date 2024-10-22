package ru.chaplyginma.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.chaplyginma.handler.ClientHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EchoServer {

    private static final int MAX_CONNECTIONS = 10;
    private final int port;
    private final Logger logger = LoggerFactory.getLogger(EchoServer.class);
    private ThreadPoolExecutor pool;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() {
        pool = createThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("EchoServer listening on port {}", port);

            while (true) {
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
        try {
            socket = serverSocket.accept();
            pool.execute(new ClientHandler(socket));
            logger.info("Pool size is {} / {}", pool.getPoolSize(), MAX_CONNECTIONS);
        } catch (RejectedExecutionException e) {
            if (socket != null && socket.isConnected()) {
                logger.info("EchoServer max connections reached. Reject connection from: {}", socket.getRemoteSocketAddress());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Max connections reached. Try again later.");
                socket.close();
            }
        }
    }

    private void shutdown() {
        logger.info("EchoServer stopping");
        pool.shutdown();
        try {
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                logger.warn("Some client tasks were not closed in 30 seconds");
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
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
}
