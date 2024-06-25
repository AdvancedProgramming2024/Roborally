package dtu.compute.RoborallyClient.fields;

import dtu.compute.RoborallyClient.model.Heading;
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
