package dk.dtu.compute.se.pisd.roborally.fileaccess.model;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
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
    public int[] program = new int[Player.NO_REGISTERS];
    public int[] hand = new int[Player.NO_CARDS];
    public int[] permanent = new int[Player.NO_UPGRADE_CARDS];
    public boolean[] permanentActive = new boolean[Player.NO_UPGRADE_CARDS];
    public int[] temporary = new int[Player.NO_UPGRADE_CARDS];
    public boolean[] temporaryActive = new boolean[Player.NO_UPGRADE_CARDS];

    public int checkpoints;
    public int energyBank;
    public boolean rebooting;

    @Override
    public PlayerTemplate clone() {
        try {
            PlayerTemplate tmp = (PlayerTemplate) super.clone();
            tmp.program = program.clone();
            tmp.hand = hand.clone();
            tmp.drawPile = new ArrayList<>(drawPile);
            tmp.discardPile = new ArrayList<>(discardPile);
            tmp.permanent = permanent.clone();
            tmp.permanentActive = permanentActive.clone();
            tmp.temporary = temporary.clone();
            tmp.temporaryActive = temporaryActive.clone();
            return tmp;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
