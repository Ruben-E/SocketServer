package nl.is.kc.nio.chat_server;

import nl.is.kc.nio.util.ExecutorFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Created by ruben on 1-4-15.
 */
public class ChatServer {
    public static void main(String[] args) {
        new ChatServer().launch();
    }

    private static final int PORT = 5000;

    public void launch() {
        try {
            ExecutorService executorService = ExecutorFactory.createExecutor();
            Executor readPool = ExecutorFactory.createExecutor();

            AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService, 1);
            AsynchronousServerSocketChannel socketChannel = AsynchronousServerSocketChannel.open(threadGroup);

            socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            socketChannel.bind(new InetSocketAddress(PORT));

            ClientSession clientSession = new ClientSession();
            clientSession.setSocketChannel(socketChannel);
            clientSession.setReadPool(readPool);

            socketChannel.accept(null, clientSession);

            synchronized (ChatServer.class) {
                try {
                    ChatServer.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
