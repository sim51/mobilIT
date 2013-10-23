package org.neo4j.gis.spatial.osm.writer;

import org.neo4j.collections.rtree.Envelope;
import org.neo4j.gis.spatial.Constants;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.gis.spatial.osm.OSMRelation;
import org.neo4j.gis.spatial.osm.utils.GeometryMetaData;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class OSMGraphWriter extends OSMWriter<Node> {

    private GraphDatabaseService graphDb;
    private Node osm_root;
    private long currentChangesetId = -1;
    private Node currentChangesetNode;
    private long currentUserId = -1;
    private Node currentUserNode;
    private Node usersNode;
    private HashMap<Long, Node> changesetNodes = new HashMap<Long, Node>();
    private Transaction tx;
    private int checkCount = 0;
    private int txInterval;

    public OSMGraphWriter(GraphDatabaseService graphDb, OSMImporter osmImporter, int txInterval) {
        super(osmImporter);
        this.graphDb = graphDb;
        this.txInterval = txInterval;
        if (this.txInterval < 100) {
            System.err.println("Warning: Unusually short txInterval, expect bad insert performance");
        }
        checkTx(); // Opens transaction for future writes
    }

    private void successTx() {
        if (tx != null) {
            tx.success();
            tx.finish();
            tx = null;
            checkCount = 0;
        }
    }

    private void checkTx() {
        if (checkCount++ > txInterval || tx == null) {
            successTx();
            tx = graphDb.beginTx();
        }
    }

    private Index<Node> indexFor(String indexName) {
        // return graphDb.index().forNodes( indexName, MapUtil.stringMap("type", "exact") );
        return graphDb.index().forNodes(indexName);
    }

    private Node findNode(String name, Node parent, RelationshipType relType) {
        for (Relationship relationship : parent.getRelationships(relType, Direction.OUTGOING)) {
            Node node = relationship.getEndNode();
            if (name.equals(node.getProperty("name"))) {
                return node;
            }
        }
        return null;
    }

    @Override
    public Node getOrCreateNode(String name, String type, Node parent, RelationshipType relType) {
        Node node = findNode(name, parent, relType);
        if (node == null) {
            node = graphDb.createNode();
            node.setProperty("name", name);
            node.setProperty("type", type);
            parent.createRelationshipTo(node, relType);
            checkTx();
        }
        return node;
    }

    @Override
    public Node getOrCreateOSMDataset(String name) {
        if (osm_dataset == null) {
            osm_root = getOrCreateNode("osm_root", "osm", graphDb.getReferenceNode(), OSMRelation.OSM);
            osm_dataset = getOrCreateNode(name, "osm", osm_root, OSMRelation.OSM);
        }
        return osm_dataset;
    }

    @Override
    public void setDatasetProperties(Map<String, Object> extractProperties) {
        for (String key : extractProperties.keySet()) {
            osm_dataset.setProperty(key, extractProperties.get(key));
        }
    }

    private void addProperties(PropertyContainer node, Map<String, Object> properties) {
        for (String property : properties.keySet()) {
            node.setProperty(property, properties.get(property));
        }
    }

    @Override
    public void addNodeTags(Node node, LinkedHashMap<String, Object> tags, String type) {
        if (node != null && tags.size() > 0) {
            Node tagsNode = graphDb.createNode();
            addProperties(tagsNode, tags);
            node.createRelationshipTo(tagsNode, OSMRelation.TAGS);
            tags.clear();
        }
    }

    @Override
    public void addNodeGeometry(Node node, int gtype, Envelope bbox, int vertices) {
        if (node != null && bbox.isValid() && vertices > 0) {
            if (gtype == Constants.GTYPE_GEOMETRY)
                gtype = vertices > 1 ? Constants.GTYPE_MULTIPOINT : Constants.GTYPE_POINT;
            Node geomNode = graphDb.createNode();
            geomNode.setProperty("gtype", gtype);
            geomNode.setProperty("vertices", vertices);
            geomNode.setProperty("bbox", new double[]{bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY()});
            node.createRelationshipTo(geomNode, OSMRelation.GEOM);
        }
    }

    @Override
    public Node addNode(String name, Map<String, Object> properties, String indexKey) {
        Node node = graphDb.createNode();
        if (indexKey != null && properties.containsKey(indexKey)) {
            indexFor(name).add(node, indexKey, properties.get(indexKey));
            properties.put(indexKey, Long.parseLong(properties.get(indexKey).toString()));
        }
        addProperties(node, properties);
        checkTx();
        return node;
    }

    @Override
    public Node index(String indexName, Node node, Set<String> indexKeys) {
        for (String indexKey : indexKeys) {
            Object property = node.getProperty(indexKey, null);
            if (indexKey != null && property != null) {
                indexFor(indexName).add(node, indexKey, property);
            }
        }
        checkTx();
        return node;
    }

    @Override
    public void createRelationship(Node from, Node to, RelationshipType relType, LinkedHashMap<String, Object> relProps) {
        Relationship rel = from.createRelationshipTo(to, relType);
        if (relProps != null && relProps.size() > 0) {
            addProperties(rel, relProps);
        }
    }

    @Override
    public long getDatasetId() {
        return osm_dataset.getId();
    }

    @Override
    public Node getSingleNode(String name, String string, Object value) {
        return indexFor(name).get(string, value).getSingle();
    }

    @Override
    public Map<String, Object> getNodeProperties(Node node) {
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        for (String property : node.getPropertyKeys()) {
            properties.put(property, node.getProperty(property));
        }
        return properties;
    }

    @Override
    public Node getOSMNode(long osmId, Node changesetNode) {
        if (currentChangesetNode != changesetNode || changesetNodes.isEmpty()) {
            currentChangesetNode = changesetNode;
            changesetNodes.clear();
            for (Relationship rel : changesetNode.getRelationships(OSMRelation.CHANGESET, Direction.INCOMING)) {
                Node node = rel.getStartNode();
                Long nodeOsmId = (Long) node.getProperty("node_osm_id", null);
                if (nodeOsmId != null) {
                    changesetNodes.put(nodeOsmId, node);
                }
            }
        }
        Node node = changesetNodes.get(osmId);
        if (node == null) {
            LOG.warn("node-index not found");
            return indexFor("node").get("node_osm_id", osmId).getSingle();
        } else {
            LOG.warn("Changeset not found");
            return node;
        }
    }

    @Override
    public void updateGeometryMetaDataFromMember(Node member, GeometryMetaData metaGeom, Map<String, Object> nodeProps) {
        for (Relationship rel : member.getRelationships(OSMRelation.GEOM)) {
            nodeProps = getNodeProperties(rel.getEndNode());
            metaGeom.checkSupportedGeometry((Integer) nodeProps.get("gtype"));
            metaGeom.expandToIncludeBBox(nodeProps);
        }
    }

    @Override
    public void finish() {
        osm_dataset.setProperty("relationCount", (Integer) osm_dataset.getProperty("relationCount", 0) + relationCount);
        osm_dataset.setProperty("wayCount", (Integer) osm_dataset.getProperty("wayCount", 0) + wayCount);
        osm_dataset.setProperty("nodeCount", (Integer) osm_dataset.getProperty("nodeCount", 0) + nodeCount);
        osm_dataset.setProperty("poiCount", (Integer) osm_dataset.getProperty("poiCount", 0) + poiCount);
        osm_dataset.setProperty("changesetCount", (Integer) osm_dataset.getProperty("changesetCount", 0)
                + changesetCount);
        osm_dataset.setProperty("userCount", (Integer) osm_dataset.getProperty("userCount", 0) + userCount);
        successTx();
    }

    @Override
    public Node createProxyNode() {
        return graphDb.createNode();
    }

    @Override
    public Node getChangesetNode(Map<String, Object> nodeProps) {
        long changeset = Long.parseLong(nodeProps.remove(osmImporter.INDEX_NAME_CHANGESET).toString());
        getUserNode(nodeProps);
        if (changeset != currentChangesetId) {
            currentChangesetId = changeset;
            IndexHits<Node> result = indexFor(osmImporter.INDEX_NAME_CHANGESET).get(osmImporter.INDEX_NAME_CHANGESET,
                    currentChangesetId);
            if (result.size() > 0) {
                currentChangesetNode = result.getSingle();
            } else {
                LinkedHashMap<String, Object> changesetProps = new LinkedHashMap<String, Object>();
                changesetProps.put(osmImporter.INDEX_NAME_CHANGESET, currentChangesetId);
                changesetProps.put("timestamp", nodeProps.get("timestamp"));
                currentChangesetNode = (Node) addNode(osmImporter.INDEX_NAME_CHANGESET, changesetProps,
                        osmImporter.INDEX_NAME_CHANGESET);
                changesetCount++;
                if (currentUserNode != null) {
                    createRelationship(currentChangesetNode, currentUserNode, OSMRelation.USER);
                }
            }
            result.close();
        }
        return currentChangesetNode;
    }

    @Override
    public Node getUserNode(Map<String, Object> nodeProps) {
        try {
            long uid = Long.parseLong(nodeProps.remove("uid").toString());
            String name = nodeProps.remove(osmImporter.INDEX_NAME_USER).toString();
            if (uid != currentUserId) {
                currentUserId = uid;
                IndexHits<Node> result = indexFor(osmImporter.INDEX_NAME_USER).get("uid", currentUserId);
                if (result.size() > 0) {
                    currentUserNode = indexFor(osmImporter.INDEX_NAME_USER).get("uid", currentUserId).getSingle();
                } else {
                    LinkedHashMap<String, Object> userProps = new LinkedHashMap<String, Object>();
                    userProps.put("uid", currentUserId);
                    userProps.put("name", name);
                    userProps.put("timestamp", nodeProps.get("timestamp"));
                    currentUserNode = (Node) addNode(osmImporter.INDEX_NAME_USER, userProps, "uid");
                    userCount++;
                    // if (currentChangesetNode != null) {
                    // currentChangesetNode.createRelationshipTo(currentUserNode, OSMRelation.USER);
                    // }
                    if (usersNode == null) {
                        usersNode = graphDb.createNode();
                        osm_dataset.createRelationshipTo(usersNode, OSMRelation.USERS);
                    }
                    usersNode.createRelationshipTo(currentUserNode, OSMRelation.OSM_USER);
                }
                result.close();
            }
        } catch (Exception e) {
            currentUserId = -1;
            currentUserNode = null;
            LOG.warn("User not found " + nodeProps.toString());
        }
        return currentUserNode;
    }

}
