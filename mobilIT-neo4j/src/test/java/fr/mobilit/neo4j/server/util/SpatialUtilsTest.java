package fr.mobilit.neo4j.server.util;

import org.geotools.filter.text.cql2.CQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.utils.SpatialUtils;

public class SpatialUtilsTest extends Neo4jTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp(false);
    }

    @Test
    public void testFindNearestWay() throws MobilITException, CQLException {
        Double lat = new Double(-1.5551018714904785);
        Double lon = new Double(47.222570240585576);
        Long startTime = System.currentTimeMillis();
        SpatialDatabaseRecord record = new SpatialUtils(this.spatial()).findNearestWay(lat, lon);
        Long endTime = System.currentTimeMillis();
        System.out.println("nearest way found in " + (endTime - startTime) / 1000 + "s");
        assertNotNull(record);
        assertEquals("Rue Paul Bellamy", record.getProperty("name"));
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
