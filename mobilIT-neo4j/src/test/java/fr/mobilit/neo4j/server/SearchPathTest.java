package fr.mobilit.neo4j.server;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.mobilit.neo4j.server.util.Neo4jTestCase;

public class SearchPathTest extends Neo4jTestCase {

    private SearchPath searchPlugin;

    @Before
    public void setUp() throws Exception {
        super.setUp(false);
        String files = Thread.currentThread().getContextClassLoader().getResource("osm/nantes.osm").getFile();
        new Import(this.graphDb()).osm(files);
        this.searchPlugin = new SearchPath(this.graphDb());
    }

    @Test
    public void testCarPath() {
        Double lat1 = new Double(-1.5569311380386353);
        Double lon1 = new Double(47.22245365625265);
        Double lat2 = new Double(-1.5539807081222534);
        Double lon2 = new Double(47.21921469525527);
        Long time = System.currentTimeMillis();
        Response response = searchPlugin.car(lat1, lon1, lat2, lon2);
        assertEquals(200, response.getStatus());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
