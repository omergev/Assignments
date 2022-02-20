
package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.example.messages.ExampleBroadcast;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {

    //Fields
    private CPU cpu;


    //Constructor
    public CPUService(CPU cpu, String name) {
        super(name);
        this.cpu = cpu;
    }

    @Override
    //Subscribe to tickBroadcast
    protected void initialize() {
        cpu.receive_DB();
        // TODO: Implement this
        subscribeBroadcast(TickBroadcast.class, message -> {
            //Check if to terminate the MS activity
            if(message.isProgramFinished()){
                terminate();
            }
            cpu.updateTick();

        });
    }
}
