package nl.is.kc.nio.name_server;

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
public class NameServer {
    private static final int PORT = 5001;

    public static void main(String[] args) {
        new NameServer().launch();
    }

    public void launch() {
        try {
            ExecutorService executorService = ExecutorFactory.createExecutor();

            AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService, 1);
            AsynchronousServerSocketChannel socketChannel = AsynchronousServerSocketChannel.open(threadGroup);

            socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            socketChannel.bind(new InetSocketAddress(PORT));

            ClientSession clientSession = new ClientSession();
            clientSession.setSocketChannel(socketChannel);

            socketChannel.accept(null, clientSession);

            synchronized (NameServer.class) {
                try {
                    NameServer.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
