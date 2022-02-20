package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    public enum Status {PreTrained , Training , Trained , Tested};
    public enum Results {None , Good , Bad };

    //Fields

    private String name;
    private Data data;
    private Student student;
    private Status status = Status.PreTrained;
    private Results results= Results.None;


    //Constructor

    public Model(String name, Data data, Student student) {
        this.name = name;
        this.data = data;
        this.student = student;
    }


    //Getters

    public String getName() {
        return name;
    }

    public Data getData() {
        return data;
    }

    public Student getStudent() {
        return student;
    }

    public Status getStatus() {
        return status;
    }

    public Results getResults() {
        return results;
    }

    //Setter

    public void setName(String name) {
        this.name = name;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setResults(Results results) {
        this.results = results;
    }
}
