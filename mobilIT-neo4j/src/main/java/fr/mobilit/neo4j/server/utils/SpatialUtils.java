package fr.mobilit.neo4j.server.utils;

import java.util.Map;

import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.indexprovider.LayerNodeIndex;
import org.neo4j.gis.spatial.indexprovider.SpatialIndexProvider;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

import fr.mobilit.neo4j.server.exception.MobilITException;

public class SpatialUtils {

    private SpatialDatabaseService spatial;
    private Layer                  osm;
    private LayerNodeIndex         osmWayIndex;

    public SpatialUtils(SpatialDatabaseService spatial) {
        this.spatial = spatial;
        this.osm = spatial.getLayer(Constant.LAYER_OSM);
        Map<String, String> config = SpatialIndexProvider.SIMPLE_POINT_CONFIG;
        this.osmWayIndex = new LayerNodeIndex(OSMImporter.INDEX_NAME_WAY, spatial.getDatabase(), config);
    }

    public Node findNearestWay(Double lat, Double lon) throws MobilITException {
        // Map<String, Object> params = new HashMap<String, Object>();
        // params.put(LayerNodeIndex.DISTANCE_IN_KM_PARAMETER, 1000.0);
        // params.put(LayerNodeIndex.POINT_PARAMETER, new Double[] { lat, lon });
        // IndexHits<Node> results = osmWayIndex.query(LayerNodeIndex.WITHIN_DISTANCE_QUERY, params);
        IndexHits<Node> results = osmWayIndex.query(LayerNodeIndex.BBOX_QUERY,
                "[-1.6330234,47.1972992,-1.4951555,47.2818119]");
        if (results.hasNext()) {
            return results.next();
        }
        else {
            throw new MobilITException("Start Node not found");
        }
    }
}
