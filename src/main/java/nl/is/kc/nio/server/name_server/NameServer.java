package nl.is.kc.nio.server.name_server;

import nl.is.kc.nio.server.SocketServer;
import nl.is.kc.nio.util.ExecutorFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;

/**
 * Created by ruben on 2-4-15.
 */
public class NameServer extends SocketServer {
    private static final int PORT = 5001;

    public static void main(String[] args) {
        new NameServer().launch();
    }

    protected NameServer() {
        super(PORT);
    }

    public void launch() {
        AsynchronousServerSocketChannel socketServer = buildSocketServer();
        if (socketServer != null) {
            ClientSession clientSession = new ClientSession();
            clientSession.setSocketChannel(socketServer);

            socketServer.accept(null, clientSession);

            synchronized (NameServer.class) {
                try {
                    NameServer.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
