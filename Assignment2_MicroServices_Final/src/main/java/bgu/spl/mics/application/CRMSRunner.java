package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;


import java.awt.*;
import java.io.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.Vector;
import java.util.concurrent.Executors;


/**
 * This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {
        String path = args[0];
        File input = new File(path);
//        File input = new File("example_input.json");
        try {
            JsonElement fileElement = JsonParser.parseReader(new FileReader(input));
            JsonObject fileObject = fileElement.getAsJsonObject();

            //Extracting the basic fields
            int duration = fileObject.get("Duration").getAsInt();//Duration of the program(number of ticks)
            int tickTime = fileObject.get("TickTime").getAsInt();//value of tick
            TimeService timeService = new TimeService(duration, tickTime);//instance of TimeService

            //Process of GPU
            JsonArray jsonArrayOfGpus = fileObject.get("GPUS").getAsJsonArray();
            Vector<GPU> gpusList = new Vector<>();
            Vector<GPUService> gpusServiceList = new Vector<>();
            int gpuId = 1;
            for (JsonElement gpuElement : jsonArrayOfGpus) {

                //Extract data of GPU:
                String gpuType = gpuElement.getAsString();
                GPU.Type type;
                if (gpuType.equals("RTX3090")) {
                    type = GPU.Type.RTX3090;
                } else if (gpuType.equals("RTX2080")) {
                    type = GPU.Type.RTX2080;
                } else {
                    type = GPU.Type.GTX1080;
                }
                GPU gpu = new GPU(type);
                gpusList.add(gpu);
                GPUService gpuService = new GPUService(gpu, "gpu" + gpuId);
                gpusServiceList.add(gpuService);
                gpuId++;
            }
//            System.out.println("All my GPUS are: " + gpusList);

            //Process of CPU
            JsonArray jsonArrayOfCpus = fileObject.get("CPUS").getAsJsonArray();
            Vector<CPU> cpusList = new Vector<>();
            Vector<CPUService> cpusServiceList = new Vector<>();
            int cpuId = 1;
            for (JsonElement cpuElement : jsonArrayOfCpus) {

                //Extract data of CPU:
                int cpuType = cpuElement.getAsInt();
                CPU cpu = new CPU(cpuType);
                cpusList.add(cpu);
                CPUService cpuService = new CPUService(cpu, "cpu" + cpuId);
                cpusServiceList.add(cpuService);
                cpuId++;
            }
            //print list of CPUS
//            System.out.println("All my CPUS are: " + cpusList);

            //Process of Conference
            JsonArray jsonArrayOfConferences = fileObject.get("Conferences").getAsJsonArray();
            Vector<ConfrenceInformation> conferencesList = new Vector<>();
            Vector<ConferenceService> conferencesServiceList = new Vector<>();
            int conferenceId = 1;
            for (JsonElement conferenceElement : jsonArrayOfConferences) {
                //Get the JsonObject of Conference:
                JsonObject conferenceJsonObject = conferenceElement.getAsJsonObject();

                //Extract data of Conference:
                String conferenceName = conferenceJsonObject.get("name").getAsString();
                int conferenceDate = conferenceJsonObject.get("date").getAsInt();
                ConfrenceInformation conference = new ConfrenceInformation(conferenceName, conferenceDate);//Instance of conference
                conferencesList.add(conference);//Adds conference to the list to which it belongs
                ConferenceService conferenceService = new ConferenceService(conference);
                conferencesServiceList.add(conferenceService);
                conferenceId++;
            }
            //print list of Conferences
//            System.out.println("All my Conferences are: " + conferencesList);

            //Process all Students
            JsonArray jsonArrayOfStudents = fileObject.get("Students").getAsJsonArray();

            Vector<Student> studentsList = new Vector<>();
            Vector<StudentService> studentsServiceList = new Vector<>();
            Vector<Model> allModelsList = new Vector<>();
            for (JsonElement studentElement : jsonArrayOfStudents) {
                //Get the JsonObject of Student:
                JsonObject studentJsonObject = studentElement.getAsJsonObject();

                //Extract data of Student
                String studentName = studentJsonObject.get("name").getAsString();
                String studentDepartment = studentJsonObject.get("department").getAsString();
                String studentStatus = studentJsonObject.get("status").getAsString();
                Student.Degree status;
                if (studentStatus == "MSc")
                    status = Student.Degree.MSc;
                else {
                    status = Student.Degree.PhD;
                }
                Student student = new Student(studentName, studentDepartment, status);//Instance of Student
                studentsList.add(student);//Adds student to the list to which it belongs

                //Process all Models
                JsonArray jsonArrayOfModels = studentJsonObject.get("models").getAsJsonArray();
                Vector<Model> modelsList = new Vector<>();
                for (JsonElement modelElement : jsonArrayOfModels) {
                    //Get the JsonObject of Model:
                    JsonObject modelJsonObject = modelElement.getAsJsonObject();

                    //Extract data of Model:
                    String modelName = modelJsonObject.get("name").getAsString();

                    //Extract data of Data:
                    String dataType = modelJsonObject.get("type").getAsString();
                    int dataSize = modelJsonObject.get("size").getAsInt();
                    Data.Type type;
                    if (dataType == "images") {
                        type = Data.Type.Images;
                    } else if (dataType == "Text") {
                        type = Data.Type.Text;
                    } else {
                        type = Data.Type.Tabular;
                    }
                    Data data = new Data(type, dataSize);//Instance of Data

                    Model model = new Model(modelName, data, student);//Instance of Model
                    modelsList.add(model);//Adds model to the list to which it belongs
                    allModelsList.add(model);
                }

                StudentService studentService = new StudentService(student, modelsList);
                studentsServiceList.add(studentService);

                //print list of students
//                System.out.println("studentService: " + studentService.getName());

            }

            Cluster cluster = Cluster.getInstance();//instance of Cluster
            cluster.setAll(gpusList, cpusList, allModelsList);

            //Create Threads

            //Threads of Conference
            Vector<Thread> conferenceThreads = new Vector<>();
            for (int i = 0; i < conferencesServiceList.size(); i++) {
                conferenceThreads.add(new Thread(conferencesServiceList.elementAt(i), "conferenceThread " + i + 1));
            }
            //Threads of GPU
            Vector<Thread> gpuThreads = new Vector<>();
            for (int i = 0; i < gpusServiceList.size(); i++) {
                gpuThreads.add(new Thread(gpusServiceList.elementAt(i), "gpuThread " + i + 1));
            }

            //Threads of CPU
            Vector<Thread> cpuThreads = new Vector<>();
            for (int i = 0; i < cpusServiceList.size(); i++) {
                cpuThreads.add(new Thread(cpusServiceList.elementAt(i), "cpuThread " + i + 1));
            }

            //Threads of Students
            Vector<Thread> studentThreads = new Vector<>();
            for (int i = 0; i < studentsServiceList.size(); i++) {
                studentThreads.add(new Thread(studentsServiceList.elementAt(i), "studentThread " + i + 1));
            }

            //Thread of TimeService
            Thread timeServiceThread = new Thread(timeService);

            //Start Threads
            for (int i = 0; i < studentThreads.size(); i++) {
                studentThreads.elementAt(i).start();
            }
            for (int i = 0; i < gpuThreads.size(); i++) {
                gpuThreads.elementAt(i).start();
            }
            for (int i = 0; i < cpuThreads.size(); i++) {
                cpuThreads.elementAt(i).start();
            }
            for (int i = 0; i < conferenceThreads.size(); i++) {
                conferenceThreads.elementAt(i).start();
            }
            timeServiceThread.start();

            //Join Threads
            for (int i = 0; i < studentThreads.size(); i++) {
                studentThreads.elementAt(i).join();
            }
            for (int i = 0; i < gpuThreads.size(); i++) {
                gpuThreads.elementAt(i).join();
            }
            for (int i = 0; i < cpuThreads.size(); i++) {
                cpuThreads.elementAt(i).join();
            }
            for (int i = 0; i < conferenceThreads.size(); i++) {
                conferenceThreads.elementAt(i).join();
            }
            timeServiceThread.join();

            cluster.releaseLocks();

//            Write Output:
            try {
                BufferedWriter output = new BufferedWriter(new FileWriter("output.txt"));
                output.write("OUTPUT FILE: " + "\n");
                output.write("\n" + "STUDENTS INFORMATION: " + "\n");
                for (StudentService student : studentsServiceList) {
                    output.write("\n");
                    String studentString = "Student name: " + student.getName() + "\n" + "Number of papers read: " + student.getStudent().getPapersRead();
                    String trainedModels = "\n" + "Trained model: ";
                    String publishModels = "\n" + "Published models: ";
                    for (Model model : student.getModelVector()) {
                        if (model.getStatus() == Model.Status.Trained || model.getStatus() == Model.Status.Tested) {
                            trainedModels += model.getName() + ", ";
                        }
                        if (model.getResults() == Model.Results.Good) {
                            publishModels += model.getName() + ", ";
                        }
                    }

                    publishModels += "\n";
                    output.write(studentString);
                    output.write(trainedModels);
                    output.write(publishModels);
                }

                output.write("\n" + "CONFERENCES INFORMATION: " + "\n");

                for (ConferenceService conferenceService : conferencesServiceList) {
                    Vector<Model> vec = conferenceService.getModelNames();
                    String conference = "\n" + "Conference : " + conferenceService.getName() + "\n";
                    String conferenceModels = "Models published : ";
                    for (Model model : vec) {
                        conferenceModels += model.getName() + ", ";
                    }
                    conferenceModels += "\n";
                    output.write(conference);
                    output.write(conferenceModels);
                }

                output.write("\n" + "GENERAL INFORMATION: " + "\n");
                output.write("\n");
                Object[] statistics = cluster.getStatistics();
                String cpuTimeUsed = "Amount of ticks used by CPUs: " + statistics[2] + "\n";
                String gpuTimeUsed = "Amount of ticks used by GPUs: " + statistics[3] + "\n";
                String cpuBatchesProcessed = "Amount of batches processed: " +statistics[1] + "\n";
                output.write(gpuTimeUsed);
                output.write(cpuTimeUsed);
                output.write(cpuBatchesProcessed);

                output.close();

            } catch (
                    IOException e) {
                e.printStackTrace();
            }


        } catch (FileNotFoundException e) {
            System.err.println("Error input file is not found!");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error processing input file!");
            e.printStackTrace();
        }


    }
}


//        Data data = new Data(Data.Type.Images, 10000);
//
//        Student s1 = new Student("Idan" , "CS", Student.Degree.MSc );
//
//        Model model = new Model("model1" , data,s1);
//
//        Vector<Model> vec = new Vector<>();
//
//        vec.add(model);
//
//        StudentService ss1 = new StudentService(s1, vec);
////
//        MessageBusImpl mb =  MessageBusImpl.getInstance();
////
//        GPU gpu = new GPU(GPU.Type.RTX3090);
//        CPU cpu = new CPU(16);
//        TimeService timeService = new TimeService(85,50);
//        ConferenceInformation conferenceInformation = new ConferenceInformation("con1", 83);
//        ConferenceService conferenceService = new ConferenceService(conferenceInformation);
//        Vector<GPU> vec2 = new Vector<>();
//        Vector<CPU> vec3 = new Vector<>();
//        vec2.add(gpu);
//        vec3.add(cpu);
//        Cluster cluster = new Cluster(vec2,vec3,vec);
//
//
//        GPUService gpuService = new GPUService(gpu, "gpus1");
//        CPUService cpuService = new CPUService(cpu, "cpus1");
//        System.out.println("Done");
//        Thread gpus1 = new Thread(gpuService);
//        Thread cpus1 = new Thread(cpuService);
//        Thread ss2 = new Thread(ss1);
//        Thread ts1 = new Thread(timeService);
//        Thread cs1 = new Thread(conferenceService);
////        timeService.run();
//        gpus1.start();
//        cpus1.start();
//        ss2.start();
//        ts1.start();
//        cs1.start();
//        try{
//            gpus1.join();
//            cpus1.join();
//            ss2.join();
//            ts1.join();
//            cs1.join();
//        }catch (Exception e){
//            System.out.println(e);
//        }
//
//        System.out.println(gpu.getTotalNumberOfTickUsed());
//        System.out.println(cpu.getTotalNumberOfProcessed_DB());
//        System.out.println(cpu.getTotalNumberOfTickUsed());
//        System.out.println(ss1.getModelVector().elementAt(0).getStatus());
//        System.out.println(ss1.getModelVector().elementAt(0).getResults());
//        System.out.println(ss1.getModelVector().elementAt(0).getStudent().getPublications());
//
//