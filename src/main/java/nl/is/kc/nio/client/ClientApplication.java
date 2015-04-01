package nl.is.kc.nio.client;

import nl.is.kc.nio.util.ExecutorFactory;
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
    private static AtomicInteger exceptionCounter = new AtomicInteger(0);
    private static AtomicInteger timeoutCounter = new AtomicInteger(0);
    private static AtomicInteger messageCounter = new AtomicInteger(0);

    public static void main(String[] args) {
        ExecutorService executor = ExecutorFactory.createExecutor(5, 50);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            Runnable worker = new ClientApplication();
            executor.execute(worker);
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        long endTime = System.currentTimeMillis();
        System.out.println(String.format("Done in: %d seconds", (endTime - startTime) / 1000));
        System.out.println(String.format("Number of messages: %d", messageCounter.get()));
        System.out.println(String.format("Number of successes: %d", successCounter.get()));
        System.out.println(String.format("Number of timeouts: %d", timeoutCounter.get()));
        System.out.println(String.format("Number of exceptions: %d", exceptionCounter.get()));
        System.out.println(String.format("Number of fails: %d", failCounter.get()));
    }

    public void launch() {
        try (AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open()) {

            if (socketChannel.isOpen()) {
                Future<Void> connect = socketChannel.connect(new InetSocketAddress(5000));
                connect.get(5000, TimeUnit.MILLISECONDS);

                int counter = 0;

                for (int i = 0; i < 5000; i++) {
                    ByteBuffer message = ByteBuffer.wrap(String.format("Thread-%s: %s", String.valueOf(Thread.currentThread().getId()), "Hallo" + counter).getBytes());
                    socketChannel.write(message);
                    messageCounter.incrementAndGet();
                    counter++;
                }

                socketChannel.close();

                successCounter.incrementAndGet();
            } else {
                failCounter.incrementAndGet();
            }
        } catch (TimeoutException e) {
            timeoutCounter.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
            exceptionCounter.incrementAndGet();
        }
    }

    @Override
    public void run() {
        launch();
    }
}
