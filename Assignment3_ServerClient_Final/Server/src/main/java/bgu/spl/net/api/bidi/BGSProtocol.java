package bgu.spl.net.api.bidi;

import bgu.spl.net.api.DataBase;
import bgu.spl.net.api.Message.ClientToServer.*;
import bgu.spl.net.api.Message.Message;
import bgu.spl.net.api.Message.ServerToClient.Ack;
import bgu.spl.net.api.Message.ServerToClient.Error;
import bgu.spl.net.api.Message.ServerToClient.Notification;
import bgu.spl.net.api.User;

import java.util.Iterator;
import java.util.Vector;

public class BGSProtocol<T> implements BidiMessagingProtocol<T> {

    private DataBase dataBase = DataBase.getInstance();
    private ConnectionsImpl<Message> connections;
    private int connectionHandlerId;
    private User user = null;
    private boolean shouldTerminate = false;

    @Override
    public void start(int connectionId, Connections<T> connections) {
        connectionHandlerId = connectionId;
        this.connections = (ConnectionsImpl<Message>) connections;
    }

    @Override
    public void process(T message) {
        short opcode = ((Message) message).getOpcode();
//        System.out.println(opcode);
        switch (opcode) {
            case 1: //Register
                synchronized (dataBase.getRegisterLock()) {
                    Register register = (Register) message;
                    if (dataBase.getUsersHashMap().containsKey(register.getUserName()))//Case of Error
                        connections.send(connectionHandlerId, new Error(opcode));
                    else {//Put in the UsersHashMap and send Ack
                        User registerUser = new User(register.getUserName(), register.getPassword(), register.getBirthday());
                        dataBase.getUsersHashMap().putIfAbsent(register.getUserName(), registerUser);
                        connections.send(connectionHandlerId, new Ack(opcode));
                    }
                }
                break;

            case 2: //Login
                Login login = (Login) message;
                User currUser = dataBase.getClient(login.getUserName());
                //Error if: 1.the user doesn't exist
                // 2.the password doesn't match the one entered for the username
                // 3.if the captcha is 0.
                if (currUser == null || user != null || currUser.isLoggedIn() || !(currUser.getPassword().equals(login.getPassword())) || login.getCaptcha() == 0) {
                    connections.send(connectionHandlerId, new Error(opcode));
                } else {
                    user = currUser;
                    user.setConHId(connectionHandlerId);
                    connections.send(connectionHandlerId, new Ack(opcode));
                    while (user.getWaitingNotifications().size() > 0) {
                        Notification currNotification = user.getWaitingNotifications().poll();
                        connections.send(connectionHandlerId, currNotification);
                    }
                }
                break;
            case 3: //Logout

                if (user == null || !user.isLoggedIn()) { //Send error when user is not logged in
                    connections.send(connectionHandlerId, new Error(opcode));
                } else {
                    synchronized (user) {
                        user.setConHId(-1);
                        shouldTerminate = connections.send(connectionHandlerId, new Ack(opcode));
                        user = null;
                        connections.disconnect(connectionHandlerId);
                        connections.send(connectionHandlerId, new Ack(opcode));
                    }
                }
                break;
            case 4: // Follow
                FollowUnfollow followUnfollow = (FollowUnfollow) message;
                int follow = followUnfollow.getFollow();
                String followName = followUnfollow.getUserName();
                User tempUser = dataBase.getClient(followName);

                //Conditions to send Error message:
                // 1. User is not logged in
                // 2. The user I want to follow is blocking me
                // 3. Im blocking the user i want to follow
                // 4. I already follow the user i want to follow
                // 5. Im already not following the user i want to unfollow
                // 6. follow to myself

                //Check condition 1
                if (user == null || !user.isLoggedIn() || tempUser == null || user.getUsername().equals(followName)) {
                    connections.send(connectionHandlerId, new Error(opcode));
                }
                //Follow
                else if (follow == 0) {
                    User blockingUser1 = user.getBlocking().get(followName);
                    User blockingUser2 = dataBase.getClient(followName).getBlocking().get(user.getUsername());
                    User followingUser = user.getFollowing().get(followName);
                    //Check condition 2-4
                    if (blockingUser1 != null || blockingUser2 != null || followingUser != null) {
                        connections.send(connectionHandlerId, new Error(opcode));
                    } else {
                        //Add to the following & followers lists and send ack msg.
                        user.getFollowing().putIfAbsent(followName, dataBase.getClient(followName));
                        dataBase.getClient(followName).getFollowers().putIfAbsent(user.getUsername(), user);
                        connections.send(connectionHandlerId, new Ack(followName));
                    }

                }
                //Unfollow
                else {
                    //Check condition 5
                    User followingUser = user.getFollowing().get(followName);
                    if (followingUser == null) {
                        connections.send(connectionHandlerId, new Error(opcode));
                    } else {
                        //Remove following & followers lists and send ack msg.
                        user.getFollowing().remove(followName);
                        dataBase.getClient(followName).getFollowers().remove(user.getUsername());
                        connections.send(connectionHandlerId, new Ack(followName));
                    }
                }
                break;

            case 5: //Post
                //Check if the user is logged in
                if (user == null || !user.isLoggedIn()) {
                    connections.send(connectionHandlerId, new Error(opcode));
                } else {

                    user.setNumOfPost(user.getNumOfPost() + 1);
                    Post post = (Post) message;
                    String content = post.getContent();
                    Vector<String> names = post.getNames();
                    for (String n : names) {
                        if (!dataBase.getUsersHashMap().containsKey(n)) {
                            connections.send(connectionHandlerId, new Error(opcode));
                            return;
                        }
                    }
                    Notification noti = new Notification((short) 1, user.getUsername(), content);

                    //Go over all the followers list and send the post msg.
                    Iterator iter = user.getFollowers().values().iterator();
                    while (iter.hasNext()) {
                        User followUser = (User) iter.next();
                        //Check if the users are not blocking each other
                        if (followUser.getBlocking().get(user.getUsername()) == null && user.getBlocking().get(followUser.getUsername()) == null) {
                            //Save all the post/PM msg in data structure
                            user.getSavedMessages().add(post);
                            // If the user is logged in send the notification right now
                            if (followUser.isLoggedIn()) {
                                connections.send(followUser.getConHId(), noti);
                            }
                            //If the user is not logged in save the notification in the notification queue
                            else {
                                synchronized (followUser) {
                                    followUser.getWaitingNotifications().add(noti);
                                }
                            }
                        }
                    }

                    // Go over all the users that mentioned in the content of the post msg. (@username)
                    for (String s : names) {
                        User contentUser = dataBase.getClient(s);
                        //Check if the user is register and check if the users are not blocking each other and check if the user is already on my followers list
                        if (contentUser != null && contentUser.getBlocking().get(user.getUsername()) == null && user.getBlocking().get(s) == null && !user.getFollowers().contains(contentUser)) {
                            //Save all the post/PM msg in data structure
                            contentUser.getSavedMessages().add(post);
                            if (contentUser.isLoggedIn()) {
                                connections.send(contentUser.getConHId(), noti);
                            } else {
                                synchronized ((contentUser)) {
                                    contentUser.getWaitingNotifications().add(noti);
                                }
                            }
                        }
                    }
                    connections.send(connectionHandlerId, new Ack(opcode));
                }
                break;
            case 6:
                Pm pm = (Pm) message;
                String filteredContent = pm.getFilteredContent();
                String receiverName = pm.getUserName();
                //Check if the sender is logged in
                if (user == null || !user.isLoggedIn()) {
                    connections.send(connectionHandlerId, new Error(opcode));
                } else {
                    User receiverUser = dataBase.getClient(receiverName);
                    //Check if the receiver is registered and also if the sender/receiver is blocking each other, the sender is following the receiver
                    if (receiverName != null && !receiverUser.getBlocking().containsKey(user.getUsername()) && !user.getBlocking().containsKey(receiverName) && user.getFollowing().contains(receiverUser)) {

                        //Save the PM in receiver history
                        Notification noti = new Notification((short) 0, user.getUsername(), filteredContent);
                        receiverUser.getSavedMessages().add(pm);
                        //If the receiver is logged in, send notification
                        if (receiverUser.isLoggedIn()) {
                            connections.send(receiverUser.getConHId(), noti);
                        }
                        //If the receiver is not logged in, save the notification in queue
                        else {
                            synchronized (receiverUser) {
                                receiverUser.getWaitingNotifications().add(noti);
                            }
                        }
                        connections.send(connectionHandlerId, new Ack(opcode));
                    } else {
                        connections.send(connectionHandlerId, new Error(opcode));
                    }
                }

                break;

            case 7: //LOGSTAT
                if (user == null || !user.isLoggedIn()) {
                    connections.send(connectionHandlerId, new Error(opcode));
                }
                Iterator iter = dataBase.getUsersHashMap().values().iterator();
                while (iter.hasNext()) {
                    User iterUser = (User) iter.next();
                    if (iterUser.isLoggedIn() && !iterUser.getBlocking().containsKey(user.getUsername()) && !user.getBlocking().containsKey(iterUser.getUsername())) {
                        int age = iterUser.getAge();
                        int numOfPost = iterUser.getNumOfPost();
                        int numOfFollower = iterUser.getOfNumOfFollowers();
                        int numOfFollowing = iterUser.getOfNumOfFollowing();
                        connections.send(connectionHandlerId, new Ack(opcode, age, numOfPost, numOfFollower, numOfFollowing));
                    }
                }
                break;
            case 8: // STAT
                if (user == null || !user.isLoggedIn()) {
                    connections.send(connectionHandlerId, new Error(opcode));
                } else {
                    Stat stat = (Stat) message;
                    String[] userNames = stat.getUserNames();
                    boolean foundNotRegister = false;
                    //Search for any not register users from users names given.
                    for (String name : userNames) {
                        User statUser = dataBase.getClient(name);
                        if (statUser == null || statUser.getBlocking().containsKey(user.getUsername()) || user.getBlocking().containsKey(name)) {
                            foundNotRegister = true;
                            connections.send(connectionHandlerId, new Error(opcode));
                            break;
                        }
                    }
                    //Send all the ack messages if all the users are already registered
                    if (!foundNotRegister) {
                        for (String name : userNames) {
                            User statUser = dataBase.getClient(name);

                            int age = statUser.getAge();
                            int numOfPost = statUser.getNumOfPost();
                            int numOfFollower = statUser.getOfNumOfFollowers();
                            int numOfFollowing = statUser.getOfNumOfFollowing();
                            connections.send(connectionHandlerId, new Ack(opcode, age, numOfPost, numOfFollower, numOfFollowing));
                        }
                    }
                }

                break;
            case 12:
                Block block = (Block) message;
                String blockName = block.getUserName();
                User blockUser = dataBase.getClient(blockName);
                //Check if the user is register and logged in, and if the block user is register
                if (user == null || !user.isLoggedIn() || blockUser == null || user.getUsername().equals(blockName)) {
                    connections.send(connectionHandlerId, new Error(opcode));
                }
                //Check if the user is already blocking the 'blocked user'
                else {
                    if (user.getBlocking().get(blockName) == null) {
                        //Put the blocked user on the blocking list
                        user.getBlocking().putIfAbsent(blockName, blockUser);

                        //Remove the blocking and blocked users from each other followers lists.
                        if (user.getFollowers().contains(blockUser)) {
                            user.getFollowers().remove(blockName);
                            blockUser.getFollowing().remove(user.getUsername());
                        }
                        if (user.getFollowing().contains(blockUser)) {
                            user.getFollowing().remove(blockName);
                            blockUser.getFollowers().remove(user.getUsername());
                        }
                    }
                    connections.send(connectionHandlerId, new Ack(opcode));
                }
                break;

        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
