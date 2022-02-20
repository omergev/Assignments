package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

public class LogOut extends Message {

    public LogOut() {
        super((short) 3);
    }

}
