package bgu.spl.net.srv;

// import bgu.spl.net.api.MessageEncoderDecoder;
// import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.StompMessageEncoderDecoder;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.impl.stomp.ConnectionsImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {
    private final int port;
    private final Supplier<StompMessagingProtocol<T>> protocolFactory;
    private final Supplier<StompMessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;
    private Connections<T> connections;
    private Integer connectionId = 0;


    public BaseServer( // TPC
            int port,
            Supplier<StompMessagingProtocol<T>> protocolFactory,
            Supplier<StompMessageEncoderDecoder<T>> encdecFactory,
            Connections<T> connections) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
        this.sock = null;
        // this.connections = new ConnectionsImpl();
        this.connections = connections;
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
            System.out.println("Server started");

            this.sock = serverSock; // just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();

                StompMessagingProtocol<T> newProtocol = protocolFactory.get();

                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(
                        clientSock,
                        encdecFactory.get(),
                        newProtocol
                        );

                Integer clientConnectionId = getNewConnectionId(); 
                connections.addConnectionHandler(clientConnectionId, handler);
                newProtocol.start(clientConnectionId, connections);

                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
        if (sock != null)
            sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T> handler);

    private Integer getNewConnectionId() {
        synchronized(this){
            connectionId++;
            return connectionId;
        }
    }

}
