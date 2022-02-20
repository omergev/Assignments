package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {

	private int currTick;
	final private int speed;
	final private Timer timer;
    final private TimerTask timerTask;
	private int duration;
//	private ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
//	private Runnable timerTask;



	public TimeService(int duration, int speed) {
		super("TimeService");
		this.duration = duration;
		this.speed = speed;
		this.currTick = 0;
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				currTick++;
				sendBroadcast(new TickBroadcast(currTick, duration));
				if(currTick == duration){
					timerTask.cancel();
					timer.cancel();
				}

			}
		};

	}

	public int getCurrTick() {
		return currTick;
	}

	public int getSpeed() {
		return speed;
	}

	public int getDuration() {
		return duration;
	}
	//	public TimeService(int duration, int speed) {
//		super("TimeService");
//		this.speed = speed;
//		this.currTick = 0;
//		t
//
//        timer = new Timer();
//
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                currTick++;
//                sendBroadcast(new TickBroadcast(currTick, duration));
//                if (currTick == duration){
//                	timerTask.c
//					timer.cancel();
//				}
//
//
//
//            }
//        };
//		timerTask = new Runnable() {
//			public void run() {
//				currTick++;
//				sendBroadcast(new TickBroadcast(currTick, duration));
////				System.out.println("sent");
//				if (currTick == duration)
//					timer.shutdown();
//			}
//		};
//
//	}

	//TODO check this
	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, c -> {
			if (c.isProgramFinished())
				terminate();
		});

//		timer.scheduleAtFixedRate(timerTask, 0, speed, TimeUnit.MILLISECONDS);
        timer.scheduleAtFixedRate(timerTask, 0, speed);
	}


}
