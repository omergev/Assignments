package bgu.spl.mics.application.objects;



import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	//Singleton
	private static class ClusterHolder {
		private static Cluster instance = new Cluster();
	}

    /**
     * Retrieves the single instance of this class.
     */
    public static Cluster getInstance() {
        return ClusterHolder.instance;
    }


    //Fields

    final private Object lock2 = new Object();
    private boolean programRunning = true;

    //TODO: check which data structure is better to use
    private Vector<GPU> gpus;
    private Vector<CPU> cpus;
    private Vector<Model> allModels;

    //TODO: check how implement statistics.
    private Object[] statistics = new Object[4];


    ConcurrentLinkedQueue<Pair<GPU, ConcurrentLinkedQueue<Vector<DataBatch>>>> unprocessedDB = new ConcurrentLinkedQueue<>();
    ConcurrentHashMap<ConcurrentLinkedQueue<Vector<DataBatch>>, GPU> processedDB = new ConcurrentHashMap<>();

    //Constructor
    public void setAll(Vector<GPU> gpus, Vector<CPU> cpus, Vector<Model> allModels) {
        this.gpus = gpus;
        this.cpus = cpus;
        this.allModels = allModels;
        //Init gpus,cpus queues
        for (int i = 0; i < gpus.size(); i++) {
            gpus.elementAt(i).setCluster(this);
            //Create queues for each gpu
            ConcurrentLinkedQueue<Vector<DataBatch>> queue1 = new ConcurrentLinkedQueue<>();
            ConcurrentLinkedQueue<Vector<DataBatch>> queue2 = new ConcurrentLinkedQueue<>();
            //Create the pairs
            Pair<GPU, ConcurrentLinkedQueue<Vector<DataBatch>>> pair1 = new Pair<>(gpus.elementAt(i), queue1);
            //Add the pairs to UnprocessedDB and processedDB queues
            unprocessedDB.add(pair1);
            processedDB.put(queue2, gpus.elementAt(i));
            //Save pointers of the queues in the GPUs
            gpus.elementAt(i).setUnprocessedBatchesQueue(queue1);
            gpus.elementAt(i).setProcessedBatchesQueue(queue2);
        }
        for (CPU cpu : cpus) {
            cpu.setCluster(this);
        }
    }

    public Cluster() {
    }

    //Setters

    public void setStatistics(Object[] statistics) {
        this.statistics = statistics;
    }

    public void setUnprocessedDB(ConcurrentLinkedQueue<Pair<GPU, ConcurrentLinkedQueue<Vector<DataBatch>>>> unprocessedDB) {
        this.unprocessedDB = unprocessedDB;
    }

    public void setProcessedDB(ConcurrentHashMap<ConcurrentLinkedQueue<Vector<DataBatch>>, GPU> processedDB) {
        this.processedDB = processedDB;
    }

//Getters

    public Vector<GPU> getGpus() {
        return gpus;
    }

    public Vector<CPU> getCpus() {
        return cpus;
    }


    public ConcurrentLinkedQueue<Pair<GPU, ConcurrentLinkedQueue<Vector<DataBatch>>>> getUnprocessedDB() {
        return unprocessedDB;
    }

    public ConcurrentHashMap<ConcurrentLinkedQueue<Vector<DataBatch>>, GPU> getProcessedDB() {
        return processedDB;
    }

    //Methods
    public void releaseLocks() {

        programRunning = false;
        synchronized (lock2) {
            for (CPU cpu : cpus) {
                lock2.notifyAll();
            }
        }

        for (GPU gpu : gpus) {
            synchronized (gpu) {
                gpu.notifyAll();
            }
        }
    }




    //Call this method from GPU after finishing processing each batch or after sending all the unprocessed batches
    public void receiveProcessedDB(ConcurrentLinkedQueue<Vector<DataBatch>> bQueue) {
        //TODO: make all this session synchronized
        //TODO: make this session blocking method
        GPU gpu = processedDB.get(bQueue);
        synchronized (gpu) {
            //In case that bQueue and storage are both empty - send thread to sleep
            while ((bQueue.size() == 0 && gpu.getStorageCPUprocessedBatches().size()==0) && programRunning) {
                try {
                    gpu.wait();
                } catch (Exception e) {
                    System.out.println("receive process DB - exception" + e);
                }
            }
            while (!gpu.isStorageFull() && !bQueue.isEmpty()) {
                gpu.getStorageCPUprocessedBatches().add(bQueue.poll());
            }
        }
    }

    //Call this method from CPU after finishing processing single DB.
    public void sendProcessedDB(Vector<DataBatch> vec) {
        //TODO: add synchronized
        GPU gpu = vec.elementAt(0).getGpu();
        synchronized (gpu) {
            gpu.getProcessedBatchesQueue().add(vec);
            gpu.notifyAll();
        }


    }

    //Call this method from CPU in the constructor or after sending the processed DB. this method should be blocking.
    public Vector<DataBatch> receiveUnprocessedDB() {
        //TODO: add synchronized
        synchronized (lock2) {
            while (programRunning) {
                //Search for DB in all the queues
                for (int i = 0; i < unprocessedDB.size(); i++) {
                    //Remove the first object in the queue
                    Pair<GPU, ConcurrentLinkedQueue<Vector<DataBatch>>> pair = unprocessedDB.poll();
                    GPU gpu = pair.getFirst();
                    ConcurrentLinkedQueue<Vector<DataBatch>> queue = pair.getSecond();
                    unprocessedDB.add(pair);
                    //Check if the queue is not empty
                    if (!queue.isEmpty()) {
                        //Take one DB from the queue
                        return queue.poll();
                    }
                    // Add the element in the end of the queue - Round robin
                }

                //TODO: send thread to sleep until an unprocessed DB is inserted
                try {
                    lock2.wait();
                } catch (Exception e) {
                    System.out.println("receiveUnprocessedDB exception : " + e);
                }
            }
        }
        return null;
    }

    //Call this method from GPU after building DB from data.
    public void sendUnprocessedDB(Vector<Vector<DataBatch>> vec) {

        synchronized (lock2) {

            ConcurrentLinkedQueue<Vector<DataBatch>> unprocessedBatchesQueue = vec.elementAt(0).elementAt(0).getGpu().getUnprocessBatchesQueue();
            while (vec.size() != 0) {
                unprocessedBatchesQueue.add(vec.remove(0));
            }
            try {
                lock2.notifyAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Object[] getStatistics() {

        //Go over all the model and find the model that have been trained and save their name
        Vector<String> trainedModel = new Vector<>();
        for (Model model : allModels) {
            if (model.getStatus() == Model.Status.Trained) {
                trainedModel.add(model.getName());
            }
        }
        statistics[0] = trainedModel;

        //Go over all the cpus and find the total number of DB processed by them and used ticks.
        int totalDBprocessedByCPU = 0;
        int totalTicksUsedByCPUs = 0;
        for (CPU cpu : cpus) {
            totalDBprocessedByCPU += cpu.getTotalNumberOfProcessed_DB();
            totalTicksUsedByCPUs += cpu.getTotalNumberOfTickUsed();
        }
        statistics[1] = totalDBprocessedByCPU;
        statistics[2] = totalTicksUsedByCPUs;

        //Go over all the gpus and find the total number of used ticks
        int totalTicksUsedByGPUs = 0;
        for (GPU gpu : gpus) {
            totalTicksUsedByGPUs += gpu.getTotalNumberOfTickUsed();
        }

        statistics[3] = totalTicksUsedByGPUs;

        return statistics;

    }
}
