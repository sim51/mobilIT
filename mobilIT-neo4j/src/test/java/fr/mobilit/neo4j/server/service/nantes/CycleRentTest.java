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
package fr.mobilit.neo4j.server.service.nantes;

<<<<<<< HEAD
import java.util.Iterator;
=======
import java.util.ArrayList;
>>>>>>> 8fb40b015727d6be00dfa936dea132071e671321
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.service.CycleRentService;
import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.Constant;

public class CycleRentTest extends Neo4jTestCase {

    private int numberOfStations = 0;


    @BeforeClass
    public void setUp() throws Exception {
        super.setUp(true);
<<<<<<< HEAD
        // import cycle rent POI
        Iterator cycleIter = Constant.CYCLE_SERVICE.keySet().iterator();
        while (cycleIter.hasNext()) {
            String geocode = (String) cycleIter.next();
            CycleRentService service = new CycleRentService(this.spatial());
            service.getGeoService(geocode).importStation();
=======
        HttpClient client = new HttpClient();
        GetMethod get = null;
        try {
            // we do the http call and parse the xml response
            get = new GetMethod(CycleRentImpl.IMPORT_URL);
            client.executeMethod(get);
            javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newInstance();
            javax.xml.stream.XMLStreamReader parser = factory.createXMLStreamReader(get.getResponseBodyAsStream());
            ArrayList<String> currentXMLTags = new ArrayList<String>();
            int depth = 0;
            while (true) {
                int event = parser.next();
                if (event == javax.xml.stream.XMLStreamConstants.END_DOCUMENT) {
                    break;
                }
                switch (event) {
                    case javax.xml.stream.XMLStreamConstants.START_ELEMENT:
                        currentXMLTags.add(depth, parser.getLocalName());
                        String tagPath = currentXMLTags.toString();
                        // here we have a match, so we construct the POI
                        if (tagPath.equals("[carto, markers, marker]")) {
                            numberOfStations++;
                        }
                        depth++;
                        break;
                    case javax.xml.stream.XMLStreamConstants.END_ELEMENT:
                        depth--;
                        currentXMLTags.remove(depth);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        } finally {
            get.releaseConnection();
>>>>>>> 8fb40b015727d6be00dfa936dea132071e671321
        }
    }

    @Test
    public void testImport() throws MobilITException {
        CycleRentImpl nantes = new CycleRentImpl(this.spatial());
        List<POI> bicloo = nantes.importStation();
        assertEquals(numberOfStations, bicloo.size());
    }

    @Test
    public void testStation() throws MobilITException {
<<<<<<< HEAD
        CycleRentService service = new CycleRentService(this.spatial());
        CycleRentImpl nantes = (CycleRentImpl) service.getGeoService(Constant.NANTES_GEO_CODE);
        Map<String, Integer> result = nantes.getStation("103");
=======
        CycleRentImpl nantes = (CycleRentImpl) CycleRent.getService(this.spatial(), Constant.NANTES_GEO_CODE);
        Map<String, Integer> result = nantes.getStation(""+numberOfStations);
>>>>>>> 8fb40b015727d6be00dfa936dea132071e671321
        assertNotNull(result.get(Constant.CYCLE_AVAIBLE));
        assertNotNull(result.get(Constant.CYCLE_FREE));
        assertNotNull(result.get(Constant.CYCLE_TOTAL));
    }

    @Test
    public void testNearestStation() throws MobilITException {
        Double lat = new Double(-1.5569311380386353);
        Double lon = new Double(47.22245365625265);
        CycleRentService service = new CycleRentService(this.spatial());
        CycleRentImpl nantes = (CycleRentImpl) service.getGeoService(Constant.NANTES_GEO_CODE);
        POI station = service.getNearest(lon, lat, 10.0, null);
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
