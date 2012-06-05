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
package fr.mobilit.neo4j.server.service.nantes;

import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.service.ParkingService;
import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.Constant;

public class ParkingTest extends Neo4jTestCase {

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp(true);
    }

    @Test
    public void testImport() throws MobilITException {
        ParkingImpl nantes = new ParkingImpl(this.spatial());
        List<POI> parking = nantes.importParking();
        assertEquals(51, parking.size());
    }

    @Test
    public void testStation() throws MobilITException {
        ParkingImpl nantes = new ParkingImpl(this.spatial());
        Map<String, Integer> result = nantes.getParking("288");
        assertNotNull(result.get(Constant.PARKING_FREE));
        assertNotNull(result.get(Constant.PARKING_TOTAL));
    }

    @Test
    public void testNearestStation() throws MobilITException {
        ParkingImpl nantes = new ParkingImpl(this.spatial());
        nantes.importParking();

        Double lat = new Double(-1.5569311380386353);
        Double lon = new Double(47.22245365625265);
        ParkingService service = new ParkingService(this.spatial());
        POI station = service.getNearest(lon, lat, 100.0, 0);
        assertNotNull(station);
        station = service.getNearest(lon, lat, 100.0, null);
        assertNotNull(station);
    }

    @AfterClass
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
