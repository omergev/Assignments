package bgu.spl.mics;

import org.junit.Before;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {


	private ConcurrentHashMap<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> eventHashMap;
	private ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastHashMap;
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> messageQueue;
	private ConcurrentHashMap<Event, Future> futureHashMap;
	final private Object lockForSendBroad;
	final private Object lockForSendEvent;

	private MessageBusImpl() {
		eventHashMap = new ConcurrentHashMap<>();
		broadcastHashMap = new ConcurrentHashMap<>();
		messageQueue = new ConcurrentHashMap<>();
		futureHashMap = new ConcurrentHashMap<>();
		lockForSendEvent = new Object();
		lockForSendBroad = new Object();
	}

	private static class MessageBusImplHolder {
		private static volatile MessageBusImpl instance = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() {
		return MessageBusImplHolder.instance;
	}


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// TODO Auto-generated method stub
		if (messageQueue.containsKey(m)) {
			synchronized ((lockForSendEvent)) { //For the case where 2 threads will add the same element twice.
				eventHashMap.putIfAbsent(type, new ConcurrentLinkedQueue<>());
				eventHashMap.get(type).add(m);
				lockForSendEvent.notifyAll();
			}
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// TODO Auto-generated method stub
		if (messageQueue.containsKey(m)) {
			synchronized (lockForSendBroad) {
				broadcastHashMap.putIfAbsent(type, new ConcurrentLinkedQueue<>());
				broadcastHashMap.get(type).add(m);
			}
		}

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub
		Future f = futureHashMap.get(e);//find the Future that match to e
		if(f != null){
			f.resolve(result);
		}
		futureHashMap.remove(e,f);


	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub
		if (broadcastHashMap.containsKey(b.getClass())) {//if broadcastHashMap.key is not empty
			synchronized (lockForSendBroad) {
				ConcurrentLinkedQueue<MicroService> queue = broadcastHashMap.get(b.getClass());
				for (MicroService m : queue) {//add the broadcast b to all the elements in the queue.
					LinkedBlockingQueue<Message> tempQueue = messageQueue.get(m);
					if(tempQueue != null){
						tempQueue.add(b);
					}
				}
			}
		}
	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		synchronized (lockForSendEvent) {
			//TODO: change to 'not'
			while (!eventHashMap.containsKey(e.getClass())) {
				try {
					lockForSendEvent.wait();
				} catch (InterruptedException ingored) {
				}
			}
			//rubinRound

			MicroService m = eventHashMap.get(e.getClass()).poll();
			LinkedBlockingQueue<Message> tempQueue = messageQueue.get(m);
			if(tempQueue != null){
				tempQueue.add(e);
			}

			ConcurrentLinkedQueue<MicroService> tempQueue2 = eventHashMap.get(e.getClass());
			if(tempQueue2 != null){
				tempQueue2.add(m);
			}
			Future<T> f = new Future<>();
			futureHashMap.put(e, f);
			return f;
		}
	}


	@Override
	public void register(MicroService m) {
		messageQueue.putIfAbsent(m, new LinkedBlockingQueue<Message>());
	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub
		if(isRegistered(m)){
			if(m.getName().equals( "TimeService")){
				resolveFutures();
			}
			if (messageQueue.containsKey(m)) {//check if m is register
				messageQueue.remove(m);
				Collection<ConcurrentLinkedQueue<MicroService>> eventQueue = eventHashMap.values();
				for (ConcurrentLinkedQueue<MicroService> queue : eventQueue) {
					queue.remove(m);
				}
				Collection<ConcurrentLinkedQueue<MicroService>> broadcastQueue = broadcastHashMap.values();
				for (ConcurrentLinkedQueue<MicroService> queue : broadcastQueue) {
					queue.remove(m);
				}
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (!messageQueue.containsKey(m)) {
			throw new IllegalArgumentException("MicroService m does not register to the system");
		}
		Message message = messageQueue.get(m).take();
		return message;
	}

	//Queries

	public boolean isRegistered(MicroService m) {
		return messageQueue.containsKey(m);
	}
	private void resolveFutures(){
		Iterator<Future> iter =futureHashMap.values().iterator();
		while(iter.hasNext()){
			try{
				iter.next().resolve(null);
			} catch (Exception e){
				System.out.println("Couldn't resolve Future in ResolveFutures");
			}

		}
	}
}

//package bgu.spl.mics;
//
//import java.util.*;
//import java.util.concurrent.*;
///**
// * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
// * Write your implementation here!
// * Only private fields and methods can be added to this class.
// */
//
//public class MessageBusImpl implements MessageBus {
//	private ConcurrentHashMap<MicroService, BlockingQueue<Message>> serviceMessagesQueue; //messages for each service
//	private ConcurrentHashMap<Class<? extends Event>,BlockingQueue<MicroService>> eventService;		//services for each event
//	private ConcurrentHashMap<Class<? extends Broadcast>, BlockingQueue<MicroService>> broadcastService;	//services for each broadcast
//	private ConcurrentHashMap<Event,Future> futureService;		//future objects
//
//	private ConcurrentHashMap<MicroService, BlockingQueue<Class <? extends Event>>> subEventTypes; //list of type of events ms is subscribed to
//	private ConcurrentHashMap<MicroService, BlockingQueue<Class <? extends Broadcast>>> subBroadcastTypes; //list of type of broadcasts ms is subscribed to
//
//	private static class MessageBusHolder {
//		private static final MessageBusImpl instance = new MessageBusImpl();
//	}
//
//	private MessageBusImpl(){
//		serviceMessagesQueue = new ConcurrentHashMap<>();
//		eventService = new ConcurrentHashMap<>();
//		broadcastService = new ConcurrentHashMap<>();
//		futureService = new ConcurrentHashMap<>();
//		subEventTypes = new ConcurrentHashMap<>();
//		subBroadcastTypes = new ConcurrentHashMap<>();
//	}
//
//	public static MessageBusImpl getInstance(){
//		return MessageBusHolder.instance;
//	}
//
//
//	//TODO possible add synchronize
//	@Override
//	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
//		eventService.putIfAbsent(type, new LinkedBlockingQueue<>());
//		if (!eventService.get(type).contains(m)){
//			eventService.get(type).add(m);
//			subEventTypes.get(m).add(type);
//		}
//	}
//
//	@Override
//	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
//		broadcastService.putIfAbsent(type, new LinkedBlockingQueue<>());
//		if(!broadcastService.get(type).contains(m)){
//			broadcastService.get(type).add(m);
//			subBroadcastTypes.get(m).add(type);
//		}
//	}
//
//	@Override
//	public <T> void complete(Event<T> e, T result) {
//		Future f = futureService.get(e);
//		if (f!=null)
//			f.resolve(result);
//		futureService.remove(e,f);
//	}
//
//	@Override
//	public void sendBroadcast(Broadcast b) {
//		BlockingQueue<MicroService> subMs = broadcastService.get(b.getClass());
//		if (subMs != null) {
//			for (MicroService m: subMs) {
//				BlockingQueue<Message> messages = serviceMessagesQueue.get(m);
//				if(messages!=null){
//					messages.add(b);
//				}
//			}
//		}
//	}
//
//
//	@Override
//	public <T> Future<T> sendEvent(Event<T> e) {
//		Future<T> future = new Future<>();
//		BlockingQueue<MicroService> subMs = eventService.get(e.getClass());
//		if(subMs!=null){
//			synchronized (subMs){
//				MicroService m = subMs.poll();
//				if(m!=null){
//					serviceMessagesQueue.get(m).add(e);
//					futureService.put(e, future);
//					subMs.add(m);
//				}
//			}
//		}
//		return future;
//	}
//
//
//	@Override
//	public void register(MicroService m) {
//		if(!isRegistered(m)){
//			BlockingQueue b = new LinkedBlockingQueue();
//			serviceMessagesQueue.put(m,b);
//		}
//		subEventTypes.put(m, new LinkedBlockingQueue<>());
//		subBroadcastTypes.put(m,new LinkedBlockingQueue<>());
//	}
//
//
//	@Override
//	public void unregister(MicroService m) {
//		if(isRegistered(m)){
//			if(m.getName().equals("TimeService")){
//				completeAll();
//			}
//			serviceMessagesQueue.remove(m);
//			BlockingQueue<Class<? extends Event>> eTypes = subEventTypes.get(m);
//			BlockingQueue<Class<? extends Broadcast>> bTypes = subBroadcastTypes.get(m);
//			for (Class<? extends Event> c: eTypes) {
//				synchronized (eventService.get(c)){
//					eventService.get(c).remove(m);
//				}
//			}
//			for (Class<? extends Broadcast> c: bTypes) {
//				synchronized (broadcastService.get(c)){
//					broadcastService.get(c).remove(m);
//				}
//			}
//			subEventTypes.remove(m);
//			subBroadcastTypes.remove(m);
//		}
//	}
//
//	@Override
//	public Message awaitMessage(MicroService m) throws InterruptedException {
//		if (!isRegistered(m))
//			throw new IllegalStateException("micro service: '" + m.getName() + "' wasn't registered");
//		return serviceMessagesQueue.get(m).take();
//	}
//
//
//	public boolean isRegistered(MicroService m){
//		return (m!=null && serviceMessagesQueue.get(m)!=null);
//	}
//
//
//	//TODO change function
//	private void completeAll(){
//		Iterator<Future> iter = futureService.values().iterator();
//		while(iter.hasNext()){
//			iter.next().resolve(null);
//		}
//	}
//
//}
