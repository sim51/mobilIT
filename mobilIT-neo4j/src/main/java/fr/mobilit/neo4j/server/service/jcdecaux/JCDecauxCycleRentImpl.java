package fr.mobilit.neo4j.server.service.jcdecaux;

import com.google.gson.Gson;
import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.service.AbstractCycleRent;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.neo4j.gis.spatial.SpatialDatabaseService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcducobu
 * Date: 05/10/13
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */
public class JCDecauxCycleRentImpl extends AbstractCycleRent {
    /**
     * JCDecaux pamameters for cycle rent api.
     */
    public final static String API_KEY = "";
    public final static String CONTACT_NAME = "";


    /**
     * JCDecaux URL service for cycle rent.
     */
    public final static String IMPORT_URL = "https://api.jcdecaux.com/vls/v1/stations?contract=Namur&apiKey=c5e55a68bcffa6f022704df6db2813e0ce621e9b"; "http://www.bicloo.nantesmetropole.fr/service/carto";
    public final static String DETAIL_URL = "https://api.jcdecaux.com/vls/v1/stations/7?contract=Namur&apiKey=c5e55a68bcffa6f022704df6db2813e0ce621e9b";

    /**
     * Constructor.
     *
     * @param spatial
     */
    public JCDecauxCycleRentImpl(SpatialDatabaseService spatial) {
        super();
        this.spatial = spatial;
    }

    @Override
    public List<POI> importStation() throws MobilITException {
        List<POI> stations = new ArrayList<POI>();
        HttpClient client = new HttpClient();
        GetMethod get = null;
        try {
            // we do the http call and parse the xml response
            get = new GetMethod(IMPORT_URL);
        }
    }


    /**
     * Namur URL service for cycle rent.
     */


}