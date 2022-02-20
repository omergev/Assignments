package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class PublishResultsEvent implements Event<Model> {

    final private Model modelName;

    public PublishResultsEvent(Model modelName) {
        this.modelName = modelName;
    }

    public Model getModelName() {
        return modelName;
    }
}
