package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

import java.util.Vector;

public class Pm extends Message {

    private final String userName;
    private String content;
    private final String sendingDateAndTime;
    private String[] forbiddenWords = {"Trump", "War"};


    public Pm(String userName, String content, String sendingDateAndTime) {
        super((short) 6);
        this.userName = userName;
        this.content = content;
        this.sendingDateAndTime = sendingDateAndTime;
        filterContent();
    }


    //Getters
    public String getUserName() {
        return userName;
    }

    public String getSendingDateAndTime() {
        return sendingDateAndTime;
    }

    public String getFilteredContent() {
        return content;
    }

    public void filterContent() {
        content = " " + content + " ";
        for (String word : forbiddenWords) {
            content = content.replaceAll(" " + word + " ", " <filtered> ");
            content = content.replaceAll(" " + word + ",", " <filtered>");
            content = content.replaceAll(" " + word + "!", " <filtered>");
            content = content.replaceAll(" " + word + ".", " <filtered>");
            content = content.replaceAll(" " + word + "?", " <filtered>");
        }
        content = content.substring(1, content.length() - 1);
    }
//        for (int i = 0; i < forbiddenWords.length; i++) {//Runs on all forbidden words
//            String bidWord = forbiddenWords[i];
//            content = content.replace(bidWord, "<filtered>");
//        }
}
