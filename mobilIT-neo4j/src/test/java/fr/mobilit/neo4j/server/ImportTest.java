/**
 * This file is part of MobilIT.
 *
 * MobilIT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MobilIT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MobilIT. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @See https://github.com/sim51/mobilIT
 */
package fr.mobilit.neo4j.server;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.collections.indexprovider.NodeIndexHits;
import org.neo4j.gis.spatial.Layer;

import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.Constant;
import org.neo4j.gis.spatial.SpatialDataset;
import org.neo4j.gis.spatial.osm.OSMDataset;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.QueryContext;

import java.util.Iterator;

public class ImportTest extends Neo4jTestCase {

    private Import importPlugin;

    @Before
    public void setUp() throws Exception {
        super.setUp(true);
        this.importPlugin = new Import(this.graphDb());
    }

    @Test
    public void testOSMImport() {
        String files = Thread.currentThread().getContextClassLoader().getResource("osm/nantes.osm").getFile();
        Response response = importPlugin.osm(files);
        assertEquals(200, response.getStatus());

        Layer layer = this.spatial().getLayer(Constant.LAYER_OSM);
        assertNotNull("OSM Layer index should not be null", layer.getIndex());
        assertNotNull("OSM Layer index envelope should not be null", layer.getIndex().getBoundingBox());

        assertTrue("The spatial layer must be an OSMLayer. Got a "+ layer.getClass().getName(), layer instanceof OSMLayer);
        OSMLayer osmLayer = (OSMLayer) layer;
        SpatialDataset dataset = osmLayer.getDataset();
        assertTrue("The dataset for the spatial layer must be an instance of OSMLayer. Got a " + dataset.getClass().getName(), dataset instanceof OSMDataset);
        OSMDataset osmDataset = (OSMDataset) dataset;
        Iterator<Node> allWayNodes = osmDataset.getAllWayNodes().iterator();
        assertTrue("The osm dataset should be much more than zero ways...", allWayNodes.hasNext());
        Node found = null;
        while (allWayNodes.hasNext() && found == null) {
            Node next = allWayNodes.next();
            if (next.hasProperty("name")) {
                found = next;
            }
        }
        assertNotNull("No ways with name property found !!", found);
        Object nameO = found.getProperty("name");
        assertTrue("Name property of found way must be a string. Got " + nameO.getClass().getName(), nameO instanceof String);
        String name = (String) nameO;
        assertFalse("Oh... got a void name for found way !", "".equals(name));

        //the name should be indexed in the "way" node index
        Index<Node> wayIndex = this.graphDb().index().forNodes(OSMImporter.INDEX_NAME_WAY);
        IndexHits<Node> hits = wayIndex.query("name:\""+name+"\"");
        assertTrue("Finding the way using its name and the node index 'way' should return at least one node for name : " + name, hits.size() >= 1);
        boolean foundBack = false;
        for (Node hit : hits) {
            if (hit.getId() == found.getId()) {
                foundBack = true;
                break;
            }
        }
        assertTrue("Cannot find the way using its name and the node index 'way'", foundBack);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
