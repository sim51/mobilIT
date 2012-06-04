package fr.mobilit.neo4j.server.utils;

import java.util.List;

import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.EditableLayerImpl;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.encoders.SimplePointEncoder;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import com.vividsolutions.jts.geom.Coordinate;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;

public class SpatialUtils {

    private SpatialDatabaseService spatial;
    private Layer                  osm;

    /**
     * Constructor.
     */
    public SpatialUtils(SpatialDatabaseService spatial) {
        this.spatial = spatial;
        this.osm = spatial.getLayer(Constant.LAYER_OSM);
    }

    /**
     * Find the nearest OSM way node from a coordinate (lng, lat).
     * 
     * @param lat
     * @param lon
     * @return
     * @throws MobilITException
     */
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

    /**
     * Method to get or create a node if it doesnt exist.
     * 
     * @param name
     * @param type
     * @param parent
     * @param relType
     * @return
     */
    public Node getOrCreateNode(String name, String type, Node parent, RelationshipType relType) {
        Node node = findNode(name, parent, relType);
        if (node == null) {
            Transaction tx = this.spatial.getDatabase().beginTx();
            node = this.spatial.getDatabase().createNode();
            node.setProperty("name", name);
            node.setProperty("type", type);
            parent.createRelationshipTo(node, relType);
            tx.success();
            tx.finish();
        }
        return node;
    }

    /**
     * Find a child node from its parent and relation.
     * 
     * @param name
     * @param parent
     * @param relType
     * @return
     */
    private Node findNode(String name, Node parent, RelationshipType relType) {
        for (Relationship relationship : parent.getRelationships(relType, Direction.OUTGOING)) {
            Node node = relationship.getEndNode();
            if (name.equals(node.getProperty("name"))) {
                return node;
            }
        }
        return null;
    }

    /**
     * Save all POI into the specified layer.
     * 
     * @param layerName
     * @param list
     * @param geocode
     */
    public void savePOIToLayer(String layerName, List<POI> list, String geocode) {
        // create layer
        EditableLayer layer;
        if (!this.spatial.containsLayer(layerName)) {
            layer = (EditableLayer) this.spatial.createLayer(layerName, SimplePointEncoder.class,
                    EditableLayerImpl.class, "lon:lat");
        }
        else {
            layer = (EditableLayer) this.spatial.getLayer(layerName);
        }

        Transaction tx = this.spatial.getDatabase().beginTx();
        for (int i = 0; i < list.size(); i++) {
            // create data node
            POI currentStation = list.get(i);
            Node currentNode = this.spatial.getDatabase().createNode();
            currentNode.setProperty("name", currentStation.getName());
            currentNode.setProperty("lat", currentStation.getGeoPoint().getLatitude());
            currentNode.setProperty("lon", currentStation.getGeoPoint().getLongitude());
            currentNode.setProperty("geocode", geocode);
            currentNode.setProperty("id", currentStation.getId());

            // save geom into layer
            layer.add(currentNode);
        }
        tx.success();
        tx.finish();
    }

}
