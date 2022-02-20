package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

import java.util.Vector;

public class PublishConfrenceBroadcast implements Broadcast {

    final private Vector<Model> modelNames;

    public PublishConfrenceBroadcast(Vector<Model> modelNames) {
        this.modelNames = modelNames;
    }

    public Vector<Model> getModelNames() {
        return modelNames;
    }
}
