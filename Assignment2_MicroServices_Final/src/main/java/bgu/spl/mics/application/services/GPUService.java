package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;


import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    //Fields
    final private GPU gpu;
    private ConcurrentLinkedQueue<TrainModelEvent> GPU_service_queue;
    private boolean isTrainingFinished = false;

    //Constructor
    public GPUService(GPU gpu, String name) {
        super(name);
        this.gpu = gpu;
    }


    @Override
    //Subscribe to tickBroadcast
    //Subscribe to TrainModelEvent
    //Subscribe to TestModelEvent
    protected void initialize() {
        GPU_service_queue = new ConcurrentLinkedQueue<>();

        //Subscribe to Tick Broadcast
        subscribeBroadcast(TickBroadcast.class, message -> {

            //Check if terminate MS activity
            if (message.isProgramFinished()) {
//                gpu.getCluster().releaseLocks();
                terminate();
            } else {
                isTrainingFinished = gpu.updateTick();

                //Check if the gpu has finished to train the current Train-Model
                if (isTrainingFinished) {
                    //Send complete
                    TrainModelEvent ev = GPU_service_queue.poll();
                    Model model = ev.getModel();
                    model.setStatus(Model.Status.Trained);
                    complete(ev, model);

                    //Check if there is another Train-Model waiting in the queue
                    if (!GPU_service_queue.isEmpty()) {
                        GPU_service_queue.peek().getModel().setStatus(Model.Status.Training);
                        assert GPU_service_queue.peek() != null;
                        gpu.initTrainModelEvent(GPU_service_queue.peek().getModel());
                    }
                }
            }
        });

        //Subscribe to Train-Event
        subscribeEvent(TrainModelEvent.class, ev -> {

            //Save the event in train-events queue
            GPU_service_queue.add(ev);

            //Check if the gpu is already processing right now
            if (!gpu.isProcessing()) {
                ev.getModel().setStatus(Model.Status.Training);
                gpu.initTrainModelEvent(ev.getModel());
            }


        });


        //Subscribe to Test-Event
        subscribeEvent(TestModelEvent.class, ev -> {
            Model model = ev.getModel();
            Random rand = new Random();
            //Generate int number from 0 to 9
            int randomInt = rand.nextInt(10);
            if (model.getStudent().getStatus() == Student.Degree.MSc) {
                //Probability : 0.6 (0 - 5)
                if (randomInt <= 5) {
                    model.setStatus(Model.Status.Tested);
                    model.setResults(Model.Results.Good);
                }
                //Probability : 0.4 (6 - 9)
                else {
                    model.setStatus(Model.Status.Tested);
                    model.setResults(Model.Results.Bad);
                }
            } else {
                //Probability : 0.8 (0 - 7)
                if (randomInt <= 7) {
                    model.setStatus(Model.Status.Tested);
                    model.setResults(Model.Results.Good);
                }
                //Probability : 0.2 (8 - 9)
                else {
                    model.setStatus(Model.Status.Tested);
                    model.setResults(Model.Results.Bad);
                }
            }
            complete(ev, model);
        });
    }
}
