package nl.is.kc.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.*;

/**
 * Created by ruben on 1-4-15.
 */
public class SocketSever {
    public static void main(String[] args) {
        new SocketSever().launch();
    }

    private static final int PORT = 5000;
    private static final int NUMBER_OF_THREADS = 20;

    public void launch() {
        try {
            ExecutorService executorService = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
            AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService, 1);
//            AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withFixedThreadPool(NUMBER_OF_THREADS, Executors.defaultThreadFactory()); // This is very bad: can cause deadlocks (http://isaiah-v.blogspot.nl/2010/11/java-7-avoiding-deadlock-in-nio2.html)
//            AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool());

            AsynchronousServerSocketChannel socketChannel = AsynchronousServerSocketChannel.open(threadGroup);
//            AsynchronousServerSocketChannel socketChannel = AsynchronousServerSocketChannel.open();

            socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);


            Executor readPool = Executors.newCachedThreadPool(); //Tests
            Executor writePool = Executors.newCachedThreadPool();

            socketChannel.bind(new InetSocketAddress(PORT));

            ClientSession clientSession = new ClientSession();
            clientSession.setSocketChannel(socketChannel);
            clientSession.setReadPool(readPool);
            clientSession.setWritePool(writePool);

            socketChannel.accept(null, clientSession);

            synchronized (SocketSever.class) {
                try {
                    SocketSever.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
