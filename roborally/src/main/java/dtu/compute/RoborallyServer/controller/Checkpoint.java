package dtu.compute.RoborallyServer.controller;

public class Checkpoint extends FieldAction{

    private final int id;

    public Checkpoint(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
