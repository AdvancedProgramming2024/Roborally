package dtu.compute.RoborallyServer.controller;

import dtu.compute.roborally.model.Heading;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushPanel extends FieldAction {
    private Heading heading;
    private PushTime pushTime;
    public static enum PushTime {
        EVEN, ODD
    }
}
