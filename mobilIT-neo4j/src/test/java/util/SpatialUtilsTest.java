package util;

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
import org.neo4j.graphdb.Transaction;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.utils.Constant;
import fr.mobilit.neo4j.server.utils.SpatialUtils;

public class SpatialUtilsTest extends Neo4jTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp(false);
    }

    @Test
    public void testFindNearestWay() throws MobilITException, CQLException {
        Double lat = new Double(-1.557004);
        Double lon = new Double(47.222265);
        Long time = System.currentTimeMillis();
        Layer osmLayer = this.spatial().getLayer(Constant.LAYER_OSM);
        List<GeoPipeFlow> results = GeoPipeline.start(osmLayer).cqlFilter("highway = 'secondary'").toList();
        Map<String, String> config = SpatialIndexProvider.SIMPLE_POINT_CONFIG;
        LayerNodeIndex osmWayIndex = new LayerNodeIndex(OSMImporter.INDEX_NAME_WAY, this.spatial().getDatabase(),
                config);
        Transaction tx = this.spatial().getDatabase().beginTx();
        for (GeoPipeFlow item : results) {
            osmWayIndex.add(item.getRecord().getGeomNode(), "way_osm_id", item.getProperties());
        }
        tx.finish();
        Node node = new SpatialUtils(this.spatial()).findNearestWay(lat, lon);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
