package fr.mobilit.neo4j.server.util;

import org.geotools.filter.text.cql2.CQLException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import fr.mobilit.neo4j.server.Import;
import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.utils.SpatialUtils;

public class SpatialUtilsTest extends Neo4jTestCase {

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp(true);
        String files = Thread.currentThread().getContextClassLoader().getResource("osm/nantes.osm").getFile();
        new Import(this.graphDb()).osm(files);
    }

    @Test
    public void testFindNearestWay() throws MobilITException, CQLException {
        Double lat = new Double(-1.5569311380386353);
        Double lon = new Double(47.22245365625265);
        Long startTime = System.currentTimeMillis();
        Node node = new SpatialUtils(this.spatial()).findNearestWay(lat, lon);
        Long endTime = System.currentTimeMillis();
        System.out.println("nearest way found in " + (endTime - startTime) / 1000 + "s");
        assertNotNull(node);
        Relationship nearestRoad = null;
        for (Relationship relation : node.getRelationships(DynamicRelationshipType.withName("LINKED"))) {
            if (relation.getProperty("name", null) != null)
                nearestRoad = relation;
        }
        assertEquals("Rue Saint Stanislas", nearestRoad.getProperty("name"));
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
