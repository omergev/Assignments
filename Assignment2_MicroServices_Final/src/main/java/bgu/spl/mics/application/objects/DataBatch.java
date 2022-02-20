package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {

    //Fields
    private Data data;
    private int start_index;
    private GPU gpu;

    //Constructor
    public DataBatch(Data data, int start_index, GPU gpu) {
        this.data = data;
        this.start_index = start_index;
        this.gpu = gpu;
    }

    //Getters
    public Data getData() {
        return data;
    }
    public int getStart_index() {
        return start_index;
    }
    public GPU getGpu() {
        return gpu;
    }


    //Setters
    public void setData(Data data) {
        this.data = data;
    }

    public void setStart_index(int start_index) {
        this.start_index = start_index;
    }

    public void setGpu(GPU gpu) {
        this.gpu = gpu;
    }
}
