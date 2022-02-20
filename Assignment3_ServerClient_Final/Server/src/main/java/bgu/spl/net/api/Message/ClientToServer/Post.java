package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Post extends Message {

    private final String content;//The message may contain @<username> in order to send it to specific users other then those following the poster.
    private Vector<String> names = new Vector<>();


    public Post(String content) {
        super((short) 5);
        this.content = content;
        setNames();

    }

    //Getters
    public String getContent() {
        return content;
    }

    public Vector<String> getNames() {
        return names;
    }

    private void setNames() {
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '@') {
                int startIndForSubstring = i + 1;
                String acc = "";
                while (startIndForSubstring < content.length() && content.charAt(startIndForSubstring) != ' ') {
                    acc += content.charAt(startIndForSubstring);
                    startIndForSubstring++;
                }
                if (!acc.equals(""))
                    names.add(acc);
            }
        }
    }

}

