package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import bgu.spl.mics.application.objects.Data.Type;


/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    //Fields
    private int numberOfTicksForProcess = 0; // Number of ticks takes to process one batch depends on his type
    private int tickCounter = 0; // Counter ticks in order to process one batch
    private boolean processing = false;
    private Type dataType = null;
    private int cores = 0;
    private Vector<DataBatch> data = null; // All the unprocessed batches (no store limit)
    private Cluster cluster = null;
    private int totalNumberOfProcessed_DB = 0;
    private int totalNumberOfTickUsed = 0;



    //Constructor
    public CPU(int numOfCores) {
        setNumOfCores(numOfCores);
    }

    //Getters

    public int getNumOfCores() {
        return cores;
    }

    public Type getDataType() {
        return dataType;
    }

    public int getTotalNumberOfProcessed_DB() {
        return totalNumberOfProcessed_DB;
    }

    public Vector<DataBatch> getData() {
        return data;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public int getTotalNumberOfTickUsed() {
        return totalNumberOfTickUsed;
    }


    //Setters


    public void setNumOfCores(int numOfCores) {
        this.cores = numOfCores;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }

    public void setData(Vector<DataBatch> data) {
        this.data = data;
        setProcessing(true);
        setDataType(data.elementAt(0).getData().getType());
        if (getDataType() == Type.Images) {
            numberOfTicksForProcess = 32 / getNumOfCores() * 4;
        } else if (getDataType() == Type.Text) {
            numberOfTicksForProcess = 32 / getNumOfCores() * 2;
        } else {
            numberOfTicksForProcess = 32 / getNumOfCores();
        }
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public void setTickCounterToZero() {
        this.tickCounter = 0;
    }

    public void setNumberOfTicksForProcess(int numberOfTicksForProcess) {
        this.numberOfTicksForProcess = numberOfTicksForProcess;
    }

    public void setDataType(Type type) {
        this.dataType = type;
    }


    //Queries


    //Check if the CPU is processing batches right now.
    public boolean isProcessingRightNow() {
        return processing;
    }

    //Check if done processing a single batch from storage
    private boolean isDoneProcessingSingleBatch() {
        return tickCounter == numberOfTicksForProcess;
    }


    // CPU - methods


    //Called when finished processing single batch. update processed batches, init tick-counter.
    private void processBatch() {
        totalNumberOfProcessed_DB++;

        send_DB(data);
        setTickCounterToZero();
        setDataType(null);
        setNumberOfTicksForProcess(0);
        setProcessing(false);
        data = null;
        receive_DB();

    }


    public void updateTick() {
        if (data != null && !data.isEmpty()) {
            tickCounter++;
            totalNumberOfTickUsed++;
            if (isDoneProcessingSingleBatch()) {
                processBatch();
            }
        }
    }

    public void receive_DB() {
        Thread t_receive = new Thread(() -> {
            //No arguments , return Vector<DataBatch>
            Vector<DataBatch> vec= getCluster().receiveUnprocessedDB();
            if(vec != null){
                setData(vec);
            }

        });
        t_receive.start();

    }

    public void send_DB(Vector<DataBatch> vec) {
        Thread t_send = new Thread(() -> {
            getCluster().sendProcessedDB(vec);
        });
        t_send.start();


    }
//    private Vector<DataBatch> DBclone(){
//        Vector<DataBatch> vec = new Vector<>();
//        DataBatch db = getData().elementAt(0);
//        DataBatch clone_db = new DataBatch(db.getData(), db.getStart_index(),db.getGpu() );
//        vec.add(clone_db);
//        return vec;
//    }


    //TODO: check if the methods necessary

//    private void clear(){
//        setTickCounterToZero();
//        setDataType(null);
//        setNumberOfTicksForProcess(0);
//        setNumOfProcessedBatches(0);
//        setProcessing(false);
//        setData(null);
//    }

    //receive unprocessed batches from cluster

//    public void receiveUnprocessedBatches(Collection<DataBatch> receivedBatches){
//    }

    //Send the process batches back to cluster

//    public void sendCPUprocessedBatches(Collection<DataBatch> unprocessedBatches){
//    }

}
