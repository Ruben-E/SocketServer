package nl.is.kc.nio.server;

import nl.is.kc.nio.util.ExecutorFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;

/**
 * Created by ruben on 2-4-15.
 */
public class SocketServer {
    private int port;
    private int initialPoolSize = 5;
    private int maximumPoolSize = 200;

    protected SocketServer(int port) {
        this.port = port;
    }

    protected AsynchronousServerSocketChannel buildSocketServer() {
        try {
            ExecutorService executorService = ExecutorFactory.createExecutor(initialPoolSize, maximumPoolSize);
            AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService, 1); // This should be 1. Reader doesnt work otherwise.
            AsynchronousServerSocketChannel socketChannel = AsynchronousServerSocketChannel.open(threadGroup);

            socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            socketChannel.bind(new InetSocketAddress(port));

            return socketChannel;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    protected void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }
}
