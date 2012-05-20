package fr.mobilit.neo4j.server.utils;

import java.util.List;

import org.geotools.filter.text.cql2.CQLException;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.osm.OSMDataset;
import org.neo4j.gis.spatial.osm.OSMDataset.Way;
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

    public Way findNearestWay(Double lat, Double lon) throws MobilITException {
        try {
            Coordinate coord = new Coordinate(lat, lon);
            //@formatter:off
            List<GeoPipeFlow> results = GeoPipeline
                    .startNearestNeighborLatLonSearch(osm, coord, 0.5)
                    .cqlFilter(
                            "highway ='primary' or " +
                            "highway ='secondary' or " +
                            "highway ='tertiary' or " +
                            "highway ='motorway' or " +
                            "highway ='trunk'")
                    .sort("OrthodromicDistance")
                    .toList();
          //@formatter:on
            if (results.size() > 0) {
                int i = 0;
                Way way = null;
                while (way == null & i < results.size()) {
                    SpatialDatabaseRecord spatialResult = results.get(i).getRecord();
                    Way tempWay = new SpatialUtils(this.spatial).getOSMWayFromGeomNode(spatialResult);
                    if (tempWay != null && tempWay.getNode().getProperty("name", null) != null) {
                        way = tempWay;
                    }
                    i++;
                }
                return way;
            }
            else {
                throw new MobilITException("Start Node not found");
            }
        } catch (CQLException e) {
            throw new MobilITException(e);
        }

    }

    /**
     * Method to get an OSM way from a GeomNode. The way contains all OSM attributs.
     * 
     * @param geomNode
     * @return
     */
    public Way getOSMWayFromGeomNode(SpatialDatabaseRecord spatialResult) {
        return ((OSMDataset) osm.getDataset()).getWayFrom(spatialResult.getGeomNode());
    }
}
