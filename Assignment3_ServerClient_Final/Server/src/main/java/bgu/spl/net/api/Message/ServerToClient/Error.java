package bgu.spl.net.api.Message.ServerToClient;

import bgu.spl.net.api.Message.ServerToClientMessages;

public class Error extends ServerToClientMessages {

    private final short messageOpcode;

    public Error(short messageOpcode) {
        super((short) 11);
        this.messageOpcode = messageOpcode;
        shortConversionToByte((short) 11);
        shortConversionToByte(messageOpcode);
    }

    //Getters

    //TODO: check if necessary
    public short getMessageOpcode() {
        return messageOpcode;
    }
}
