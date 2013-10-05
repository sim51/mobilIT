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

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.SpatialUtils;
import org.geotools.filter.text.cql2.CQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

import javax.ws.rs.core.Response;

public class NamurSearchPathTest extends Neo4jTestCase {

    private SearchPath searchPlugin;

    @Before
    public void setUp() throws Exception {
        super.setUp(false);
        String files = Thread.currentThread().getContextClassLoader().getResource("osm/namur.osm").getFile();
        new Import(this.graphDb()).osm(files);
        this.searchPlugin = new SearchPath(this.graphDb());
    }

    @Test
    public void testFindNearestWay1() throws MobilITException, CQLException {
        Double lat = new Double(50.4705314);
        Double lon = new Double(4.825353000000007);
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
        assertEquals("Chaussée de Marche", nearestRoad.getProperty("name"));
    }

    @Test
    public void testFindNearestWay2() throws MobilITException, CQLException {
        Double lat = new Double(50.4673823);
        Double lon = new Double(4.86544649999999);
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
       // assertEquals("", nearestRoad.getProperty("name"));
    }

    @Test
    public void testCarPath() {
        Double lat1 = new Double(50.4721675);
        Double lon1 = new Double(4.825353000000007);
        Double lat2 = new Double(50.4673823);
        Double lon2 = new Double(4.86561549999999);
        Long time = System.currentTimeMillis();
        Response response = searchPlugin.car(lat1, lon1, lat2, lon2);
        assertEquals(200, response.getStatus());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
