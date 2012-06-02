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

public class ServiceCycleRent {

    protected SpatialDatabaseService spatial;

    /**
     * Constructor.
     * 
     * @param spatial
     */
    public ServiceCycleRent(SpatialDatabaseService spatial) {
        super();
        this.spatial = spatial;
    }

    /**
     * Method to get a cycle service by the geocode.
     * 
     * @param spatial
     * @param geocode
     * @return
     * @throws MobilITException
     */
    public AbstractCycleRent getGeoService(String geocode) throws MobilITException {
        Class serviceClass = Constant.CYCLE_SERVICE.get(geocode);
        if (serviceClass != null) {
            try {
                Constructor serviceConstructor = serviceClass.getConstructor(SpatialDatabaseService.class);
                AbstractCycleRent service = (AbstractCycleRent) serviceConstructor.newInstance(spatial);
                return service;
            } catch (Exception e) {
                throw new MobilITException(e.getMessage(), e.getCause());
            }
        }
        else {
            throw new MobilITException("There is no cycle service for geocode " + geocode);
        }
    }

    public POI getNearestStation(Double lon, Double lat, Integer status) throws MobilITException {
        return getNearestStation(lon, lat, 2.0, status);
    }

    /**
     * Method to get the nearest rent cycle station by longitude and latitude.
     * 
     * @param lon the longitude
     * @param lat the latitude
     * @param distance
     * @param status if 0 we search a station with free cycle, if 1 with free slot and if null whatever !
     * @return
     */
    public POI getNearestStation(Double lon, Double lat, Double distance, Integer status) throws MobilITException {
        Coordinate coord = new Coordinate(lat, lon);
        EditableLayer cycleLayer = spatial.getOrCreateEditableLayer(Constant.LAYER_CYCLE);
        //@formatter:off
        List<GeoPipeFlow> results = GeoPipeline
                .startNearestNeighborSearch(cycleLayer, coord, distance)
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
            if (status != null && (status == 0 || status == 1)) {
                AbstractCycleRent service = this.getGeoService(geocode);
                HashMap<String, Integer> places = (HashMap<String, Integer>) service.getStation(id);
                switch (status) {
                // check if there is free cycle (start point)
                    case 0:
                        Integer freeCycle = places.get(Constant.CYCLE_AVAIBLE);
                        if (freeCycle > 0) {
                            nearest = new POI(id, name, lng, lati, geocode);
                            find = true;
                        }
                        break;
                    // check if there is free slot (end point)
                    case 1:
                        Integer freeSlot = places.get(Constant.CYCLE_FREE);
                        if (freeSlot > 0) {
                            nearest = new POI(id, name, lng, lati, geocode);
                            find = true;
                        }
                        break;
                    default:
                        break;
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
