package nl.is.kc.nio.client;

import nl.is.kc.nio.util.ExecutorFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ruben on 1-4-15.
 */
public class ClientApplication implements Runnable {
    private static final int PORT = 5000;
    private static final int NUMBER_OF_MESSAGES = 5000;
    private static final int MAXIMUM_POOL_SIZE = 50;
//    private static final int NUMBER_OF_CONNECTIONS = 1000;
    private static final int NUMBER_OF_CONNECTIONS = 100;
    private static final int CONNECTION_TIMEOUT_IN_SECONDS = 5;
    private static final int READ_TIMEOUT_IN_SECONDS = 5;

    private static AtomicInteger successCounter = new AtomicInteger(0);
    private static AtomicInteger failCounter = new AtomicInteger(0);
    private static AtomicInteger exceptionCounter = new AtomicInteger(0);
    private static AtomicInteger timeoutCounter = new AtomicInteger(0);
    private static AtomicInteger messageCounter = new AtomicInteger(0);
    private static AtomicInteger invalidMessageCounter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = ExecutorFactory.createExecutor(5, MAXIMUM_POOL_SIZE);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_OF_CONNECTIONS; i++) {
            Runnable worker = new ClientApplication();
            executor.execute(worker);
        }

        executor.shutdown();
        executor.awaitTermination(180, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        System.out.println(String.format("Done in: %d seconds", (endTime - startTime) / 1000));
        System.out.println(String.format("Number of messages: %d", messageCounter.get()));
        System.out.println(String.format("Number of invalid messages: %d", invalidMessageCounter.get()));
        System.out.println(String.format("Number of successes: %d", successCounter.get()));
        System.out.println(String.format("Number of timeouts: %d", timeoutCounter.get()));
        System.out.println(String.format("Number of exceptions: %d", exceptionCounter.get()));
        System.out.println(String.format("Number of fails: %d", failCounter.get()));
    }

    public void launch() {
        try (AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open()) {

            if (socketChannel.isOpen()) {
                Future<Void> connect = socketChannel.connect(new InetSocketAddress(PORT));
                connect.get(CONNECTION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

                int counter = 0;
                for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
                    String messageToSend = String.format("Thread-%s: %s", String.valueOf(Thread.currentThread().getId()), "Hallo" + counter);
                    ByteBuffer message = ByteBuffer.wrap(messageToSend.getBytes());

                    socketChannel.write(message);
                    messageCounter.incrementAndGet();

                    ByteBuffer readBuffer = ByteBuffer.allocate(1000);
                    socketChannel.read(readBuffer).get(READ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

                    readBuffer.flip();
                    String messageReceived = new String(readBuffer.array()).trim();

                    if (messageToSend.trim().equals(messageReceived)) {
                        successCounter.incrementAndGet();
                    } else {
                        invalidMessageCounter.incrementAndGet();
                    }

                    counter++;
                }

                socketChannel.close();
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
