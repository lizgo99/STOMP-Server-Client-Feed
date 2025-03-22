package bgu.spl.net.impl.stomp;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        // TODO: implement this
        
        Connections<String> connections = new ConnectionsImpl();

        if(args[1].equals("tpc")){
            Server.threadPerClient(
                Integer.parseInt(args[0]), //port 
                () -> new StompMessagingProtocolImpl(), //protocol factory
                StompMessageEncoderDecoderImpl::new, //message encoder decoder factory
                connections
            ).serve();
        }
        else{
            Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                Integer.parseInt(args[0]), //port
                () -> new StompMessagingProtocolImpl(), //protocol factory
                StompMessageEncoderDecoderImpl::new, //message encoder decoder factory
                connections
            ).serve();
        } 
    }
}
