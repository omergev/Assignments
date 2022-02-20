package bgu.spl.net.api;

import bgu.spl.net.api.Message.ClientToServer.*;
import bgu.spl.net.api.Message.Message;
import bgu.spl.net.api.Message.ClientToServer.Block;
import bgu.spl.net.api.Message.ServerToClientMessages;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Message>{

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private short opcode = -1;
    String [] ArrayMessage;

    @Override
    public Message decodeNextByte(byte nextByte) {
//        System.out.println(nextByte);
        if(len == 2){//The first two bits are always opcode
            opcode = bytesToShort(bytes);
        }

        if (nextByte == ';') {//Every message should be ended with the symbol ‘;’,
            return popMessage();
        }

        pushByte(nextByte);
        return null; //not a line yet
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    @Override
    public byte[] encode(Message message) {
        return ((ServerToClientMessages)message).getBytesMessage();
    }

    private Message popMessage() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in jSava.
        String inputString =  new String(bytes, 2, len-2, StandardCharsets.UTF_8);
//        System.out.println(inputString);
        ArrayMessage = inputString.split("\0");//After each string the character '\0' appears
//        System.out.println(Arrays.toString(ArrayMessage));
        Message result = null;
//        System.out.println(opcode);
        switch ((opcode)){
            case 1://Register
                result = new Register(ArrayMessage[0], ArrayMessage[1], ArrayMessage[2]); break;

            case 2://Login
                int captcha = Integer.parseInt(ArrayMessage[2]);
                result = new Login(ArrayMessage[0], ArrayMessage[1], captcha); break;

            case 3://Logout
                result = new LogOut(); break;

            case 4://Follow/Unfollow
                int follow = Character.getNumericValue(ArrayMessage[0].charAt(0));
                result = new FollowUnfollow(follow,ArrayMessage[0].substring(1)); break;

            case 5://Post
                result = new Post(ArrayMessage[0]); break;

            case 6://PM
                result = new Pm(ArrayMessage[0],ArrayMessage[1],ArrayMessage[2]); break;

            case 7://Logstat
                result = new Logstas(); break;

            case 8://Stat
                result = new Stat(ArrayMessage[0]); break;

            case 12://Block
                result = new Block(ArrayMessage[0]); break;
        }
        //Init:
        len=0;
        return result;
    }

}

