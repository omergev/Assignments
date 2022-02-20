package bgu.spl.net.api.Message;

public abstract class Message {
    private final short opcode;

    public Message (short opcode){
        this.opcode = opcode;
    }

    public short getOpcode() {
        return opcode;
    }


}
