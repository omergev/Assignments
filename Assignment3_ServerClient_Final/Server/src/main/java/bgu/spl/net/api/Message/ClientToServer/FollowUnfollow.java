package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

public class FollowUnfollow extends Message {

    private final String userName;
    private final int follow; //0=follow, 1=unfollow


    public FollowUnfollow(int follow, String userName) {
        super((short) 4);
        this.userName = userName;
        this.follow = follow;
    }

    //Getters
    public String getUserName() {
        return userName;
    }
    public int getFollow() {
        return follow;
    }
}
