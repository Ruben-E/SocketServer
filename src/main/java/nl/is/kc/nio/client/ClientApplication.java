package nl.is.kc.nio.client;

import org.omg.CORBA.Environment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ruben on 1-4-15.
 */
public class ClientApplication implements Runnable {
    private static AtomicInteger successCounter = new AtomicInteger(0);
    private static AtomicInteger failCounter = new AtomicInteger(0);

    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 50000; i++) {
            Runnable worker = new ClientApplication();
            executor.execute(worker);
        }

        executor.shutdown();
        while(!executor.isTerminated()) {}
        long endTime = System.currentTimeMillis();
        System.out.println(String.format("Done in: %d seconds", (endTime - startTime) / 1000));
        System.out.println(String.format("Number of successes: %d", successCounter.get()));
        System.out.println(String.format("Number of fails: %d", failCounter.get()));
    }

    public void launch() {
        try (AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open()) {
            if (socketChannel.isOpen()) {
                Future<Void> connect = socketChannel.connect(new InetSocketAddress(5000));
                connect.get(5000, TimeUnit.MILLISECONDS);

                ByteBuffer message = ByteBuffer.wrap(String.format("Thead-%s: %s", String.valueOf(Thread.currentThread().getId()), "Hallo").getBytes());
                socketChannel.write(message);

//                ByteBuffer byteBuffer = ByteBuffer.allocate(4000);
//                socketChannel.read(byteBuffer).get(1000, TimeUnit.MILLISECONDS);

//            System.out.println("Message received: " + Arrays.toString(byteBuffer.array()));

//                byteBuffer.clear();
                socketChannel.shutdownInput();
                socketChannel.shutdownOutput();
                socketChannel.close();

                successCounter.incrementAndGet();
            } else {
                failCounter.incrementAndGet();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            failCounter.incrementAndGet();
//            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        launch();
    }
}
