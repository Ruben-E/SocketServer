package nl.is.kc.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ruben on 1-4-15.
 */
public class ClientSession implements CompletionHandler<AsynchronousSocketChannel, Void> {
    private AsynchronousServerSocketChannel socketChannel;
    private Executor readPool;
    private Executor writePool;

    @Override
    public void completed(AsynchronousSocketChannel connection, Void attachment) {
        socketChannel.accept(null, this);
        connection.write(ByteBuffer.wrap("Connected!".getBytes()));

        readPool.execute(new Runnable() {
            @Override
            public void run() {
                ByteBuffer byteBuffer = ByteBuffer.allocate(50);

                try {
                    int bytesRead = connection.read(byteBuffer).get(1, TimeUnit.SECONDS);
                    while (bytesRead != -1) {
                        byteBuffer.flip();
                        String message = new String(byteBuffer.array());

                        writePool.execute(new Runnable() {
                            @Override
                            public void run() {
                                connection.write(ByteBuffer.wrap(message.getBytes()));
                            }
                        });

                        byteBuffer.clear();
                        bytesRead = -1;
                    }
                } catch (Exception ignored) {
                } finally {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
//
//        ByteBuffer byteBuffer = ByteBuffer.allocate(4000);
//
//
//        try {
//            int bytesRead = connection.read(byteBuffer).get(1, TimeUnit.SECONDS);
//            while (bytesRead != -1) {
////                System.out.println("Bytes: " + bytesRead);
//                byteBuffer.flip();
//                String message = new String(byteBuffer.array());
//
//                connection.write(ByteBuffer.wrap(message.getBytes()));
//
//                byteBuffer.clear();
//
////                bytesRead = connection.read(byteBuffer).get(1, TimeUnit.SECONDS);
//                bytesRead = -1;
//            }
//        } catch (Exception ignored) {
//        } finally {
//            try {
////                System.out.println("Closing connection");
//                connection.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
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
}
