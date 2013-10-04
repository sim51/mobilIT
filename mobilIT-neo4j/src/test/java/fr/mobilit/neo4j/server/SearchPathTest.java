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

import fr.mobilit.neo4j.server.util.Neo4jTestCase;

public class SearchPathTest extends Neo4jTestCase {

    private SearchPath searchPlugin;

    @Before
    public void setUp() throws Exception {
        super.setUp(false);
        String files = Thread.currentThread().getContextClassLoader().getResource("osm/nantes.osm").getFile();
        new Import(this.graphDb()).osm(files);
        this.searchPlugin = new SearchPath(this.graphDb());
    }

    @Test
    public void testCarPath() {
        Double lat1 = new Double(-1.5569311380386353);
        Double lon1 = new Double(47.22245365625265);
        Double lat2 = new Double(-1.5539807081222534);
        Double lon2 = new Double(47.21921469525527);
        Long time = System.currentTimeMillis();
        Response response = searchPlugin.car(lat1, lon1, lat2, lon2);
        assertEquals(200, response.getStatus());
    }

    @Test
    public  void testCyclePath() {
        Double lat1 = new Double(-1.5569311380386353);
        Double lon1 = new Double(47.22245365625265);
        Double lat2 = new Double(-1.5539807081222534);
        Double lon2 = new Double(47.21921469525527);
        Long time = System.currentTimeMillis();
        Response response = searchPlugin.cycle(lat1, lon1, lat2, lon2);
        assertEquals(200, response.getStatus());
    }

    @Test
    public  void testPedestrianPath() {
        Double lat1 = new Double(-1.5569311380386353);
        Double lon1 = new Double(47.22245365625265);
        Double lat2 = new Double(-1.5539807081222534);
        Double lon2 = new Double(47.21921469525527);
        Long time = System.currentTimeMillis();
        Response response = searchPlugin.pedestrian(lat1, lon1, lat2, lon2);
        assertEquals(200, response.getStatus());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
