package bgu.spl.net.api;

import bgu.spl.net.api.Message.Message;
import bgu.spl.net.api.Message.ServerToClient.Notification;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class User {
    private final String username;
    private final String password;
    private final String birthday;
    private ConcurrentHashMap<String, User > following = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, User > followers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, User > blocking = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Notification> waitingNotifications = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Message> savedMessages = new ConcurrentLinkedQueue<>();
    private int numOfPost=0;
    private int age;
    int conHId=-1;

    public User(String username, String password, String birthday) {
        this.username = username;
        this.password = password;
        this.birthday = birthday;
        age = calculatingAge();
    }


    //Getters
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getBirthday() {
        return birthday;
    }

    public ConcurrentHashMap<String, User> getFollowing() {
        return following;
    }

    public ConcurrentHashMap<String, User> getFollowers() {
        return followers;
    }

    public ConcurrentLinkedQueue<Notification> getWaitingNotifications() {
        return waitingNotifications;
    }
    public ConcurrentLinkedQueue<Message> getSavedMessages() {
        return savedMessages;
    }
    public int getNumOfPost() {
        return numOfPost;
    }
    public int getAge() {
        return age;
    }
    public int getConHId() {
        return conHId;
    }
    public ConcurrentHashMap<String, User> getBlocking() {
        return blocking;
    }

    public int getOfNumOfFollowers () { return getFollowers().size();}
    public int getOfNumOfFollowing () { return getFollowing().size();}

    public int calculatingAge () {
        int birthYear= Integer.parseInt(birthday.substring(6));
        return 2022 - birthYear;
    }

    //Setters
    public void setConHId(int conHId) {
        this.conHId = conHId;
    }

    public void setNumOfPost(int numOfPost) {
        this.numOfPost = numOfPost;
    }

    public boolean isLoggedIn(){
        return -1 != getConHId();
    }
}
