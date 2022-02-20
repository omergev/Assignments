package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }

    //Fields
    private Type type;
    private int processed = 0;
    private int size;


    //Constructor -
    public Data(Type type, int size) {
        this.type = type;
        this.size = size;
    }

    //Getters
    public Type getType(){
        return type;
    }
    public int getProcessed(){
        return processed;
    }
    public int getSize(){
        return size;
    }

    //Setters
    public void setType(Type type){
        this.type = type;
    }
    public void setProcessed(int processed){
        this.processed = processed;
    }
    public void setSize(int size){
        this.size = size;
    }
}
