package ru.chaplyginma.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.chaplyginma.handler.ClientHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class implements a multithreaded TCP Echo Server.
 * It listens for incoming client connections on a specified port
 * and handles each connection in a separate thread using a thread pool.
 */
public class EchoServer extends Thread {

    private static final int MAX_CONNECTIONS = 10;
    private final int port;
    private final Logger logger = LoggerFactory.getLogger(EchoServer.class);
    private ThreadPoolExecutor pool;
    private boolean running = true;

    /**
     * Constructs an EchoServer with the specified port.
     *
     * @param port the port number to listen on
     */
    public EchoServer(int port) {
        super("EchoServerThread");
        this.port = port;
    }

    /**
     * Starts the server and listens for incoming client connections.
     * It accepts a maximum number of concurrent connections defined
     * by {@link #MAX_CONNECTIONS}.
     */
    @Override
    public void run() {
        startServer();
    }

    public void startServer() {
        pool = createThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("EchoServer listening on port {}", port);

            while (running) {
                if (pool.getActiveCount() < MAX_CONNECTIONS) {
                    handleClient(serverSocket);
                }
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
                new SynchronousQueue<>(),
                getCustomNamedThreadFactory()
        );
    }

    private ThreadFactory getCustomNamedThreadFactory() {
        return new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                String nameTemplate = "EchoServer-ClientThread-%d";
                String threadName = String.format(nameTemplate, threadNumber.getAndIncrement());
                return new Thread(r, threadName);
            }
        };
    }

    public void stopServer() {
        shutdown();
    }
}
