package fr.mobilit.neo4j.server.service.namur;

import com.google.gson.stream.JsonReader;
import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.service.CycleRentService;
import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.Constant;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: marcducobu
 * Date: 05/10/13
 * Time: 12:21
 * To change this template use File | Settings | File Templates.
 */
public class CycleRentTest extends Neo4jTestCase {
    private int numberOfStations = 0;

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp(true);
        HttpClient client = new HttpClient();
        GetMethod get = null;
        try {
            // we do the http call and parse the xml response
            CycleRentImpl cycleRent = new CycleRentImpl(this.spatial());
            get = new GetMethod(cycleRent.getImportUrl());
            client.executeMethod(get);
            JsonReader reader = new JsonReader(new InputStreamReader(get.getResponseBodyAsStream(), "UTF-8"));
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                while (reader.hasNext()) {
                    reader.skipValue();
                }
                reader.endObject();
                numberOfStations ++;
            }
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        } finally {
            get.releaseConnection();
        }
    }

    @Test
    public void testImport() throws MobilITException {
        CycleRentService service = new CycleRentService(this.spatial());
        CycleRentImpl namur = (CycleRentImpl) service.getGeoService(Constant.NAMUR_GEO_CODE);
        List<POI> libia = namur.importStation();
        assertEquals(numberOfStations, libia.size());
    }

    @Test
    public void testStation() throws MobilITException {
        CycleRentService service = new CycleRentService(this.spatial());
        CycleRentImpl namur = (CycleRentImpl) service.getGeoService(Constant.NAMUR_GEO_CODE);
        Map<String, Integer> result = namur.getStation("" + numberOfStations);
        assertNotNull(result.get(Constant.CYCLE_AVAIBLE));
        assertNotNull(result.get(Constant.CYCLE_FREE));
        assertNotNull(result.get(Constant.CYCLE_TOTAL));
    }

    /*
    @Test
    public void testNearestStation() throws MobilITException {
        CycleRentService service = new CycleRentService(this.spatial());
        CycleRentImpl namur = (CycleRentImpl) service.getGeoService(Constant.NAMUR_GEO_CODE);
        namur.importStation();
        Double lat = new Double(50.46424);
        Double lon = new Double(4.8652);
        POI station = service.getNearest(lon, lat, null);
        assertNotNull(station);
        station = service.getNearest(lon, lat, 10.0, 0);
        assertNotNull(station);
        station = service.getNearest(lon, lat, 10.0, 1);
        assertNotNull(station);
    }*/

    @Test
    public void testNearestStation() throws MobilITException {
        CycleRentImpl nantes = new CycleRentImpl(this.spatial());
        nantes.importStation();

        Double lon = new Double(50.46424);
        Double lat = new Double(4.8652);
        CycleRentService service = new CycleRentService(this.spatial());
        POI station = service.getNearest(lon, lat, null);
        assertNotNull(station);
        station = service.getNearest(lon, lat, 10.0, 0);
        assertNotNull(station);
        station = service.getNearest(lon, lat, 10.0, 1);
        assertNotNull(station);
    }


    @AfterClass
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
