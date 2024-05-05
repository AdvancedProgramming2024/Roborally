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
package dk.dtu.compute.se.pisd.roborally.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ...
 *
 * @author Jamie (s236939)
 *
 */
public enum Upgrade {

    //Permanent Upgrades
    ADMIN_PRIVILEGE("Admin Privilege", 3, true),
    CORRUPTION_WAVE("Corruption Wave", 4, true),
    BLUE_SCREEN_OF_DEATH("Blue Screen of Death", 4, true),
    BRAKES("Brakes", 3, true),
    DEFLECTOR_SHIELD("Deflector Shield", 2, true);

    //TO DO
    //Temporary Upgrades

    final public String displayName;
    final public int cost;
    final public boolean isPermanent;

    Upgrade(String displayName, int cost, boolean isPermanent) {
        this.displayName = displayName;
        this.cost = cost;
        this.isPermanent = isPermanent;
    }

}
