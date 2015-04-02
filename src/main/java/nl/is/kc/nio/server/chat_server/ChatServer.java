package nl.is.kc.nio.server.chat_server;

import nl.is.kc.nio.server.SocketServer;
import nl.is.kc.nio.util.ExecutorFactory;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.Executor;

/**
 * Created by ruben on 1-4-15.
 */
public class ChatServer extends SocketServer {
    public static void main(String[] args) {
        new ChatServer().launch();
    }

    private static final int PORT = 5000;

    protected ChatServer() {
        super(PORT);
    }

    public void launch() {
        AsynchronousServerSocketChannel socketServer = buildSocketServer();
        if (socketServer != null) {
            Executor readPool = ExecutorFactory.createExecutor();

            ClientSession clientSession = new ClientSession();
            clientSession.setSocketChannel(socketServer);
            clientSession.setReadPool(readPool);

            socketServer.accept(null, clientSession);

            synchronized (ChatServer.class) {
                try {
                    ChatServer.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new RuntimeException("Socketserver is null");
        }
    }
}
