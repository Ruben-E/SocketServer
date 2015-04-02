package nl.is.kc.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.*;

/**
 * Created by ruben on 1-4-15.
 */
public class ClientSession implements CompletionHandler<AsynchronousSocketChannel, Void> {
    public static final int TIMEOUT_IN_SECONDS = 10;
    public static final int BYTEBUFFER_CAPACITY = 1000;

    private AsynchronousServerSocketChannel socketChannel;
    private Executor readPool;
    private Executor writePool;

    @Override
    public void completed(AsynchronousSocketChannel connection, Void attachment) {
        socketChannel.accept(null, this);
        readPool.execute(new Reader(connection));
    }

    @Override
    public void failed(Throwable exc, Void attachment) {
        exc.printStackTrace();
    }

    public void setSocketChannel(AsynchronousServerSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void setReadPool(Executor readPool) {
        this.readPool = readPool;
    }

    private class Reader implements Runnable {
        private AsynchronousSocketChannel connection;

        public Reader(AsynchronousSocketChannel connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            readMessages();
        }

        private void readMessages() {
            ByteBuffer byteBuffer = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);

            try {
                int bytesRead = connection.read(byteBuffer).get(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
                while (bytesRead != -1) {
                    if (connection.isOpen()) {
                        byteBuffer.flip();
                        String message = new String(byteBuffer.array());

                        sendMessage(message);

                        byteBuffer.clear();
                        bytesRead = connection.read(byteBuffer).get(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
                    } else {
                        break;
                    }
                }
            } catch (TimeoutException e) {
                closeConnection();
            } catch (InterruptedException | ExecutionException ignored) {

            }
        }

        private void sendMessage(String message) {
            if (writePool == null) {
                writePool = Executors.newSingleThreadExecutor(); // Lazy loading
            }

            writePool.execute(new Writer(connection, message));
        }

        private void closeConnection() {
            try {
                if (connection.isOpen()) {
                    connection.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class Writer implements Runnable {
        private AsynchronousSocketChannel connection;
        private String message;

        public Writer(AsynchronousSocketChannel connection, String message) {
            this.connection = connection;
            this.message = message;
        }

        @Override
        public void run() {
            if (connection.isOpen()) {
                connection.write(ByteBuffer.wrap(message.getBytes()));
            }
        }
    }
}
