package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.Future;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    //Fields
    private Student student;
    private Vector<Model> modelVector;


    //Constructor
    public StudentService(Student student, Vector<Model> modelsVector) {
        super(student.getName());
        this.student = student;
        this.modelVector = modelsVector;
    }

    //Getters

    public Vector<Model> getModelVector() {
        return modelVector;
    }

    public Student getStudent() {
        return student;
    }

    @Override
    //Subscribe to TickBroadcast
    //Subscribe to PublishConferenceBroadcast
    //Send TrainModelEvent
    //Send TestModelEvent
    //Send PublishResultEvent
    protected void initialize() {

        //Subscribe to Tick Broadcast
        subscribeBroadcast(TickBroadcast.class, message -> {

            //Terminate the MS activity
            if (message.isProgramFinished()) {
                terminate();
            }
        });

        //Subscribe to Publish Conference Broadcast
        subscribeBroadcast(PublishConfrenceBroadcast.class, message -> {
            Vector<Model> modelNames = message.getModelNames();
            for (Model modelName : modelNames) {

                //Check if the model name belong to the student's models
                boolean flag = false;
                for (int i = 0; i < modelVector.size() && !flag; i++) {
                    if (modelName.getStudent().equals(modelVector.elementAt(i).getStudent())) {
                        flag = true;
                    }
                }
                //If its not belong, increase the number of the paper read
                if (!flag) {
                    student.setPapersRead(student.getPapersRead() + 1);
                }
                //If its belong, increase the number of the publications
                else {
                    student.setPublications(student.getPublications() + 1);
                }
            }
        });
        boolean programFinished = false;
        //Start to send models in loops - go over all the student's models
        Iterator<Model> iter = modelVector.iterator();
        while (iter.hasNext() && !programFinished) {
            Model pretrained_model = iter.next();
            TrainModelEvent tme = new TrainModelEvent(pretrained_model);
            Future<Model> future = sendEvent(tme);
//            Model trained_model = future.get((500+ts.getDuration() - ts.getCurrTick())*ts.getSpeed(), TimeUnit.MILLISECONDS );
            Model trained_model = future.get();
            if (trained_model != null) {
                //Send Test model
                TestModelEvent testModelEvent = new TestModelEvent(trained_model);
                future = sendEvent(testModelEvent);
//                Model tested_model = future.get((500+ts.getDuration() - ts.getCurrTick())*ts.getSpeed(), TimeUnit.MILLISECONDS );
                Model tested_model = future.get();

                if (tested_model != null) {
                    //If the result is good, send publish event
                    if (tested_model.getResults() == Model.Results.Good) {
                        //TODO: maybe send also the model???
                        PublishResultsEvent pre = new PublishResultsEvent(tested_model);
                        Future<Model> stringFuture = sendEvent(pre);
//                        Model modelName = stringFuture.get((500+ts.getDuration() - ts.getCurrTick())*ts.getSpeed(), TimeUnit.MILLISECONDS );
                        Model modelName = stringFuture.get();
                        if (modelName == null) {
                            programFinished = true;
                        }
                    }
                } else {
                    programFinished = true;
                }
            } else {
                programFinished = true;
            }
        }
    }
}
