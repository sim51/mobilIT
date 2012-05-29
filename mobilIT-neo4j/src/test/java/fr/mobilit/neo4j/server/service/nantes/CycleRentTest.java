package fr.mobilit.neo4j.server.service.nantes;

import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.service.CycleRent;
import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import fr.mobilit.neo4j.server.utils.Constant;

public class CycleRentTest extends Neo4jTestCase {

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp(false);
    }

    @Test
    public void testImport() throws MobilITException {
        CycleRentImpl nantes = new CycleRentImpl(this.spatial());
        List<POI> bicloo = nantes.importStation();
        assertEquals(103, bicloo.size());
    }

    @Test
    public void testStation() throws MobilITException {
        CycleRentImpl nantes = (CycleRentImpl) CycleRent.getService(this.spatial(), Constant.NANTES_GEO_CODE);
        Map<String, Integer> result = nantes.getStation("103");
        assertNotNull(result.get(Constant.CYCLE_AVAIBLE));
        assertNotNull(result.get(Constant.CYCLE_FREE));
        assertNotNull(result.get(Constant.CYCLE_TOTAL));
    }

    @AfterClass
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
