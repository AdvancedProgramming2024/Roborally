package dtu.compute.RoborallyServer.controller;

import dtu.compute.roborally.model.Heading;

public class Laser extends FieldAction {
    private Heading heading;

    public void setHeading(Heading heading) {this.heading = heading;}

    public Heading getHeading() {return heading;}

    private int lazer;

    public int getLazer() {return lazer;}

    public void setLazer(int lazer) {this.lazer = lazer;}
}
