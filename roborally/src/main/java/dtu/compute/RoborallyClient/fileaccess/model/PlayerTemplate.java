package dtu.compute.RoborallyClient.fileaccess.model;

import java.util.ArrayList;
import java.util.List;

import static dtu.compute.RoborallyClient.view.PlayerView.*;

public class PlayerTemplate implements Cloneable {
    public int id;
    public String name;
    public String color;

    public int xPosition;
    public int yPosition;
    public int heading;
    public List<Integer> drawPile = new ArrayList<>();
    public List<Integer> discardPile = new ArrayList<>();
    public int[] program = new int[NO_REGISTERS];
    public int[] hand = new int[NO_CARDS];
    public int[] permanent = new int[NO_UPGRADE_CARDS];
    public boolean[] permanentActive = new boolean[NO_UPGRADE_CARDS];
    public int[] temporary = new int[NO_UPGRADE_CARDS];
    public boolean[] temporaryActive = new boolean[NO_UPGRADE_CARDS];

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
