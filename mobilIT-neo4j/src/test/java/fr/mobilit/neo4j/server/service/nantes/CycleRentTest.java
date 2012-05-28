package fr.mobilit.neo4j.server.service.nantes;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.utils.Constant;

public class CycleRentTest extends TestCase {

    @Test
    public void testImport() throws MobilITException {
        CycleRentImpl nantes = new CycleRentImpl();
        List<POI> bicloo = nantes.importStation();
        assertEquals(103, bicloo.size());
    }

    @Test
    public void testStation() throws MobilITException {
        CycleRentImpl nantes = new CycleRentImpl();
        Map<String, Integer> result = nantes.getStation("103");
        assertNotNull(result.get(Constant.CYCLE_AVAIBLE));
        assertNotNull(result.get(Constant.CYCLE_FREE));
        assertNotNull(result.get(Constant.CYCLE_TOTAL));
    }

}
