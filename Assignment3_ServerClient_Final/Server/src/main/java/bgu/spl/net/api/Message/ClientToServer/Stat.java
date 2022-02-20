package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

import java.util.Vector;

public class Stat extends Message {

    private final String listOfUsernames;//The list of users whose stats will be returned to the client.
    private final String[] userNames;


    public Stat(String listOfUsernames) {
        super((short) 8);
        this.listOfUsernames = listOfUsernames;
         userNames = listOfUsernames.split("\\|");
    }

    //Getters
    public String getListOfUsernames() {
        return listOfUsernames;
    }

    public String[] getUserNames() {
        return userNames;
    }
}
