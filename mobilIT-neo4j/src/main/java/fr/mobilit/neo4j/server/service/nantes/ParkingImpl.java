package fr.mobilit.neo4j.server.service.nantes;

import java.util.List;
import java.util.Map;

import org.neo4j.gis.spatial.SpatialDatabaseService;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.service.AbstractCycleRent;

public class ParkingImpl extends AbstractCycleRent {

    /**
     * Nantes URL service for cycle rent.
     */
    private final static String IMPORT_PARKING_URL = "http://datastore.opendatasoft.com/api/fetch/dataset/equipementsdeplacelementnantes2012?{*options*}";
    private final static String DETAIL_URL         = "http://data.nantes.fr/api/getDisponibiliteParkingsPublics/1.0/ATMPSTDOTJCNTJ2";

    /**
     * Constructor.
     * 
     * @param spatial
     */
    public ParkingImpl(SpatialDatabaseService spatial) {
        super();
        this.spatial = spatial;
    }

    @Override
    public List<POI> importStation() throws MobilITException {
        return null;
    }

    @Override
    public Map<String, Integer> getStation(String id) throws MobilITException {
        return null;
    }

}
