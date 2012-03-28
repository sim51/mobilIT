package fr.mobilit.neo4j.server.utils;

import java.util.List;

import org.geotools.filter.text.cql2.CQLException;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;

import com.vividsolutions.jts.geom.Coordinate;

import fr.mobilit.neo4j.server.exception.MobilITException;

public class SpatialUtils {

    private SpatialDatabaseService spatial;
    private Layer                  osm;

    public SpatialUtils(SpatialDatabaseService spatial) {
        this.spatial = spatial;
        this.osm = spatial.getLayer(Constant.LAYER_OSM);
    }

    public SpatialDatabaseRecord findNearestWay(Double lat, Double lon) throws MobilITException {
        try {
            Coordinate coord = new Coordinate(lat, lon);
            //@formatter:off
            List<GeoPipeFlow> results;
            results = GeoPipeline
                    .startNearestNeighborLatLonSearch(osm, coord, 1.0)
                    .cqlFilter(
                            "highway ='primary' or " +
                            "highway ='secondary' or " +
                            "highway ='tertiary' or " +
                            "highway ='motorway' or " +
                            "highway ='trunk'")
                    .sort("OrthodromicDistance")
                    .getMin("OrthodromicDistance")
                    .copyDatabaseRecordProperties()
                    .toList();
            //@formatter:on
            if (results.size() > 0) {
                return results.get(0).getRecord();
            }
            else {
                throw new MobilITException("Start Node not found");
            }
        } catch (CQLException e) {
            throw new MobilITException(e);
        }

    }
}
