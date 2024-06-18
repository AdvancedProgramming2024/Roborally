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
    CRAB_LEGS("Crab Legs", 5, true),
    BRAKES("Brakes", 3, true),
    DEFLECTOR_SHIELD("Deflector Shield", 2, true),
    DOUBLE_BARREL_LASER("Double Barrel Laser", 2, true),
    MODULAR_CHASSIS("Modular Chassis", 1, true),
    FIREWALL("Firewall", 3, true),
    PRESSOR_BEAM("Pressor Beam", 3, true),
    HOVER_UNIT("Hover unit", 1, true),
    RAIL_GUN("Rail Gun", 2, true),
    MEMORY_STICK("Memory Stick", 3, true),
    RAMMING_GEAR("Ramming Gear", 2, true),
    MINI_HOWITZER("Mini Howitzer", 2, true),
    REAR_LASER("Rear Laser", 2, true),
    SCRAMBLER("Scrambler", 3, true),
    TRACTOR_BEAM("Tractor Beam", 3, true),
    SIDE_ARMS("Side Arms", 3, true),
    TROJAN_NEEDLER("Trojan Needler", 3, true),
    TELEPORTER("Teleporter", 3, true),
    VIRUS_MODULE("Virus Module", 2, true),
    BOINK("Boink", 1, false);

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
