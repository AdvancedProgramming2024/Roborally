package dk.dtu.compute.se.pisd.roborally.fileaccess.model;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;

import java.util.ArrayList;
import java.util.List;

import static dk.dtu.compute.se.pisd.roborally.model.Heading.SOUTH;

public class PlayerTemplate implements Cloneable {
    public int id;
    public String name;
    public String color;

    public int xPosition;
    public int yPosition;
    public int heading;
    public List<Integer> drawPile = new ArrayList<>();
    public List<Integer> discardPile = new ArrayList<>();
    public int[] program = new int[5];
    public int[] hand = new int[8];

    public int checkpoints;
    public int energyBank;
    public boolean rebooting;

    @Override
    public PlayerTemplate clone() {
        try {
            return (PlayerTemplate) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
