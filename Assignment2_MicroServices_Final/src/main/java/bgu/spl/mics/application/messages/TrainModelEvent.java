package bgu.spl.mics.application.messages;

import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.Event;


public class TrainModelEvent implements Event<Model> {


    //Fields
    private Model model;


    //Constructor
    public TrainModelEvent(Model model){
        setModel(model);
    }

    //Getters
    public Model getModel(){
        return model;
    }

    //Setters
    public void setModel(Model model){
        this.model = model;
    }

}
