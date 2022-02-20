package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.ConnectionHandler;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T>{

    ConcurrentHashMap<Integer, ConnectionHandler<T>> handlersMap = new ConcurrentHashMap<Integer, ConnectionHandler<T>>();

    @Override
    public boolean send(int connectionId, T msg) {
        if(handlersMap.containsKey(connectionId)){
            ConnectionHandler<T> ch = handlersMap.get(connectionId);
            ch.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {
        for (ConnectionHandler<T> ch : handlersMap.values()) {
            ch.send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        if(handlersMap.containsKey(connectionId)){
            handlersMap.remove(connectionId);
        }
    }

    public void register(ConnectionHandler<T> handler, int id) {
        handlersMap.putIfAbsent(id, handler);
    }
}

