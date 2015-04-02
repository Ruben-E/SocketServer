package nl.is.kc.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by ruben on 1-4-15.
 */
public class ClientSession implements CompletionHandler<AsynchronousSocketChannel, Void> {
    public static final int TIMEOUT_IN_SECONDS = 10;
    public static final int BYTEBUFFER_CAPACITY = 50;

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

    public void setWritePool(Executor writePool) {
        this.writePool = writePool;
    }

    private class Reader implements Runnable {
        private AsynchronousSocketChannel connection;

        public Reader(AsynchronousSocketChannel connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);

            Writer writer = new Writer(connection);
            writePool.execute(writer);

            try {
                int bytesRead = connection.read(byteBuffer).get(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
                while (bytesRead != -1) {
                    if (connection.isOpen()) {
                        byteBuffer.flip();
                        String message = new String(byteBuffer.array());
                        writer.writeMessage(message);

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
        private Stack<String> messages; // Stack system because of the multi threaded environment. Under a spam of messages it could happen that the previous write was not completed yet.

        public Writer(AsynchronousSocketChannel connection) {
            this.connection = connection;
            this.messages = new Stack<>();
        }

        public synchronized void writeMessage(String message) {
            messages.push(message);
        }

        @Override
        public void run() {
            while(connection.isOpen()) {
                synchronized (this) {
                    if (!messages.empty()) {
                        String message = messages.pop();
                        connection.write(ByteBuffer.wrap(message.getBytes()));
                    }
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
