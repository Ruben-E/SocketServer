package nl.is.kc.nio.name_server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by ruben on 1-4-15.
 */
public class ClientSession implements CompletionHandler<AsynchronousSocketChannel, Void> {
    private AsynchronousServerSocketChannel socketChannel;

    @Override
    public void completed(AsynchronousSocketChannel connection, Void attachment) {
        socketChannel.accept(null, this);

        try {
            String message = "localhost:5000";

            if (connection.isOpen()) {
                connection.write(ByteBuffer.wrap(message.getBytes()));
                connection.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exc, Void attachment) {
        exc.printStackTrace();
    }

    public void setSocketChannel(AsynchronousServerSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}
