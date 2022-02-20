package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}

    //Fields
    private Type type; // Type of GPU
    private Cluster cluster = null; // Cluster used to send and get batches
    private Model model = null; // The model that used for training/testing
    private final int storageSize; // size of the storage to store CPU-processed batches
    private int tickCounter = 0; // Counter ticks in order to process one batch
    private int numOfProcessedBatches = 0; // Count the number of processed batches in order to finish processing everything
    private Data data = null; // The data that used for training/testing
    private Vector<Vector<DataBatch>> unprocessedBatches = new Vector<>(); // All the unprocessBatches (No store limit)
    private Vector<Vector<DataBatch>> storageCPUprocessedBatches = new Vector<>(); //All the CPU-process batches (with the store limit)
    private int numberOfTicksForProcess; // Number of ticks takes to process one batch depends on his type
    private ConcurrentLinkedQueue<Vector<DataBatch>> unprocessBatchesQueue;
    private ConcurrentLinkedQueue<Vector<DataBatch>> processedBatchesQueue;
    private boolean isProcessing = false;
    private int totalNumberOfTickUsed = 0;


    //Constructor
    public GPU(Type type) {
        this.type = type;
        if (getType() == Type.GTX1080) {
            storageSize = 8;
            numberOfTicksForProcess = 4;
        } else if (getType() == Type.RTX2080) {
            storageSize = 16;
            numberOfTicksForProcess = 2;
        } else {
            storageSize = 32;
            numberOfTicksForProcess = 1;
        }

    }

    //Setters
    public void setModel(Model model) {
        this.model = model;
    }

    public void setType(Type type) {
        this.type = type;
    }


    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public void setTickCounterToZero() {
        tickCounter = 0;
    }

    public void setNumOfProcessedBatches(int num) {
        numOfProcessedBatches = num;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public void setUnprocessedBatches(Vector<Vector<DataBatch>> unprocessedBatches) {
        this.unprocessedBatches = unprocessedBatches;
    }

    public void setUnprocessBatchesQueue(ConcurrentLinkedQueue<Vector<DataBatch>> unprocessBatchesQueue) {
        this.unprocessBatchesQueue = unprocessBatchesQueue;
    }

    public void setNumberOfTicksForProcess(int numberOfTicksForProcess) {
        this.numberOfTicksForProcess = numberOfTicksForProcess;
    }

    public void setUnprocessedBatchesQueue(ConcurrentLinkedQueue<Vector<DataBatch>> unprocessBatchesQueue) {
        this.unprocessBatchesQueue = unprocessBatchesQueue;
    }

    public void setProcessedBatchesQueue(ConcurrentLinkedQueue<Vector<DataBatch>> processedBatchesQueue) {
        this.processedBatchesQueue = processedBatchesQueue;
    }

    //Getters

    public Model getModel() {
        return model;
    }

    public Type getType() {
        return type;
    }

    public int getStorageSize() {
        return storageSize;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public int getNumOfProcessedBatches() {
        return numOfProcessedBatches;
    }

    public Data getData() {
        return data;
    }

    public Vector<Vector<DataBatch>> getUnprocessedBatches() {
        return unprocessedBatches;
    }

    public Vector<Vector<DataBatch>> getStorageCPUprocessedBatches() {
        return storageCPUprocessedBatches;
    }

    public int getNumberOfTicksForProcess() {
        return numberOfTicksForProcess;
    }

    public ConcurrentLinkedQueue<Vector<DataBatch>> getUnprocessBatchesQueue() {
        return unprocessBatchesQueue;
    }

    public ConcurrentLinkedQueue<Vector<DataBatch>> getProcessedBatchesQueue() {
        return processedBatchesQueue;
    }

    public int getTotalNumberOfTickUsed() {
        return totalNumberOfTickUsed;
    }


    //Queries


    //Check if the storage is empty
    public boolean isStorageEmpty() {
        return storageCPUprocessedBatches.isEmpty();
    }

    //Check if the storage is full
    public boolean isStorageFull() {
        return storageCPUprocessedBatches.size() == storageSize;
    }

    //Check if all the batches are processed
    public boolean isEveryThingProcessed() {
        return numOfProcessedBatches == data.getSize() / 1000;
    }

    //Check if done processing a single batch from storage
    private boolean isDoneProceessingSingleBatch() {
        return tickCounter == numberOfTicksForProcess;
    }

    public boolean isStorageValid() {
        return storageCPUprocessedBatches.size() <= storageSize;
    }

    public boolean isProcessing() {
        return isProcessing;
    }


    //Training methods

    //Init trainModelEvent
    public void initTrainModelEvent(Model model) {
        //Change processing status
        isProcessing = true;
        //set model, data
        setModel(model);
        setData(getModel().getData());
        //Build all the unprocessed DB
        buildBatchesFromData(getData());
        //Send all the unprocessed DB to the cluster
        sendUnprocessedDB(unprocessedBatches);
        //Receive DB
        receiveProcessedDB();

    }


    //Update the tick counter if there are any batches in the storage.
    public boolean updateTick() {
        if (storageCPUprocessedBatches != null && !storageCPUprocessedBatches.isEmpty()) {
            tickCounter++;
            totalNumberOfTickUsed++;
            if (isDoneProceessingSingleBatch()) {
                processBatch();
                if (isEveryThingProcessed()) {
                  clear();
                  return true;
                }
                if (storageCPUprocessedBatches.isEmpty() && isProcessing) {
                    receiveProcessedDB();
                }
            }
        }

        return false;
    }

    //Called when finished processing single batch from storage. delete one batch from storage , update processed batches, init ticks-counter.
    private void processBatch() {
        removeOneBatchFromStorage();
        numOfProcessedBatches++;
        setTickCounterToZero();
    }

    //Build the unprocessed batches from data model
    public void buildBatchesFromData(Data data) {
        for (int i = 0; i < data.getSize(); i = i + 1000) {
            //Create DB and wrap him with vector, then push the vector to the unprocessed DB
            DataBatch db = new DataBatch(data, i, this);
            Vector<DataBatch> singleDB_Collection = new Vector<>();
            singleDB_Collection.add(db);
            unprocessedBatches.add(singleDB_Collection);
        }

    }

    // Remove one batch from storage after finishing processing him.
    private void removeOneBatchFromStorage() {
        //Remove the first DB from storage
        storageCPUprocessedBatches.removeElementAt(0);
    }

    private void sendUnprocessedDB(Vector<Vector<DataBatch>> vec) {
        Thread t_send = new Thread(() ->{
            getCluster().sendUnprocessedDB(vec);
        });
        t_send.start();
    }

    private void receiveProcessedDB() {
        Thread t_receive  = new Thread(() ->{
            getCluster().receiveProcessedDB(getProcessedBatchesQueue());
        });
        t_receive.start();
    }

    private void clear(){
        this.model = null;
        this.isProcessing = false;
        this.numOfProcessedBatches = 0;
        this.type = null;
        this.data = null;
        this.tickCounter = 0;

    }

}
