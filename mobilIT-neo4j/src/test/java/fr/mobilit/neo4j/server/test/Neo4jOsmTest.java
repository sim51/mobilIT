package fr.mobilit.neo4j.server.test;

import java.util.List;

import org.geotools.filter.text.cql2.CQLException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.osm.OSMDataset.Way;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.gis.spatial.pipes.osm.OSMGeoPipeline;

import com.vividsolutions.jts.geom.Coordinate;

import fr.mobilit.neo4j.server.Import;
import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.Constant;
import fr.mobilit.neo4j.server.utils.SpatialUtils;

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
        Layer osmLayer = this.spatial().getLayer(Constant.LAYER_CAR_HIGHWAY_OSM);
        // query
        Long startTime = System.currentTimeMillis();
        //@formatter:off
        List<GeoPipeFlow> results = GeoPipeline
                .startNearestNeighborLatLonSearch(osmLayer, coord, 0.5)
                .cqlFilter(
                        "highway ='primary' or " +
                        "highway ='secondary' or " +
                        "highway ='tertiary' or " +
                        "highway ='motorway' or " +
                        "highway ='trunk'")
                .getMin("OrthodromicDistance")
                .copyDatabaseRecordProperties()
                .toList();
        //@formatter:on
        Long endTime = System.currentTimeMillis();
        System.out.println("nearest way found in " + (endTime - startTime) / 1000 + "s");
        // check
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertEquals("Rue Paul Bellamy", results.get(0).getRecord().getProperty("name"));
    }

    @Test
    public void testFindNearestWayWithGeoPipeline2() throws MobilITException, CQLException {
        // init
        Double lat = new Double(-1.5569311380386353);
        Double lon = new Double(47.22245365625265);
        Coordinate coord = new Coordinate(lat, lon);
        // get layers
        Layer osmLayer = this.spatial().getLayer(Constant.LAYER_CAR_HIGHWAY_OSM);
        // query
        Long startTime = System.currentTimeMillis();
        //@formatter:off
        List<GeoPipeFlow> results = GeoPipeline
                .startNearestNeighborLatLonSearch(osmLayer, coord, 0.5)
                .getMin("OrthodromicDistance")
                .copyDatabaseRecordProperties()
                .toList();
        //@formatter:on
        Long endTime = System.currentTimeMillis();
        System.out.println("nearest way found in " + (endTime - startTime) / 1000 + "s");
        // check
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertEquals("Rue Paul Bellamy", results.get(0).getRecord().getProperty("name"));
    }

    @Test
    public void testFindNearestWayWithGeoPipeline3() throws MobilITException, CQLException {
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
                .startNearestNeighborLatLonSearch(osmLayer, coord, 0.5)
                .cqlFilter(
                        "highway ='primary' or " +
                        "highway ='secondary' or " +
                        "highway ='tertiary' or " +
                        "highway ='motorway' or " +
                        "highway ='trunk'")
                .getMin("OrthodromicDistance")
                .copyDatabaseRecordProperties().toList();
      //@formatter:on
        Long endTime = System.currentTimeMillis();
        System.out.println("nearest way found in " + (endTime - startTime) / 1000 + "s");
        // check
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertEquals("Rue Paul Bellamy", results.get(0).getRecord().getProperty("name"));
    }

    @Test
    public void testFindNearestWayWithGeoPipeline4() throws MobilITException, CQLException {
        // init
        Double lat = new Double(-1.5569311380386353);
        Double lon = new Double(47.22245365625265);
        Coordinate coord = new Coordinate(lat, lon);
        // get layers
        OSMLayer osmLayer = (OSMLayer) this.spatial().getDynamicLayer(Constant.LAYER_OSM);
        // query
        Long startTime = System.currentTimeMillis();
        //@formatter:off
        List<GeoPipeFlow> results = OSMGeoPipeline
                .start(osmLayer)
                .cqlFilter(
                        "highway ='primary' or " +
                        "highway ='secondary' or " +
                        "highway ='tertiary' or " +
                        "highway ='motorway' or " +
                        "highway ='trunk'")
                .startNearestNeighborLatLonSearch(osmLayer, coord, 0.5)
                .sort("OrthodromicDistance")
                .toList();
        //@formatter:on
        Long endTime = System.currentTimeMillis();
        boolean find = false;
        int i = 0;
        Way way = null;
        while (find == false & i < results.size()) {
            SpatialDatabaseRecord spatialResult = results.get(i).getRecord();
            way = new SpatialUtils(this.spatial()).getOSMWayFromGeomNode(spatialResult);
            if (way != null && way.getNode().getProperty("name", null) != null) {
                find = true;
            }
            i++;
        }
        System.out.println("nearest way found in " + (endTime - startTime) / 1000 + "s");
        // check
        assertEquals("Rue Paul Bellamy", way.getNode().getProperty("name"));
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
