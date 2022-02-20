import bgu.spl.mics.application.objects.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.*;

public class CPUTest {
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
        gpu = new GPU(GPU.Type.RTX3090);
        gpus.add(gpu);
        student = new Student("student", "department", Student.Degree.MSc);
        model = new Model("model", data1, student);
        allModels.add(model);
        cluster.setAll(gpus,cpus,allModels);
        cpu.setData(data);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getNumOfCores() {
        cpu.setNumOfCores(16);
        assertEquals(16,cpu.getNumOfCores());
    }

    @Test
    public void getDataType() {
        cpu.setDataType(Data.Type.Images);
        assertEquals(Data.Type.Images, cpu.getDataType());
    }

    @Test
    public void getTotalNumberOfProcessed_DB() {
        assertEquals(0,cpu.getTotalNumberOfProcessed_DB());
    }

    @Test
    public void getData() {
        cpu.setData(data);
        assertEquals(data,cpu.getData());
    }

    @Test
    public void getCluster() {
        cpu.setCluster(cluster);
        assertEquals(cluster, cpu.getCluster());
    }

    @Test
    public void getTickCounter() {
        cpu.setTickCounterToZero();
        cpu.updateTick();
        assertEquals(1,cpu.getTickCounter());
    }

    @Test
    public void getTotalNumberOfTickUsed() {
        cpu.setTickCounterToZero();
        cpu.updateTick();
        assertEquals(1,cpu.getTotalNumberOfTickUsed());
    }

    @Test
    public void setNumOfCores() {
        cpu.setNumOfCores(16);
        assertEquals(16,cpu.getNumOfCores());
    }

    @Test
    public void setProcessing() {
        cpu.setProcessing(true);
        assertEquals(true, cpu.isProcessingRightNow());
        cpu.setProcessing(false);
        assertEquals(false, cpu.isProcessingRightNow());
    }

    @Test
    public void setData() {
        cpu.setData(data);
        assertEquals(data,cpu.getData());
    }

    @Test
    public void setCluster() {
        cpu.setCluster(cluster);
        assertEquals(cluster, cpu.getCluster());
    }

    @Test
    public void setTickCounterToZero() {
        cpu.updateTick();
        cpu.updateTick();
        cpu.setTickCounterToZero();
        assertEquals(0,cpu.getTickCounter());
    }

    @Test
    public void setNumberOfTicksForProcess() {

    }

    @Test
    public void setDataType() {
        cpu.setDataType(Data.Type.Images);
        assertEquals(Data.Type.Images, cpu.getDataType());
    }



    @Test
    public void isProcessingRightNow() {
        cpu.setProcessing(true);
        assertEquals(true, cpu.isProcessingRightNow());
        cpu.setProcessing(false);
        assertEquals(false, cpu.isProcessingRightNow());
    }

    @Test
    public void updateTick() {
        cpu.setTickCounterToZero();
        cpu.updateTick();
        assertEquals(1, cpu.getTickCounter());
    }

    @Test
    public void receive_DB() {
        cpu.receive_DB();
        assertEquals(cpu.getData(), data);
    }

    @Test
    public void send_DB() {
    }
}