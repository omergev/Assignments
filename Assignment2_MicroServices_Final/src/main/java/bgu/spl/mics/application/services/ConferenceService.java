package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConfrenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.Vector;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {


    //Fields
    final private int date;
    private int tickCounter = 0;
    final private Vector<Model> modelNames = new Vector<>();

    //Constructor
    public ConferenceService(ConfrenceInformation conference){
        super(conference.getName());
        this.date = conference.getDate();
    }

    //Getters

    public Vector<Model> getModelNames() {
        return modelNames;
    }

    @Override

    //Subscribe to tickBroadcast
    //Subscribe to PublishResultsEvent
    //Send publishConferenceBroadcast
    protected void initialize() {
        //Only if subscribeAt is 0 : subscribe to publish result event
        subscribeEvent(PublishResultsEvent.class, message ->{
            modelNames.add(message.getModelName());
            complete(message, message.getModelName());
        });

        //Subscribe to tick broadcast
        subscribeBroadcast(TickBroadcast.class, message1 -> {
            //Update tick counter
            tickCounter++;

            // Check if need to terminate the MS
            if( tickCounter == date ){
                sendBroadcast(new PublishConfrenceBroadcast(modelNames));
                terminate();
            }
            else if(message1.isProgramFinished()){
                sendBroadcast(new PublishConfrenceBroadcast(modelNames));
                terminate();
            }
        });
    }
}
