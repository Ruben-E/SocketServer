package nl.is.kc.nio.client;

import nl.is.kc.nio.client.model.BulkMessageResponse;
import nl.is.kc.nio.client.model.NameServerResponse;
import nl.is.kc.nio.util.ExecutorFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ruben on 2-4-15.
 */
public class LoadTestingClientWithNameServer extends LoadTestingClient implements Runnable {
    private static final int NAME_SERVER_PORT = 5001;
    private static final int NUMBER_OF_MESSAGES = 5000;
    private static final int MAXIMUM_POOL_SIZE = 50;
    //    private static final int NUMBER_OF_CONNECTIONS = 1000;
    private static final int NUMBER_OF_CONNECTIONS = 100;
    private static final int CONNECTION_TIMEOUT_IN_SECONDS = 5;

    private static AtomicInteger messageCounter = new AtomicInteger(0);
    private static AtomicInteger messageSuccessCounter = new AtomicInteger(0);
    private static AtomicInteger messageFailCounter = new AtomicInteger(0);
    private static AtomicInteger timeoutCounter = new AtomicInteger(0);
    private static AtomicInteger exceptionCounter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = ExecutorFactory.createExecutor(5, MAXIMUM_POOL_SIZE);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_OF_CONNECTIONS; i++) {
            Runnable worker = new LoadTestingClientWithNameServer();
            executor.execute(worker);
        }

        executor.shutdown();
        executor.awaitTermination(180, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        System.out.println(String.format("Done in: %d seconds", (endTime - startTime) / 1000));
        System.out.println(String.format("Number of messages: %d", messageCounter.get()));
        System.out.println(String.format("Number of successful messages: %d", messageSuccessCounter.get()));
        System.out.println(String.format("Number of failed messages: %d", messageFailCounter.get()));
        System.out.println(String.format("Number of timeouts: %d", timeoutCounter.get()));
        System.out.println(String.format("Number of exceptions: %d", exceptionCounter.get()));
    }

    private void sendMessages(String host, int port) {
        try (AsynchronousSocketChannel socket = AsynchronousSocketChannel.open()) {
            if (socket.isOpen()) {
                Future<Void> connect = connectToSocket(socket, new InetSocketAddress(host, port));
                connect.get(CONNECTION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

                BulkMessageResponse bulkMessageResponse = sendBulkMessagesToSocket(socket, NUMBER_OF_MESSAGES);
                socket.close();

                messageCounter.addAndGet(bulkMessageResponse.getMessageCounter());
                messageSuccessCounter.addAndGet(bulkMessageResponse.getMessageSuccessCounter());
                messageFailCounter.addAndGet(bulkMessageResponse.getMessageFailCounter());
                timeoutCounter.addAndGet(bulkMessageResponse.getTimeoutCounter());
                exceptionCounter.addAndGet(bulkMessageResponse.getExceptionCounter());
            } else {
                timeoutCounter.incrementAndGet();
            }
        } catch (TimeoutException e) {
            timeoutCounter.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
            exceptionCounter.incrementAndGet();
        }
    }

    private NameServerResponse connectToNameServer() {
        try (AsynchronousSocketChannel socket = AsynchronousSocketChannel.open()) {
            if (socket.isOpen()) {
                Future<Void> connect = connectToSocket(socket, new InetSocketAddress(NAME_SERVER_PORT));
                connect.get(CONNECTION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

                ByteBuffer readBuffer = readFromSocket(socket);

                readBuffer.flip();
                String messageReceived = new String(readBuffer.array()).trim();

                String[] messageReceivedPars = messageReceived.split(":");
                String host = messageReceivedPars[0];
                int port = Integer.valueOf(messageReceivedPars[1]);

                return new NameServerResponse(host, port);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void launch() {
        NameServerResponse nameServerResponse = connectToNameServer();
        if (nameServerResponse != null) {
            sendMessages(nameServerResponse.getHost(), nameServerResponse.getPort());
        }
    }

    @Override
    public void run() {
        launch();
    }
}
