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

/**
 * Constant class for MobilIT project.
 * 
 * @author bsimard
 * 
 */
public class Constant {

    public static final String  LATITUDE                 = "lat";
    public static final String  LONGITUDE                = "lon";
    public static final String  LAYER_OSM                = "OSM";
    public static final Integer DEFAULT_SPEED            = 50;
    public static final Integer DEFAULT_CYCLE_SPEED      = 18;
    public static final Integer DEFAULT_PEDESTRIAN_SPEED = 5;
    public static final Double  INFINY                   = new Double(99999);
}
