package fr.mobilit.neo4j.server.test;

import java.util.List;

import org.geotools.filter.text.cql2.CQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;

import com.vividsolutions.jts.geom.Coordinate;

import fr.mobilit.neo4j.server.Import;
import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.Constant;

public class Neo4jOsmTest extends Neo4jTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp(true);
        String files = Thread.currentThread().getContextClassLoader().getResource("osm/nantes.osm").getFile();
        new Import(this.graphDb()).osm(files);
    }

    // @Test
    // public void testFindNearestWayWithLayerNodeindex() throws MobilITException, CQLException {
    // // init
    // Double lat = new Double(-1.5551018714904785);
    // Double lon = new Double(47.222570240585576);
    // // get layers
    // Map<String, String> config = new HashMap<String, String>();
    // LayerNodeIndex osmWayIndex = new LayerNodeIndex(Constant.LAYER_OSM, this.spatial().getDatabase(), config);
    // // doing request
    // Map<String, Object> params = new HashMap<String, Object>();
    // params.put(LayerNodeIndex.DISTANCE_IN_KM_PARAMETER, 10.0);
    // params.put(LayerNodeIndex.POINT_PARAMETER, new Double[] { lon, lat });
    // IndexHits<Node> results = osmWayIndex.query(LayerNodeIndex.WITHIN_DISTANCE_QUERY, params);
    // Node node = null;
    // if (results.hasNext()) {
    // node = results.next();
    // }
    // // check
    // assertNotNull(node);
    // }

    @Test
    public void testFindNearestWayWithGeoPipeline() throws MobilITException, CQLException {
        // init
        Double lat = new Double(-1.5551018714904785);
        Double lon = new Double(47.222570240585576);
        Coordinate coord = new Coordinate(lat, lon);
        // get layers
        Layer osmLayer = this.spatial().getLayer(Constant.LAYER_OSM);
        // query
        Long startTime = System.currentTimeMillis();
        List<GeoPipeFlow> results = GeoPipeline
                .start(osmLayer)
                .cqlFilter(
                        "geometryType(the_geom) = 'LineString' and highway ='primary' or highway ='secondary' or highway ='tertiary' or highway ='motorway' or highway ='trunk'")
                .startNearestNeighborLatLonSearch(osmLayer, coord, 1.0).getMin("OrthodromicDistance")
                .copyDatabaseRecordProperties().toList();
        Long endTime = System.currentTimeMillis();
        System.out.println("nearest way found in " + (endTime - startTime) / 1000 + "s");
        // check
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    public void testFindNearestWayWithGeoPipeline2() throws MobilITException, CQLException {
        // init
        Double lat = new Double(-1.5551018714904785);
        Double lon = new Double(47.222570240585576);
        Coordinate coord = new Coordinate(lat, lon);
        // get layers
        Layer osmLayer = this.spatial().getLayer(Constant.LAYER_OSM);
        // query
        Long startTime = System.currentTimeMillis();
        List<GeoPipeFlow> results = GeoPipeline
                .start(osmLayer)
                .cqlFilter(
                        "highway ='primary' or highway ='secondary' or highway ='tertiary' or highway ='motorway' or highway ='trunk'")
                .startNearestNeighborLatLonSearch(osmLayer, coord, 1.0).getMin("OrthodromicDistance")
                .copyDatabaseRecordProperties().toList();
        Long endTime = System.currentTimeMillis();
        System.out.println("nearest way found in " + (endTime - startTime) / 1000 + "s");
        // check
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
