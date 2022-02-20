package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BGSProtocol;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) {
        if (args != null) {
            int port = Integer.parseInt(args[0]);
            int numOfThreads = Integer.parseInt(args[1]);
            Server.reactor(
                    numOfThreads,
                    port, //port
                    () -> new BGSProtocol(), //protocol factory
                    MessageEncoderDecoderImpl::new //message encoder decoder factory
            ).serve();

        }
    }
}
