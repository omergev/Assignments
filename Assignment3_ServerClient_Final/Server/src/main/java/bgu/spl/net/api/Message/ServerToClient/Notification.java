package bgu.spl.net.api.Message.ServerToClient;

import bgu.spl.net.api.Message.ServerToClientMessages;

public class Notification extends ServerToClientMessages {

    private final short nType;//0=PM, 1=Post
    private final String postingUser;
    private final String content;

    public Notification(short nType, String postingUser, String content) {
        super((short) 9);
        this.nType = nType;
        this.postingUser = postingUser;
        this.content = content;
        shortConversionToByte((short) 9);
        if (nType==0)
            bytesArray.add((byte) '\0');
        else
            bytesArray.add((byte) '\1');
        stringConversionToByte(postingUser);
        stringConversionToByte(content);
    }

    //Getters
    //TODO: check if necessary
    public short getnType() {
        return nType;
    }
    public String getPostingUser() {
        return postingUser;
    }
    public String getContent() {
        return content;
    }

}
