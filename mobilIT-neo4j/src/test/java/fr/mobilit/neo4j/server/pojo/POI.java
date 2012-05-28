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

public class POI {

    private String id;
    private String name;
    private String osm_node_id;
    private Double longitude;
    private Double latitude;

    /**
     * @param id
     * @param name
     * @param osm_node_id
     * @param longitude
     * @param latitude
     */
    public POI(String id, String name, Double longitude, Double latitude) {
        super();
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
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
     * @return the osm_node_id
     */
    public String getOsm_node_id() {
        return osm_node_id;
    }

    /**
     * @param osm_node_id the osm_node_id to set
     */
    public void setOsm_node_id(String osm_node_id) {
        this.osm_node_id = osm_node_id;
    }

    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

}
