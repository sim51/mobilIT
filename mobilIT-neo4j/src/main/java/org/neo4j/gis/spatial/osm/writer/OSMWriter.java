package org.neo4j.gis.spatial.osm.writer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.neo4j.collections.rtree.Envelope;
import org.neo4j.gis.spatial.Constants;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.gis.spatial.osm.OSMRelation;
import org.neo4j.gis.spatial.osm.RoadDirection;
import org.neo4j.gis.spatial.osm.utils.GeometryMetaData;
import org.neo4j.gis.spatial.osm.utils.StatsManager;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import fr.mobilit.neo4j.server.utils.MobilITRelation;

public abstract class OSMWriter<T> {

    protected HashMap<String, Integer>    stats            = new HashMap<String, Integer>();
    protected HashMap<String, LogCounter> nodeFindStats    = new HashMap<String, LogCounter>();
    protected long                        logTime          = 0;
    protected long                        findTime         = 0;
    protected long                        firstFindTime    = 0;
    protected long                        lastFindTime     = 0;
    protected long                        firstLogTime     = 0;
    protected static int                  foundNodes       = 0;
    protected static int                  createdNodes     = 0;
    protected int                         foundOSMNodes    = 0;
    protected int                         missingUserCount = 0;
    protected T                           currentNode      = null;
    protected T                           prev_way         = null;
    protected T                           prev_relation    = null;
    protected int                         nodeCount        = 0;
    protected int                         poiCount         = 0;
    protected int                         wayCount         = 0;
    protected int                         relationCount    = 0;
    protected int                         userCount        = 0;
    protected int                         changesetCount   = 0;
    protected StatsManager                statsManager;
    protected OSMImporter                 osmImporter;
    protected T                           osm_dataset;

    public OSMWriter(StatsManager statsManager, OSMImporter osmImporter) {
        this.statsManager = statsManager;
        this.osmImporter = osmImporter;
    }

    public static OSMWriter<Node> fromGraphDatabase(GraphDatabaseService graphDb, StatsManager stats,
            OSMImporter osmImporter, int txInterval) {
        return new OSMGraphWriter(graphDb, stats, osmImporter, txInterval);
    }

    public abstract T getOrCreateNode(String name, String type, T parent, RelationshipType relType);

    public abstract T getOrCreateOSMDataset(String name);

    public abstract void setDatasetProperties(Map<String, Object> extractProperties);

    public abstract void addNodeTags(T node, LinkedHashMap<String, Object> tags, String type);

    public abstract void addNodeGeometry(T node, int gtype, Envelope bbox, int vertices);

    /**
     * Create and index node <code>name</code>
     * 
     * @param name
     * @param properties
     * @param indexKey
     * @return
     */
    public abstract T addNode(String name, Map<String, Object> properties, String indexKey);

    public abstract void createRelationship(T from, T to, RelationshipType relType,
            LinkedHashMap<String, Object> relProps);

    public void createRelationship(T from, T to, RelationshipType relType) {
        createRelationship(from, to, relType, null);
    }

    public void logMissingUser(Map<String, Object> nodeProps) {
        if (missingUserCount++ < 10) {
            System.err.println("Missing user or uid: " + nodeProps.toString());
        }
    }

    private class LogCounter {

        private long count     = 0;
        private long totalTime = 0;
    }

    protected void logNodeFoundFrom(String key) {
        LogCounter counter = nodeFindStats.get(key);
        if (counter == null) {
            counter = new LogCounter();
            nodeFindStats.put(key, counter);
        }
        counter.count++;
        foundOSMNodes++;
        long currentTime = System.currentTimeMillis();
        if (lastFindTime > 0) {
            counter.totalTime += currentTime - lastFindTime;
        }
        lastFindTime = currentTime;
        logNodesFound(currentTime);
    }

    protected void logNodesFound(long currentTime) {
        if (firstFindTime == 0) {
            firstFindTime = currentTime;
            findTime = currentTime;
        }
        if (currentTime == 0 || currentTime - findTime > 1432) {
            int duration = 0;
            if (currentTime > 0) {
                duration = (int) ((currentTime - firstFindTime) / 1000);
            }
            System.out.println(new Date(currentTime) + ": Found " + foundOSMNodes + " nodes during " + duration
                    + "s way creation: ");
            for (String type : nodeFindStats.keySet()) {
                LogCounter found = nodeFindStats.get(type);
                double rate = 0.0f;
                if (found.totalTime > 0) {
                    rate = (1000.0 * (float) found.count / (float) found.totalTime);
                }
                System.out.println("\t" + type + ": \t" + found.count + "/" + (found.totalTime / 1000) + "s" + " \t("
                        + rate + " nodes/second)");
            }
            findTime = currentTime;
        }
    }

    protected void logNodeAddition(LinkedHashMap<String, Object> tags, String type) {
        Integer count = stats.get(type);
        if (count == null) {
            count = 1;
        }
        else {
            count++;
        }
        stats.put(type, count);
        long currentTime = System.currentTimeMillis();
        if (firstLogTime == 0) {
            firstLogTime = currentTime;
            logTime = currentTime;
        }
        if (currentTime - logTime > 1432) {
            System.out.println(new Date(currentTime) + ": Saving " + type + " " + count + " \t("
                    + (1000.0 * (float) count / (float) (currentTime - firstLogTime)) + " " + type + "/second)");
            logTime = currentTime;
        }
    }

    public void describeLoaded() {
        logNodesFound(0);
        for (String type : new String[] { "node", "way", "relation" }) {
            Integer count = stats.get(type);
            if (count != null) {
                System.out.println("Loaded " + count + " " + type + "s");
            }
        }
    }

    public abstract long getDatasetId();

    private int missingNodeCount = 0;

    public void missingNode(long ndRef) {
        if (missingNodeCount++ < 10) {
            osmImporter.error("Cannot find node for osm-id " + ndRef);
        }
    }

    public void describeMissing() {
        if (missingNodeCount > 0) {
            osmImporter.error("When processing the ways, there were " + missingNodeCount + " missing nodes");
        }
        if (missingMemberCount > 0) {
            osmImporter.error("When processing the relations, there were " + missingMemberCount + " missing members");
        }
    }

    private int missingMemberCount = 0;

    private void missingMember(String description) {
        if (missingMemberCount++ < 10) {
            osmImporter.error("Cannot find member: " + description);
        }
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
        debugNodeWithId(currentNode, "node_osm_id", new long[] { 8090260, 273534207 });
    }

    public void addOSMNodeTags(boolean allPoints, LinkedHashMap<String, Object> currentNodeTags) {
        currentNodeTags.remove("created_by"); // redundant information
        // Nodes with tags get added to the index as point geometries
        if (allPoints || currentNodeTags.size() > 0) {
            Map<String, Object> nodeProps = getNodeProperties(currentNode);
            Envelope bbox = new Envelope();
            double[] location = new double[] { (Double) nodeProps.get("lon"), (Double) nodeProps.get("lat") };
            bbox.expandToInclude(location[0], location[1]);
            addNodeGeometry(currentNode, Constants.GTYPE_POINT, bbox, 1);
            poiCount++;
        }
        addNodeTags(currentNode, currentNodeTags, "node");
    }

    protected void debugNodeWithId(T node, String idName, long[] idValues) {
        Map<String, Object> nodeProperties = getNodeProperties(node);
        String node_osm_id = nodeProperties.get(idName).toString();
        for (long idValue : idValues) {
            if (node_osm_id.equals(Long.toString(idValue))) {
                System.out.println("Debug node: " + node_osm_id);
            }
        }
    }

    public void createOSMWay(Map<String, Object> wayProperties, ArrayList<Long> wayNodes,
            LinkedHashMap<String, Object> wayTags) {
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
        createRelationship(way, changesetNode, OSMRelation.CHANGESET);
        if (prev_way == null) {
            createRelationship(osm_dataset, way, OSMRelation.WAYS);
        }
        else {
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
            if (pointNode == null) {
                /*
                 * This can happen if we import not whole planet, so some referenced nodes will be unavailable
                 */
                missingNode(nd_ref);
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
            double[] location = new double[] { (Double) nodeProps.get("lon"), (Double) nodeProps.get("lat") };
            bbox.expandToInclude(location[0], location[1]);
            if (prevNode == null) {
                createRelationship(way, firstNode, OSMRelation.FIRST_NODE);
            }
            else {
                relProps.clear();
                double[] prevLoc = new double[] { (Double) prevProps.get("lon"), (Double) prevProps.get("lat") };

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
                }
                else {
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

    public void createOSMRelation(Map<String, Object> relationProperties,
            ArrayList<Map<String, Object>> relationMembers, LinkedHashMap<String, Object> relationTags) {
        String name = (String) relationTags.get("name");
        if (name != null) {
            // Copy name tag to way because this seems like a valuable location for
            // such a property
            relationProperties.put("name", name);
        }
        T relation = addNode("relation", relationProperties, "relation_osm_id");
        if (prev_relation == null) {
            createRelationship(osm_dataset, relation, OSMRelation.RELATIONS);
        }
        else {
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
                if (null == member || prevMember == member) {
                    /*
                     * This can happen if we import not whole planet, so some referenced nodes will be unavailable
                     */
                    missingMember(memberProps.toString());
                    continue;
                }
                if (member == relation) {
                    osmImporter.error("Cannot add relation to same member: relation[" + relationTags + "] - member["
                            + memberProps + "]");
                    continue;
                }
                Map<String, Object> nodeProps = getNodeProperties(member);
                if (memberType.equals("node")) {
                    double[] location = new double[] { (Double) nodeProps.get("lon"), (Double) nodeProps.get("lat") };
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
                // members can belong to multiple relations, in multiple orders, so NEXT will clash (also with NEXT
                // between ways in original way load)
                // if (prevMember < 0) {
                // batchGraphDb.createRelationship(relation, member, OSMRelation.MEMBERS, null);
                // } else {
                // batchGraphDb.createRelationship(prevMember, member, OSMRelation.NEXT, null);
                // }
                prevMember = member;
            }
            else {
                System.err.println("Cannot process invalid relation member: " + memberProps.toString());
            }
        }
        if (metaGeom.isValid()) {
            addNodeGeometry(relation, metaGeom.getGeometryType(), metaGeom.getBBox(), metaGeom.getVertices());
        }
        this.relationCount++;
    }

    /**
     * This method should be overridden by implementation that are able to perform database or index optimizations when
     * requested, like the batch inserter.
     */
    public void optimize() {
    };

    public abstract T getSingleNode(String name, String string, Object value);

    public abstract Map<String, Object> getNodeProperties(T member);

    public abstract T getOSMNode(long osmId, T changesetNode);

    public abstract void updateGeometryMetaDataFromMember(T member, GeometryMetaData metaGeom,
            Map<String, Object> nodeProps);

    public abstract void finish();

    public abstract T createProxyNode();

    public abstract T getChangesetNode(Map<String, Object> nodeProps);

    public abstract T getUserNode(Map<String, Object> nodeProps);

}
