/**
 * This file is part of MobilIT.
 *
 * MobilIT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MobilIT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MobilIT. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @See https://github.com/sim51/mobilIT
 */
package fr.mobilit.neo4j.server.utils;

import java.util.HashMap;

/**
 * Constant class for MobilIT project.
 * 
 * @author bsimard
 * 
 */
public class Constant {

    // General constante
    public static final String                 LATITUDE                 = "lat";
    public static final String                 LONGITUDE                = "lon";
    public static final String                 LAYER_OSM                = "OSM";

    // default value for cost comparator
    public static final Integer                DEFAULT_SPEED            = 50;
    public static final Integer                DEFAULT_CYCLE_SPEED      = 18;
    public static final Integer                DEFAULT_PEDESTRIAN_SPEED = 5;
    public static final Double                 INFINY                   = new Double(99999);

    // constante for geo zone
    public static final String                 NANTES_GEO_CODE          = "FR_NTS";

    // constant for cycle service
    public static final String                 CYCLE_FREE               = "free";
    public static final String                 CYCLE_AVAIBLE            = "avaible";
    public static final String                 CYCLE_TOTAL              = "total";
    public static final String                 LAYER_CYCLE              = "cycle";
    public static final HashMap<String, Class> CYCLE_SERVICE            = new HashMap<String, Class>();
    static {
        CYCLE_SERVICE.put(NANTES_GEO_CODE, fr.mobilit.neo4j.server.service.nantes.CycleRentImpl.class);
    }

}
