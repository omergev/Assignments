package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

public class Block extends Message {

    private final String userName;

    public Block(String userName) {
        super((short) 12);
        this.userName = userName;
    }

    //Getters
    public String getUserName() {
        return userName;
    }

}
