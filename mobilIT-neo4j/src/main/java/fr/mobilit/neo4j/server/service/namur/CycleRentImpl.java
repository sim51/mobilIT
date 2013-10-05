package fr.mobilit.neo4j.server.service.namur;

import fr.mobilit.neo4j.server.service.jcdecaux.JCDecauxCycleRentImpl;
import fr.mobilit.neo4j.server.utils.Constant;
import org.neo4j.gis.spatial.SpatialDatabaseService;

/**
 * Created with IntelliJ IDEA.
 * User: marcducobu
 * Date: 05/10/13
 * Time: 15:14
 * To change this template use File | Settings | File Templates.
 */
public class CycleRentImpl extends JCDecauxCycleRentImpl {
    /**
     * Constructor.
     *
     * @param spatial
     */
    public CycleRentImpl(SpatialDatabaseService spatial) {
        super(spatial);
        super.setJcdecauxApiKey(Constant.JCD_API_KEY);
        super.setJcdecauxContractName("Namur");
    }
}
