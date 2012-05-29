package fr.mobilit.neo4j.server.test;

import java.util.List;

import org.geotools.filter.text.cql2.CQLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.vividsolutions.jts.geom.Coordinate;

import fr.mobilit.neo4j.server.Import;
import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.Constant;

public class Neo4jOsmTest extends Neo4jTestCase {

    @BeforeClass
    public void setUp() throws Exception {
        // super.setUp(false);
        super.setUp(true);
        String files = Thread.currentThread().getContextClassLoader().getResource("osm/nantes.osm").getFile();
        new Import(this.graphDb()).osm(files);

    }

    @Test
    public void testFindNearestWayWithGeoPipeline() throws MobilITException, CQLException {
        // init
        Double lat = new Double(-1.5569311380386353);
        Double lon = new Double(47.22245365625265);
        Coordinate coord = new Coordinate(lat, lon);
        // get layers
        Layer osmLayer = this.spatial().getLayer(Constant.LAYER_OSM);
        // query
        Long startTime = System.currentTimeMillis();
        //@formatter:off
        List<GeoPipeFlow> results = GeoPipeline
                .startNearestNeighborLatLonSearch(osmLayer, coord, 0.2)
                .sort("OrthodromicDistance")
                .toList();
        //@formatter:on

        // check
        assertNotNull(results);
        assertTrue(results.size() > 0);
        Boolean find = false;
        int i = 0;
        Relationship nearestRoad = null;
        while (find == false && i < results.size()) {
            SpatialDatabaseRecord dbRecord = results.get(i).getRecord();
            Node node = dbRecord.getGeomNode();
            Node osmPoint = node.getSingleRelationship(DynamicRelationshipType.withName("GEOM"), Direction.INCOMING)
                    .getStartNode();
            for (Relationship relation : osmPoint.getRelationships(DynamicRelationshipType.withName("LINKED"))) {
                if (relation.getProperty("name", null) != null) {
                    nearestRoad = relation;
                    find = true;
                }
            }
            i++;
        }
        Long endTime = System.currentTimeMillis();
        System.out.println("nearest way found in " + (endTime - startTime) + "ms");
        assertEquals("Rue Saint Stanislas", nearestRoad.getProperty("name"));
    }

    @AfterClass
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
