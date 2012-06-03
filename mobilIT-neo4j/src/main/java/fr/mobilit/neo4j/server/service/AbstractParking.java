package fr.mobilit.neo4j.server.service;

import java.util.List;
import java.util.Map;

import org.neo4j.gis.spatial.SpatialDatabaseService;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;

public abstract class AbstractParking {

    protected SpatialDatabaseService spatial;

    /**
     * Method to import all rent cycle station to the database.
     * 
     * @return the list of station imported
     * @throws MobilITException
     */
    public abstract List<POI> importParking() throws MobilITException;

    /**
     * Method to get the nearest parking.
     * 
     * @param id the id of the station.
     * @return
     */
    public abstract Map<String, Integer> getParking(String id) throws MobilITException;

}
