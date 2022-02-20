package bgu.spl.net.api.Message;

import java.util.ArrayList;

public class ServerToClientMessages extends Message {

    protected ArrayList<Byte> bytesArray;
    private byte[] bytesMessage;


    public ServerToClientMessages(short opCode){
        super(opCode);
        bytesArray = new ArrayList<>();
        bytesMessage = null;
    }


    //This method call from the method Encode
    public byte[] getBytesMessage() {
        if (bytesMessage == null)
            bytesMessage = addBytesToBytesMsg();
        return bytesMessage;
    }

    //Adds the bits we added to the bytesArray
    public byte[] addBytesToBytesMsg() {
        byte[] output = new byte[bytesArray.size()];
        for (int i = 0; i < bytesArray.size(); i++) {
            output[i] = bytesArray.get(i);
        }
        return output;
    }

    public void shortConversionToByte(short opcode) {//short equals to 2 bytes
        byte[] toAdd = shortToBytes((short)opcode);
        bytesArray.add(toAdd[0]);
        bytesArray.add(toAdd[1]);
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    public void stringConversionToByte(String st) {
        if (st != null) {
            byte[] toAdd = st.getBytes();
            for (int i = 0; i < toAdd.length; i++)
                bytesArray.add(toAdd[i]);
            bytesArray.add((byte) '\0');
        }
    }

}

