package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

public class Register extends Message {

    private final String userName;
    private final String password;
    private final String birthday;


    public Register(String userName, String password, String birthday) {
        super((short) 1);
        this.userName = userName;
        this.password = password;
        this.birthday = birthday;
    }

    //Getters
    public String getUserName() {
        return userName;
    }
    public String getPassword() {
        return password;
    }
    public String getBirthday() {
        return birthday;
    }
}
