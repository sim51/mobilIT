package fr.mobilit.neo4j.server.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.filter.text.cql2.CQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.indexprovider.LayerNodeIndex;
import org.neo4j.gis.spatial.indexprovider.SpatialIndexProvider;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

import com.vividsolutions.jts.geom.Coordinate;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.Constant;

public class Neo4jOsmTest extends Neo4jTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp(false);
        // String files = Thread.currentThread().getContextClassLoader().getResource("osm/nantes.osm").getFile();
        // new Import(this.graphDb()).osm(files);
    }

    @Test
    public void testFindNearestWayWithLayerNodeindex() throws MobilITException, CQLException {
        // init
        Double lat = new Double(-1.557004);
        Double lon = new Double(47.222265);
        // get layers
        Map<String, String> config = SpatialIndexProvider.SIMPLE_POINT_CONFIG;
        LayerNodeIndex osmWayIndex = new LayerNodeIndex(OSMImporter.INDEX_NAME_WAY, this.spatial().getDatabase(),
                config);
        // doing request
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LayerNodeIndex.DISTANCE_IN_KM_PARAMETER, 10.0);
        params.put(LayerNodeIndex.POINT_PARAMETER, new Double[] { lat, lon });
        IndexHits<Node> results = osmWayIndex.query(LayerNodeIndex.WITHIN_DISTANCE_QUERY, params);
        Node node = null;
        if (results.hasNext()) {
            node = results.next();
        }
        // check
        assertNotNull(node);
    }

    @Test
    public void testFindNearestWayWithGeoPipeline() throws MobilITException, CQLException {
        // init
        Double lat = new Double(-1.5551018714904785);
        Double lon = new Double(47.222570240585576);
        Coordinate coord = new Coordinate(lat, lon);
        // get layers
        Layer osmLayer = this.spatial().getLayer(Constant.LAYER_OSM);
        // query
        List<GeoPipeFlow> results = GeoPipeline
                .startNearestNeighborLatLonSearch(osmLayer, coord, 1.0)
                .cqlFilter(
                        "highway ='primary' or highway ='secondary' or highway ='tertiary' or highway ='motorway' or highway ='trunk'")
                .sort("OrthodromicDistance").getMin("OrthodromicDistance").copyDatabaseRecordProperties().toList();
        // check
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    //
    // @Test
    // public void testFindNearestWayByLayerIndexReaderSearchFunction() throws MobilITException, CQLException {
    // // init
    // Double lat = new Double(-1.557004);
    // Double lon = new Double(47.222265);
    // Coordinate coord = new Coordinate(lat, lon);
    // // get layers & indexes
    // Layer osmLayer = this.spatial().getLayer(Constant.LAYER_OSM);
    // LayerIndexReader rtreeIndex = new SpatialIndexPerformanceProxy(osmLayer.getIndex());
    // // query
    // SearchCQL searchCQLQuery = new SearchCQL(osmLayer, "");
    // SearchRecords results = rtreeIndex.search(searchCQLQuery);
    // // check
    // assertNotNull(results);
    // assertTrue(results.count() > 0);
    // }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
