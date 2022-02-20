import bgu.spl.mics.application.objects.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.*;

public class GPUTest {
    private CPU cpu;
    private Data data1;
    private DataBatch dataBatch1;
    private DataBatch dataBatch2;
    private Vector<DataBatch> data = new Vector<>(2);
    private int totalNumberOfTickUsed=0;
    private Cluster cluster;
    private GPU gpu;
    private Model model;
    private Vector<GPU> gpus = new Vector<>();
    private Vector<CPU> cpus = new Vector<>();
    private Vector<Model> allModels= new Vector<>();
    private Student student;




    @Before
    public void setUp() throws Exception {

        cpu = new CPU(32);
        cpus.add(cpu);
        data1 = new Data(Data.Type.Images, 2000);
        dataBatch1 = new DataBatch(data1,0,gpu);
        dataBatch2 = new DataBatch(data1,1000,gpu);
        data.add(dataBatch1);
        data.add(dataBatch2);
        cluster = new Cluster();
        gpu = new GPU(GPU.Type.GTX1080);
        gpus.add(gpu);
        student = new Student("student", "department", Student.Degree.MSc);
        model = new Model("model", data1, student);
        allModels.add(model);
        cluster.setAll(gpus,cpus,allModels);
        cpu.setData(data);
//        unprocessBatchesQueue.add(data);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void setModel() {
        gpu.setModel(model);
        assertEquals(model, gpu.getModel());
    }

    @Test
    public void setType() {
        gpu.setType(GPU.Type.RTX3090);
        assertEquals(GPU.Type.RTX3090,gpu.getType());
    }

    @Test
    public void setCluster() {
        gpu.setCluster(cluster);
        assertEquals(cluster, gpu.getCluster());
    }

    @Test
    public void setTickCounterToZero() {
        updateTick();
        gpu.setTickCounterToZero();
        assertEquals(0, gpu.getTickCounter());
    }

    @Test
    public void setNumOfProcessedBatches() {
        gpu.setNumOfProcessedBatches(10);
        assertEquals(10, gpu.getNumOfProcessedBatches());
    }

    @Test
    public void setData() {
        gpu.setData(data1);
        assertEquals(data1, gpu.getData());
    }

    @Test
    public void setUnprocessedBatches() {
        Vector<Vector<DataBatch>> unprocessedBatches = new Vector<>();
        unprocessedBatches.add(data);
        gpu.setUnprocessedBatches(unprocessedBatches);
        assertEquals(gpu.getUnprocessedBatches(), unprocessedBatches);

    }

    @Test
    public void setUnprocessBatchesQueue() {
        ConcurrentLinkedQueue<Vector<DataBatch>> unprocessBatchesQueue = new ConcurrentLinkedQueue<>();
        unprocessBatchesQueue.add(data);
        gpu.setUnprocessBatchesQueue(unprocessBatchesQueue);
        assertEquals(gpu.getUnprocessBatchesQueue(), unprocessBatchesQueue);
    }

    @Test
    public void setNumberOfTicksForProcess() {
        gpu.setNumberOfTicksForProcess(10);
        assertEquals(10,gpu.getNumberOfTicksForProcess());
    }

    @Test
    public void setProcessedBatchesQueue() {
        ConcurrentLinkedQueue<Vector<DataBatch>> processedBatchesQueue = new ConcurrentLinkedQueue<>();
        processedBatchesQueue.add(data);
        gpu.setProcessedBatchesQueue(processedBatchesQueue);
        assertEquals(gpu.getProcessedBatchesQueue(), processedBatchesQueue);
    }

    @Test
    public void getModel() {
        gpu.setModel(model);
        assertEquals(model, gpu.getModel());
    }

    @Test
    public void getType() {
        gpu.setType(GPU.Type.RTX3090);
        assertEquals(GPU.Type.RTX3090,gpu.getType());
    }


    @Test
    public void getCluster() {
        gpu.setCluster(cluster);
        assertEquals(cluster, gpu.getCluster());
    }

    @Test
    public void getTickCounter() {
        ConcurrentLinkedQueue<Vector<DataBatch>> processedBatchesQueue = new ConcurrentLinkedQueue<>();
        processedBatchesQueue.add(data);
        ConcurrentHashMap<ConcurrentLinkedQueue<Vector<DataBatch>>, GPU> queue = new ConcurrentHashMap<>();
        queue.put(processedBatchesQueue,gpu);
        gpu.getCluster().setProcessedDB(queue);
        gpu.getCluster().receiveProcessedDB(processedBatchesQueue);
        assertEquals(0,gpu.getTickCounter());
        gpu.updateTick();
        assertEquals(1,gpu.getTickCounter());

    }

    @Test
    public void getNumOfProcessedBatches() {
        gpu.setNumOfProcessedBatches(10);
        assertEquals(10, gpu.getNumOfProcessedBatches());
    }

    @Test
    public void getData() {
        gpu.setData(data1);
        assertEquals(data1, gpu.getData());
    }

    @Test
    public void getStorageCPUprocessedBatches() {
        Vector<Vector<DataBatch>> unprocessedBatches = new Vector<>();
        unprocessedBatches.add(data);
        gpu.setUnprocessedBatches(unprocessedBatches);
        assertEquals(gpu.getUnprocessedBatches(), unprocessedBatches);
    }

    @Test
    public void getNumberOfTicksForProcess() {
        gpu.setNumberOfTicksForProcess(10);
        assertEquals(10, gpu.getNumberOfTicksForProcess());
    }

    @Test
    public void getUnprocessBatchesQueue() {
        ConcurrentLinkedQueue<Vector<DataBatch>> processedBatchesQueue = new ConcurrentLinkedQueue<>();
        processedBatchesQueue.add(data);
        gpu.setProcessedBatchesQueue(processedBatchesQueue);
        assertEquals(gpu.getProcessedBatchesQueue(), processedBatchesQueue);
    }

    @Test
    public void getProcessedBatchesQueue() {
        ConcurrentLinkedQueue<Vector<DataBatch>> processedBatchesQueue = new ConcurrentLinkedQueue<>();
        processedBatchesQueue.add(data);
        gpu.setProcessedBatchesQueue(processedBatchesQueue);
        assertEquals(gpu.getProcessedBatchesQueue(), processedBatchesQueue);
    }

    @Test
    public void getTotalNumberOfTickUsed() {
        ConcurrentLinkedQueue<Vector<DataBatch>> processedBatchesQueue = new ConcurrentLinkedQueue<>();
        processedBatchesQueue.add(data);
        ConcurrentHashMap<ConcurrentLinkedQueue<Vector<DataBatch>>, GPU> queue = new ConcurrentHashMap<>();
        queue.put(processedBatchesQueue,gpu);
        gpu.getCluster().setProcessedDB(queue);
        gpu.getCluster().receiveProcessedDB(processedBatchesQueue);
        assertEquals(0,gpu.getTickCounter());
        gpu.updateTick();
        assertEquals(1,gpu.getTotalNumberOfTickUsed());
    }

    @Test
    public void isStorageEmpty() {
    }

    @Test
    public void isStorageFull() {
    }

    @Test
    public void isEveryThingProcessed() {
    }

    @Test
    public void isStorageValid() {
    }

    @Test
    public void isProcessing() {

    }

    @Test
    public void initTrainModelEvent() {
        gpu.initTrainModelEvent(model);
        assertEquals(model, gpu.getModel());
        assertEquals(model.getData(), gpu.getData());
    }

    @Test
    public void updateTick() {
        ConcurrentLinkedQueue<Vector<DataBatch>> processedBatchesQueue = new ConcurrentLinkedQueue<>();
        processedBatchesQueue.add(data);
        ConcurrentHashMap<ConcurrentLinkedQueue<Vector<DataBatch>>, GPU> queue = new ConcurrentHashMap<>();
        queue.put(processedBatchesQueue,gpu);
        gpu.getCluster().setProcessedDB(queue);
        gpu.getCluster().receiveProcessedDB(processedBatchesQueue);
        assertEquals(0,gpu.getTickCounter());
        assertEquals(0,gpu.getTotalNumberOfTickUsed());

        gpu.updateTick();
        assertEquals(1,gpu.getTotalNumberOfTickUsed());
        assertEquals(1,gpu.getTotalNumberOfTickUsed());
    }

    @Test
    public void buildBatchesFromData() {
        gpu.buildBatchesFromData(data1);
        Vector<Vector<DataBatch>> unprocessedBatches = new Vector<>();
        unprocessedBatches.add(data);
    }
}