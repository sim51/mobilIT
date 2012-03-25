package fr.mobilit.neo4j.server;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.gis.spatial.osm.OSMGeometryEncoder;
import org.neo4j.gis.spatial.osm.OSMLayer;

import util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.Constant;

public class ImportTest extends Neo4jTestCase {

    private Import importPlugin;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.importPlugin = new Import(this.graphDb());
    }

    @Test
    public void testOSMImport() {
        String files = Thread.currentThread().getContextClassLoader().getResource("osm/nantes.osm").getFile();
        Response response = importPlugin.osm(files);
        assertEquals(200, response.getStatus());
        OSMLayer layer = (OSMLayer) this.spatial().getOrCreateLayer(Constant.LAYER_OSM, OSMGeometryEncoder.class,
                OSMLayer.class);
        assertNotNull("OSM Layer index should not be null", layer.getIndex());
        assertNotNull("OSM Layer index envelope should not be null", layer.getIndex().getBoundingBox());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
