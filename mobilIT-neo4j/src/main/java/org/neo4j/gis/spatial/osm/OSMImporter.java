package org.neo4j.gis.spatial.osm;

import org.geotools.referencing.datum.DefaultEllipsoid;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.osm.utils.CountedFileReader;
import org.neo4j.gis.spatial.osm.writer.OSMWriter;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.server.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class that import an OSM File into an OSM Neo4j Spatial layer.
 */
public class OSMImporter {

    /**
     * Index name of neo4j for OSM.
     */
    public static String INDEX_NAME_CHANGESET = "changeset";
    public static String INDEX_NAME_USER = "user";
    public static String INDEX_NAME_NODE = "node";
    public static String INDEX_NAME_WAY = "way";

    /**
     * Default porjection of OSM.
     */
    public static DefaultEllipsoid WGS84 = DefaultEllipsoid.WGS84;

    /**
     * The logger.
     */
    private final static Logger LOG = Logger.getLogger(OSMImporter.class);

    /**
     * Date formater for OSM date ("2008-06-11T12:36:28Z").
     */
    private final static DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * OSM Import varaibles.
     */
    private String layerName;
    private long osm_dataset = -1;
    private Charset charset = Charset.defaultCharset();

    /**
     * Constructor.
     *
     * @param layerName
     */
    public OSMImporter(String layerName) {
        this.layerName = layerName;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Import an OSM file.
     *
     * @param database
     * @param dataset the path to the OSM file
     * @param allPoints
     * @param txInterval
     * @throws IOException
     * @throws XMLStreamException
     */
    public void importFile(GraphDatabaseService database, String dataset, boolean allPoints, int txInterval) throws IOException, XMLStreamException {
        importFile(OSMWriter.fromGraphDatabase(database, this, txInterval), dataset, allPoints, charset);
    }

    public void importFile(OSMWriter<?> osmWriter, String dataset, boolean allPoints, Charset charset) throws IOException, XMLStreamException {
        System.out.println("Importing with osm-writer: " + osmWriter);

        osmWriter.getOrCreateOSMDataset(layerName);
        osm_dataset = osmWriter.getDatasetId();

        long startTime = System.currentTimeMillis();
        long[] times = new long[]{0L, 0L, 0L, 0L};

        javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newInstance();
        CountedFileReader reader = new CountedFileReader(dataset, charset);
        javax.xml.stream.XMLStreamReader parser = factory.createXMLStreamReader(reader);

        int countXMLTags = 0;
        boolean startedWays = false;
        boolean startedRelations = false;
        try {
            int depth = 0;
            ArrayList<String> currentXMLTags = new ArrayList<String>();
            Map<String, Object> wayProperties = null;
            ArrayList<Long> wayNodes = new ArrayList<Long>();
            Map<String, Object> relationProperties = null;
            ArrayList<Map<String, Object>> relationMembers = new ArrayList<Map<String, Object>>();
            LinkedHashMap<String, Object> currentNodeTags = new LinkedHashMap<String, Object>();

            while (true) {
                int event = parser.next();
                if (event == javax.xml.stream.XMLStreamConstants.END_DOCUMENT) {
                    break;
                }
                switch (event) {

                    // we found a starting XML element
                    case javax.xml.stream.XMLStreamConstants.START_ELEMENT:
                        currentXMLTags.add(depth, parser.getLocalName());
                        String tagPath = currentXMLTags.toString();
                        if (tagPath.equals("[osm]")) {
                            osmWriter.setDatasetProperties(extractProperties(parser));
                        }
                        else if (tagPath.equals("[osm, bounds]")) {
                            osmWriter.addOSMBBox(extractProperties("bbox", parser));
                        }
                        else if (tagPath.equals("[osm, node]")) {
                            // <node id="269682538" lat="56.0420950" lon="12.9693483" user="sanna" uid="31450"
                            // visible="true" version="1" changeset="133823" timestamp="2008-06-11T12:36:28Z"/>
                            osmWriter.createOSMNode(extractProperties("node", parser));
                        }
                        else if (tagPath.equals("[osm, way]")) {
                            // <way id="27359054" user="spull" uid="61533" visible="true" version="8"
                            // changeset="4707351" timestamp="2010-05-15T15:39:57Z">
                            if (!startedWays) {
                                startedWays = true;
                                times[0] = System.currentTimeMillis();
                                times[1] = System.currentTimeMillis();
                            }
                            wayProperties = extractProperties("way", parser);
                            wayNodes.clear();
                        }
                        else if (tagPath.equals("[osm, way, nd]")) {
                            Map<String, Object> properties = extractProperties(parser);
                            wayNodes.add(Long.parseLong(properties.get("ref").toString()));
                        }
                        else if (tagPath.endsWith("tag]")) {
                            Map<String, Object> properties = extractProperties(parser);
                            currentNodeTags.put(properties.get("k").toString(), properties.get("v").toString());
                        }
                        else if (tagPath.equals("[osm, relation]")) {
                            // <relation id="77965" user="Grillo" uid="13957" visible="true" version="24"
                            // changeset="5465617" timestamp="2010-08-11T19:25:46Z">
                            if (!startedRelations) {
                                startedRelations = true;
                                times[2] = System.currentTimeMillis();
                                times[3] = System.currentTimeMillis();
                            }
                            relationProperties = extractProperties("relation", parser);
                            relationMembers.clear();
                        }
                        else if (tagPath.equals("[osm, relation, member]")) {
                            relationMembers.add(extractProperties(parser));
                        }
                        if (startedRelations) {
                            if (countXMLTags < 10) {
                                LOG.info("Starting tag at depth " + depth + ": " + currentXMLTags.get(depth) + " - "
                                        + currentXMLTags.toString());
                                for (int i = 0; i < parser.getAttributeCount(); i++) {
                                    LOG.info("\t" + currentXMLTags.toString() + ": " + parser.getAttributeLocalName(i) + "["
                                            + parser.getAttributeNamespace(i) + "," + parser.getAttributePrefix(i)
                                            + "," + parser.getAttributeType(i) + "," + "] = "
                                            + parser.getAttributeValue(i));
                                }
                            }
                            countXMLTags++;
                        }
                        depth++;
                        break;

                    // we found an ending XML element
                    case javax.xml.stream.XMLStreamConstants.END_ELEMENT:

                        // it's an OSM node
                        if (currentXMLTags.toString().equals("[osm, node]")) {
                            osmWriter.addOSMNodeTags(allPoints, currentNodeTags);
                        }

                        // it's an OSM way
                        else if (currentXMLTags.toString().equals("[osm, way]")) {
                            osmWriter.createOSMWay(wayProperties, wayNodes, currentNodeTags);
                        }

                        // it's an OSM relation
                        else if (currentXMLTags.toString().equals("[osm, relation]")) {
                            osmWriter.createOSMRelation(relationProperties, relationMembers, currentNodeTags);
                        }

                        // update status of the XML parsing
                        depth--;
                        currentXMLTags.remove(depth);
                        break;

                    // otherwise...
                    default:
                        break;
                }
            }
        } finally {
            parser.close();
            osmWriter.finish();
            this.osm_dataset = osmWriter.getDatasetId();
        }
        describeTimes(startTime, times);

        long stopTime = System.currentTimeMillis();
        LOG.info("Elapsed time in seconds: " + (1.0 * (stopTime - startTime) / 1000.0));
    }

    /**
     * Return all attributs of an XML elements as a Map.
     *
     * @param name : the name of the node. It will be adding as a property and can be null.
     * @param parser
     * @return
     */
    private Map<String, Object> extractProperties(String name, XMLStreamReader parser) {
        // <node id="269682538" lat="56.0420950" lon="12.9693483" user="sanna" uid="31450" visible="true" version="1"
        // changeset="133823" timestamp="2008-06-11T12:36:28Z"/>
        // <way id="27359054" user="spull" uid="61533" visible="true" version="8" changeset="4707351"
        // timestamp="2010-05-15T15:39:57Z">
        // <relation id="77965" user="Grillo" uid="13957" visible="true" version="24" changeset="5465617"
        // timestamp="2010-08-11T19:25:46Z">
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String prop = parser.getAttributeLocalName(i);
            String value = parser.getAttributeValue(i);
            if (name != null && prop.equals("id")) {
                prop = name + "_osm_id";
                name = null;
            }
            if (prop.equals("lat") || prop.equals("lon")) {
                properties.put(prop, Double.parseDouble(value));
            } else if (name != null && prop.equals("version")) {
                properties.put(prop, Integer.parseInt(value));
            } else if (prop.equals("visible")) {
                if (!value.equals("true") && !value.equals("1")) {
                    properties.put(prop, false);
                }
            } else if (prop.equals("timestamp")) {
                try {
                    Date timestamp = timestampFormat.parse(value);
                    properties.put(prop, timestamp.getTime());
                } catch (ParseException e) {
                    LOG.error("Error parsing timestamp", e);
                }
            } else {
                properties.put(prop, value);
            }
        }
        if (name != null) {
            properties.put("name", name);
        }
        return properties;
    }

    private Map<String, Object> extractProperties(XMLStreamReader parser) {
        return extractProperties(null, parser);
    }

    /**
     * Display some usefull stats at the end of the import.
     *
     * @param startTime
     * @param times
     */
    private void describeTimes(long startTime, long[] times) {
        long endTime = System.currentTimeMillis();
        LOG.info("Completed load in " + (1.0 * (endTime - startTime) / 1000.0) + "s");
        LOG.info("\tImported nodes:  " + (1.0 * (times[0] - startTime) / 1000.0) + "s");
        LOG.info("\tOptimized index: " + (1.0 * (times[1] - times[0]) / 1000.0) + "s");
        LOG.info("\tImported ways:   " + (1.0 * (times[2] - times[1]) / 1000.0) + "s");
        LOG.info("\tOptimized index: " + (1.0 * (times[3] - times[2]) / 1000.0) + "s");
        LOG.info("\tImported rels:   " + (1.0 * (endTime - times[3]) / 1000.0) + "s");
    }

    /**
     * Detects if road has the only direction
     *
     * @param wayProperties
     * @return RoadDirection
     */
    public static RoadDirection isOneway(Map<String, Object> wayProperties) {
        String oneway = (String) wayProperties.get("oneway");
        if (null != oneway) {
            if ("-1".equals(oneway))
                return RoadDirection.BACKWARD;
            if ("1".equals(oneway) || "yes".equalsIgnoreCase(oneway) || "true".equalsIgnoreCase(oneway))
                return RoadDirection.FORWARD;
        }
        return RoadDirection.BOTH;
    }

    /**
     * Calculate correct distance between 2 points on Earth.
     *
     * @param latA
     * @param lonA
     * @param latB
     * @param lonB
     * @return distance in meters
     */
    public static double distance(double lonA, double latA, double lonB, double latB) {
        return WGS84.orthodromicDistance(lonA, latA, lonB, latB);
    }
}
