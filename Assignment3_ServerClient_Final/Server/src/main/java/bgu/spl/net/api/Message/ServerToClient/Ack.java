package bgu.spl.net.api.Message.ServerToClient;

import bgu.spl.net.api.Message.Message;
import bgu.spl.net.api.Message.ServerToClientMessages;

public class Ack extends ServerToClientMessages {

    private short messageOpcode;
    //Optional ; //changes for each message.

    //Constructor
    public Ack(short messageOpcode) {
        super((short) 10);
        this.messageOpcode = messageOpcode;
        shortConversionToByte((short) 10);
        shortConversionToByte(messageOpcode);
    }

    //Constructor for Follow
    public Ack(String username) {
        super((short) 10);
        shortConversionToByte((short) 10);
        shortConversionToByte((short) 4);
        stringConversionToByte(username);
    }

    //Constructor for Stat/Logstat
    public Ack(short opcodeMsg, int age, int numPosts, int numFollowers, int numFollowing) {
        super((short) 10);
        shortConversionToByte((short) 10);
        if (opcodeMsg == 7)
            shortConversionToByte((short) 7);
        else
            shortConversionToByte((short) 8);
        shortConversionToByte((short) age);
        shortConversionToByte((short) numPosts);
        shortConversionToByte((short) numFollowers);
        shortConversionToByte((short) numFollowing);

    }

    //Getters
    //TODO: check if necessary
    public short getMessageOpcode() {
        return messageOpcode;
    }

}
