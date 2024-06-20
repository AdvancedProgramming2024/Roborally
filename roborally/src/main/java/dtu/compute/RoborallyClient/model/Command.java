/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dtu.compute.RoborallyClient.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public enum Command {

    // This is a very simplistic way of realizing different commands.
    // Basic programming commands
    MOVE_1("Move 1"),
    MOVE_2("Move 2"),
    MOVE_3("Move 3"),
    RIGHT("Turn Right"),
    LEFT("Turn Left"),
    U_TURN("U Turn"),
    MOVE_BACK("Move Back"),
    POWER_UP("Power Up"),
    AGAIN("Again (repeat action)"),

    // Damage commands
    SPAM("Spam"),
    TROJAN_HORSE("Trojan Horse"),
    VIRUS("Virus"),
    WORM("Worm"),

    // Special programming commands
    ENERGY_ROUTINE("Energy Routine"),
    SANDBOX_ROUTINE("Sandbox Routine", MOVE_1, MOVE_2, MOVE_3, MOVE_BACK, LEFT, RIGHT, U_TURN),
    WEASEL_ROUTINE("Weasel Routine", LEFT, RIGHT, U_TURN),
    SPEED_ROUTINE("Speed Routine", MOVE_3),
    SPAM_FOLDER("Spam Folder"),
    REPEAT_ROUTINE("Repeat Routine", AGAIN);

    final public String displayName;

    final private List<Command> options;

    Command(String displayName, Command... options) {
        this.displayName = displayName;
        this.options = Collections.unmodifiableList(Arrays.asList(options));
    }

    public List<Command> getOptions() {
        return options;
    }

}
