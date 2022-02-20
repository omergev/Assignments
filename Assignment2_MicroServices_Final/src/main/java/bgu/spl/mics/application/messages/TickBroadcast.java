package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {


    //Fields
    private int currentTick = 0;
    private int duration;


    //Constructor
    public TickBroadcast(int currentTick, int duration) {
        this.currentTick = currentTick;
        this.duration = duration;
    }


    //Getters
    public int getCurrentTick() {
        return currentTick;
    }

    public int getDuration() {
        return duration;
    }

    //Setters


    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    //Methods

    public boolean isProgramFinished(){
        return currentTick == duration;
    }
}
