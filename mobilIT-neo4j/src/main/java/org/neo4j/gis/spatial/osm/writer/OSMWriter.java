package org.neo4j.gis.spatial.osm.writer;

import fr.mobilit.neo4j.server.utils.MobilITRelation;
import org.neo4j.collections.rtree.Envelope;
import org.neo4j.gis.spatial.Constants;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.gis.spatial.osm.OSMRelation;
import org.neo4j.gis.spatial.osm.RoadDirection;
import org.neo4j.gis.spatial.osm.utils.GeometryMetaData;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.server.logging.Logger;

import java.util.*;

public abstract class OSMWriter<T> {

    /**
     * The logger.
     */
    protected final static Logger LOG = Logger.getLogger(OSMWriter.class);

    protected T currentNode = null;
    protected T prev_way = null;
    protected T prev_relation = null;
    protected int nodeCount = 0;
    protected int poiCount = 0;
    protected int wayCount = 0;
    protected int relationCount = 0;
    protected int userCount = 0;
    protected int changesetCount = 0;
    protected OSMImporter osmImporter;
    protected T osm_dataset;

    public OSMWriter(OSMImporter osmImporter) {
        this.osmImporter = osmImporter;
    }

    public static OSMWriter<Node> fromGraphDatabase(GraphDatabaseService graphDb, OSMImporter osmImporter, int txInterval) {
        return new OSMGraphWriter(graphDb, osmImporter, txInterval);
    }

    public abstract T getOrCreateNode(String name, String type, T parent, RelationshipType relType);

    public abstract T getOrCreateOSMDataset(String name);

    public abstract void setDatasetProperties(Map<String, Object> extractProperties);

    public abstract void addNodeTags(T node, LinkedHashMap<String, Object> tags, String type);

    public abstract void addNodeGeometry(T node, int gtype, Envelope bbox, int vertices);

    public abstract T getSingleNode(String name, String string, Object value);

    public abstract Map<String, Object> getNodeProperties(T member);

    public abstract T getOSMNode(long osmId, T changesetNode);

    public abstract void updateGeometryMetaDataFromMember(T member, GeometryMetaData metaGeom,
                                                          Map<String, Object> nodeProps);

    public abstract void finish();

    public abstract T getChangesetNode(Map<String, Object> nodeProps);

    public abstract T getUserNode(Map<String, Object> nodeProps);

    /**
     * Create and index node <code>name</code>
     *
     * @param name
     * @param properties
     * @param indexKey
     * @return
     */
    public abstract T addNode(String name, Map<String, Object> properties, String indexKey);

    /**
     * Create and index node <code>name</code>
     *
     * @param indexName which index to be updated
     * @param node      the node to be indexed
     * @param indexKeys the indexed keys (which have their values in properties)
     * @return
     */
    public abstract T index(String indexName, T node, Set<String> indexKeys);

    public abstract void createRelationship(T from, T to, RelationshipType relType, LinkedHashMap<String, Object> relProps);

    public abstract long getDatasetId();

    public void createRelationship(T from, T to, RelationshipType relType) {
        createRelationship(from, to, relType, null);
    }

    /**
     * Add the BBox metadata to the dataset
     *
     * @param bboxProperties
     */
    public void addOSMBBox(Map<String, Object> bboxProperties) {
        T bbox = addNode("bbox", bboxProperties, null);
        createRelationship(osm_dataset, bbox, OSMRelation.BBOX);
    }

    /**
     * Create a new OSM node from the specified attributes (including location, user, changeset). The node is stored in
     * the currentNode field, so that it can be used in the subsequent call to addOSMNodeTags after we close the XML tag
     * for OSM nodes.
     *
     * @param nodeProps HashMap of attributes for the OSM-node
     */
    public void createOSMNode(Map<String, Object> nodeProps) {
        T changesetNode = getChangesetNode(nodeProps);
        currentNode = addNode("node", nodeProps, "node_osm_id");
        createRelationship(currentNode, changesetNode, OSMRelation.CHANGESET);
        nodeCount++;
        LOG.debug("Create OSM node " + currentNode.toString());
    }

    public void addOSMNodeTags(boolean allPoints, LinkedHashMap<String, Object> currentNodeTags) {
        currentNodeTags.remove("created_by"); // redundant information
        // Nodes with tags get added to the index as point geometries
        if (allPoints || currentNodeTags.size() > 0) {
            Map<String, Object> nodeProps = getNodeProperties(currentNode);
            Envelope bbox = new Envelope();
            double[] location = new double[]{(Double) nodeProps.get("lon"), (Double) nodeProps.get("lat")};
            bbox.expandToInclude(location[0], location[1]);
            addNodeGeometry(currentNode, Constants.GTYPE_POINT, bbox, 1);
            poiCount++;
        }
        addNodeTags(currentNode, currentNodeTags, "node");
    }

    public void createOSMWay(Map<String, Object> wayProperties, ArrayList<Long> wayNodes, LinkedHashMap<String, Object> wayTags) {
        RoadDirection direction = osmImporter.isOneway(wayTags);
        String name = (String) wayTags.get("name");
        int geometry = Constants.GTYPE_LINESTRING;
        boolean isRoad = wayTags.containsKey("highway");
        if (isRoad) {
            wayProperties.put("oneway", direction.toString());
            wayProperties.put("highway", wayTags.get("highway"));
        }
        if (name != null) {
            // Copy name tag to way because this seems like a valuable location for
            // such a property
            wayProperties.put("name", name);
        }
        String way_osm_id = (String) wayProperties.get("way_osm_id");
        T changesetNode = getChangesetNode(wayProperties);
        T way = addNode(osmImporter.INDEX_NAME_WAY, wayProperties, "way_osm_id");


        index(osmImporter.INDEX_NAME_WAY, way, Collections.singleton("name"));

        createRelationship(way, changesetNode, OSMRelation.CHANGESET);
        if (prev_way == null) {
            createRelationship(osm_dataset, way, OSMRelation.WAYS);
        } else {
            createRelationship(prev_way, way, OSMRelation.NEXT);
        }
        prev_way = way;
        // addNodeTags(way, wayTags, "way");
        Envelope bbox = new Envelope();
        T firstNode = null;
        T prevNode = null;
        Map<String, Object> prevProps = null;
        LinkedHashMap<String, Object> relProps = new LinkedHashMap<String, Object>();
        for (long nd_ref : wayNodes) {
            T pointNode = getOSMNode(nd_ref, changesetNode);

            // This can happen if we import not whole planet, so some referenced nodes will be unavailable
            if (pointNode == null) {
                LOG.warn("Missing node with ref " + nd_ref);
                continue;
            }

            if (firstNode == null) {
                firstNode = pointNode;
            }
            if (prevNode == pointNode) {
                continue;
            }
            // createRelationship(proxyNode, pointNode, OSMRelation.NODE, null);
            Map<String, Object> nodeProps = getNodeProperties(pointNode);
            double[] location = new double[]{(Double) nodeProps.get("lon"), (Double) nodeProps.get("lat")};
            bbox.expandToInclude(location[0], location[1]);
            if (prevNode == null) {
                createRelationship(way, firstNode, OSMRelation.FIRST_NODE);
            } else {
                relProps.clear();
                double[] prevLoc = new double[]{(Double) prevProps.get("lon"), (Double) prevProps.get("lat")};

                double length = osmImporter.distance(prevLoc[0], prevLoc[1], location[0], location[1]);
                relProps.put("length", length);
                Iterator<String> wayTagPropIter = wayTags.keySet().iterator();
                while (wayTagPropIter.hasNext()) {
                    String key = wayTagPropIter.next();
                    relProps.put(key, wayTags.get(key));
                }
                relProps.remove("oneway");
                relProps.put("oneway", direction.toString());

                // We default to bi-directional (and don't store direction in the
                // way node), but if it is one-way we mark it as such, and define
                // the direction using the relationship direction
                if (direction == RoadDirection.BACKWARD) {
                    createRelationship(pointNode, prevNode, MobilITRelation.LINKED, relProps);
                } else {
                    createRelationship(prevNode, pointNode, MobilITRelation.LINKED, relProps);
                }
            }
            prevNode = pointNode;
            prevProps = nodeProps;
        }
        wayTags.clear();
        if (prevNode != null) {
            createRelationship(way, prevNode, OSMRelation.LAST_NODE);
        }
        if (firstNode != null && prevNode == firstNode) {
            geometry = Constants.GTYPE_POLYGON;
        }
        if (wayNodes.size() < 2) {
            geometry = Constants.GTYPE_POINT;
        }
        addNodeGeometry(way, geometry, bbox, wayNodes.size());
        this.wayCount++;
    }

    public void createOSMRelation(Map<String, Object> relationProperties, ArrayList<Map<String, Object>> relationMembers, LinkedHashMap<String, Object> relationTags) {
        String name = (String) relationTags.get("name");
        if (name != null) {
            // Copy name tag to way because this seems like a valuable location for
            // such a property
            relationProperties.put("name", name);
        }
        T relation = addNode("relation", relationProperties, "relation_osm_id");
        if (prev_relation == null) {
            createRelationship(osm_dataset, relation, OSMRelation.RELATIONS);
        } else {
            createRelationship(prev_relation, relation, OSMRelation.NEXT);
        }
        prev_relation = relation;
        addNodeTags(relation, relationTags, "relation");
        // We will test for cases that invalidate multilinestring further down
        GeometryMetaData metaGeom = new GeometryMetaData(Constants.GTYPE_MULTILINESTRING);
        T prevMember = null;
        LinkedHashMap<String, Object> relProps = new LinkedHashMap<String, Object>();
        for (Map<String, Object> memberProps : relationMembers) {
            String memberType = (String) memberProps.get("type");
            long member_ref = Long.parseLong(memberProps.get("ref").toString());
            if (memberType != null) {
                T member = getSingleNode(memberType, memberType + "_osm_id", member_ref);

                // This can happen if we import not whole planet, so some referenced nodes will be unavailable
                if (null == member || prevMember == member) {
                    LOG.warn("Missing member " + memberProps.toString());
                    continue;
                }

                if (member == relation) {
                    LOG.error("Cannot add relation to same member: relation[" + relationTags + "] - member[" + memberProps + "]");
                    continue;
                }

                Map<String, Object> nodeProps = getNodeProperties(member);
                if (memberType.equals("node")) {
                    double[] location = new double[]{(Double) nodeProps.get("lon"), (Double) nodeProps.get("lat")};
                    metaGeom.expandToIncludePoint(location);
                }
                else if (memberType.equals("nodes")) {
                    System.err.println("Unexpected 'nodes' member type");
                }
                else {
                    updateGeometryMetaDataFromMember(member, metaGeom, nodeProps);
                }

                relProps.clear();
                String role = (String) memberProps.get("role");
                if (role != null && role.length() > 0) {
                    relProps.put("role", role);
                    if (role.equals("outer")) {
                        metaGeom.setPolygon();
                    }
                }
                createRelationship(relation, member, OSMRelation.MEMBER, relProps);
                prevMember = member;
            } else {
                System.err.println("Cannot process invalid relation member: " + memberProps.toString());
            }
        }
        if (metaGeom.isValid()) {
            addNodeGeometry(relation, metaGeom.getGeometryType(), metaGeom.getBBox(), metaGeom.getVertices());
        }
        this.relationCount++;
    }

}
