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
        this.searchPlugin = new SearchPath(this.graphDb());
    }

    @Test
    public void testCarPath() {
        Double lat1 = new Double(-1.557004);
        Double long1 = new Double(47.222265);
        Double lat2 = new Double(-1.55341);
        Double long2 = new Double(47.216924);
        Long time = System.currentTimeMillis();
        Response response = searchPlugin.car(lat1, long1, lat2, long2, time);
        assertEquals(200, response.getStatus());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
