package bgu.spl.net.api;

import java.util.concurrent.ConcurrentHashMap;

public class DataBase {

    private ConcurrentHashMap <String , User> usersHashMap;
    private Object registerLock;

    //Getter
    public ConcurrentHashMap<String, User> getUsersHashMap() {
        return usersHashMap;
    }

    //Singleton
    private static class DataBaseHolder {
        private static DataBase instance = new DataBase();
    }
    private DataBase(){
        usersHashMap = new ConcurrentHashMap<>();
        registerLock = new Object();
    }

    public Object getRegisterLock() {
        return registerLock;
    }

    public static DataBase getInstance() {
        return DataBaseHolder.instance;
    }

    public User getClient(String username){
        return usersHashMap.get(username);
    }

}
