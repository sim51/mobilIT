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
import org.neo4j.gis.spatial.Layer;

import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.Constant;

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
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
