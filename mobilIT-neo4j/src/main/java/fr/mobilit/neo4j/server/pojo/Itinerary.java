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
package fr.mobilit.neo4j.server.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represent an fragment of an itinary.
 * 
 * @author bsimard
 * 
 */
public class Itinerary {

    private String         name;
    private Double         distance;
    private List<GeoPoint> line = new ArrayList<GeoPoint>();

    /**
     * @param line
     */
    public Itinerary() {
        super();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the distance
     */
    public Double getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(Double distance) {
        this.distance = distance;
    }

    /**
     * @return the line
     */
    public List<GeoPoint> getLine() {
        return line;
    }

    /**
     * @param line the line to set
     */
    public void setLine(List<GeoPoint> line) {
        this.line = line;
    }

}
