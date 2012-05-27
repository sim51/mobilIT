package fr.mobilit.neo4j.server.utils;

import java.util.List;

import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;

import com.vividsolutions.jts.geom.Coordinate;

import fr.mobilit.neo4j.server.exception.MobilITException;

public class SpatialUtils {

    private SpatialDatabaseService spatial;
    private Layer                  osm;

    public SpatialUtils(SpatialDatabaseService spatial) {
        this.spatial = spatial;
        this.osm = spatial.getLayer(Constant.LAYER_OSM);
    }

    public Node findNearestWay(Double lat, Double lon) throws MobilITException {
        Coordinate coord = new Coordinate(lat, lon);
        //@formatter:off
        List<GeoPipeFlow> results = GeoPipeline
                .startNearestNeighborLatLonSearch(osm, coord, 0.2)
                .sort("OrthodromicDistance")
                .toList();
        //@formatter:on
        Node osmPoint = null;
        if (results.size() > 0) {
            int i = 0;
            Boolean find = false;
            while (find == false & i < results.size()) {
                SpatialDatabaseRecord dbRecord = results.get(i).getRecord();
                Node node = dbRecord.getGeomNode();
                osmPoint = node.getSingleRelationship(DynamicRelationshipType.withName("GEOM"), Direction.INCOMING)
                        .getStartNode();
                if (osmPoint.getRelationships(DynamicRelationshipType.withName("LINKED")).iterator().hasNext()) {
                    find = true;
                }
                i++;
            }
        }
        if (osmPoint == null) {
            throw new MobilITException("Start Node not found");
        }
        return osmPoint;
    }

}
