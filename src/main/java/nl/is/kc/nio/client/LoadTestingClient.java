package nl.is.kc.nio.client;

import nl.is.kc.nio.client.model.BulkMessageResponse;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ruben on 2-4-15.
 */
public class LoadTestingClient {
    private static final int READ_TIMEOUT_IN_SECONDS = 5;
    private AtomicInteger messageCounter = new AtomicInteger(0);
    private AtomicInteger messageSuccessCounter = new AtomicInteger(0);
    private AtomicInteger messageFailCounter = new AtomicInteger(0);
    private AtomicInteger timeoutCounter = new AtomicInteger(0);
    private AtomicInteger exceptionCounter = new AtomicInteger(0);

    protected BulkMessageResponse sendBulkMessagesToSocket(AsynchronousSocketChannel socket, int numerOfMessages) {
        for (int i = 0; i < numerOfMessages; i++) {
            try {
                String messageToSend = String.format("Thread-%s: %s", String.valueOf(Thread.currentThread().getId()), "Hallo" + i);

                writeToSocket(socket, messageToSend);

                messageCounter.incrementAndGet();

                ByteBuffer readBuffer = readFromSocket(socket);

                readBuffer.flip();
                String messageReceived = new String(readBuffer.array()).trim();

                if (messageToSend.trim().equals(messageReceived)) {
                    messageSuccessCounter.incrementAndGet();
                } else {
                    messageFailCounter.incrementAndGet();
                }
            } catch (TimeoutException e) {
                timeoutCounter.incrementAndGet();
            } catch (Exception e) {
                exceptionCounter.incrementAndGet();
            }
        }

        return new BulkMessageResponse(messageCounter.intValue(),
                messageSuccessCounter.intValue(),
                messageFailCounter.intValue(),
                timeoutCounter.intValue(),
                exceptionCounter.intValue());
    }

    protected Future<Void> connectToSocket(AsynchronousSocketChannel socket, InetSocketAddress socketAddress) {
        return socket.connect(socketAddress);
    }

    protected ByteBuffer readFromSocket(AsynchronousSocketChannel socket) throws InterruptedException, ExecutionException, TimeoutException {
        ByteBuffer readBuffer = ByteBuffer.allocate(1000);
        socket.read(readBuffer).get(READ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

        return readBuffer;
    }

    protected Future<Integer> writeToSocket(AsynchronousSocketChannel socket, String message) {
        ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
        return socket.write(writeBuffer);
    }
}
