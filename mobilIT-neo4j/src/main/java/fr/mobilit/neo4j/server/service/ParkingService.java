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
package fr.mobilit.neo4j.server.service;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;

import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.graphdb.Node;

import com.vividsolutions.jts.geom.Coordinate;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.utils.Constant;

public class ParkingService {

    protected SpatialDatabaseService spatial;

    /**
     * Constructor.
     * 
     * @param spatial
     */
    public ParkingService(SpatialDatabaseService spatial) {
        super();
        this.spatial = spatial;
    }

    /**
     * Method to get a parking service by the geocode.
     * 
     * @param geocode
     * @return
     * @throws MobilITException
     */
    public AbstractParking getGeoService(String geocode) throws MobilITException {
        Class serviceClass = Constant.PARKING_SERVICE.get(geocode);
        if (serviceClass != null) {
            try {
                Constructor serviceConstructor = serviceClass.getConstructor(SpatialDatabaseService.class);
                AbstractParking service = (AbstractParking) serviceConstructor.newInstance(spatial);
                return service;
            } catch (Exception e) {
                throw new MobilITException(e.getMessage(), e.getCause());
            }
        }
        else {
            throw new MobilITException("There is no parking service for geocode " + geocode);
        }
    }

    public POI getNearest(Double lon, Double lat, Integer status) throws MobilITException {
        return getNearest(lon, lat, 2.0, status);
    }

    /**
     * Method to get the nearest parking by longitude and latitude.
     * 
     * @param lon the longitude
     * @param lat the latitude
     * @param distance
     * @param status if 0 we search a station with free places and if null whatever !
     * @return
     */
    public POI getNearest(Double lon, Double lat, Double distance, Integer status) throws MobilITException {
        Coordinate coord = new Coordinate(lat, lon);
        EditableLayer layer = spatial.getOrCreateEditableLayer(Constant.PARKING_LAYER);
        //@formatter:off
        List<GeoPipeFlow> results = GeoPipeline
                .startNearestNeighborSearch(layer, coord, distance)
                .sort("Distance")
                .toList();
        //@formatter:on
        int i = 0;
        Boolean find = false;
        POI nearest = null;
        while (find == false && i < results.size()) {
            SpatialDatabaseRecord dbRecord = results.get(i).getRecord();
            Node node = dbRecord.getGeomNode();
            String geocode = (String) node.getProperty("geocode", null);
            String id = (String) node.getProperty("id", null);
            String name = (String) node.getProperty("name", null);
            Double lng = (Double) node.getProperty("lon", null);
            Double lati = (Double) node.getProperty("lat", null);
            if (status != null) {
                AbstractParking service = this.getGeoService(geocode);
                HashMap<String, Integer> places = (HashMap<String, Integer>) service.getParking(id);
                if (places.isEmpty()) {
                    // here there is no information
                    nearest = new POI(id, name, lng, lati, geocode);
                    find = true;
                }
                else {
                    Integer free = places.get(Constant.PARKING_FREE);
                    if (free != null && free > 0) {
                        nearest = new POI(id, name, lng, lati, geocode);
                        find = true;
                    }
                }
            }
            else {
                nearest = new POI(id, name, lng, lati, geocode);
                find = true;
            }
            i++;
        }
        // TODO: throw an exception if there is no station ?
        return nearest;
    }

    /**
     * Getter.
     * 
     * @return the spatial
     */
    public SpatialDatabaseService getSpatial() {
        return spatial;
    }

    /**
     * Setter.
     * 
     * @param spatial the spatial to set
     */
    public void setSpatial(SpatialDatabaseService spatial) {
        this.spatial = spatial;
    }

}
