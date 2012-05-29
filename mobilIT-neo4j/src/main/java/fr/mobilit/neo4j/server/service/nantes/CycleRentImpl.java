package fr.mobilit.neo4j.server.service.nantes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.EditableLayerImpl;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.encoders.SimplePointEncoder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.service.CycleRent;
import fr.mobilit.neo4j.server.utils.Constant;

public class CycleRentImpl extends CycleRent {

    /**
     * Nantes URL service for cycle rent.
     */
    private final static String IMPORT_URL = "http://www.bicloo.nantesmetropole.fr/service/carto";
    private final static String DETAIL_URL = "http://www.bicloo.nantesmetropole.fr/service/stationdetails/nantes/";

    /**
     * Constructor.
     * 
     * @param spatial
     */
    public CycleRentImpl(SpatialDatabaseService spatial) {
        super();
        this.spatial = spatial;
    }

    @Override
    public List<POI> importStation() throws MobilITException {
        List<POI> stations = new ArrayList<POI>();
        HttpClient client = new HttpClient();
        GetMethod get = null;
        try {
            // we do the http call and parse the xml response
            get = new GetMethod(IMPORT_URL);
            client.executeMethod(get);
            javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newInstance();
            javax.xml.stream.XMLStreamReader parser = factory.createXMLStreamReader(get.getResponseBodyAsStream());
            ArrayList<String> currentXMLTags = new ArrayList<String>();
            int depth = 0;
            while (true) {
                int event = parser.next();
                if (event == javax.xml.stream.XMLStreamConstants.END_DOCUMENT) {
                    break;
                }
                switch (event) {
                    case javax.xml.stream.XMLStreamConstants.START_ELEMENT:
                        currentXMLTags.add(depth, parser.getLocalName());
                        String tagPath = currentXMLTags.toString();
                        // here we have a match, so we construct the POI
                        if (tagPath.equals("[carto, markers, marker]")) {
                            String id = parser.getAttributeValue("", "number");
                            String name = parser.getAttributeValue("", "address");
                            Double longitude = Double.valueOf(parser.getAttributeValue("", "lng"));
                            Double latitude = Double.valueOf(parser.getAttributeValue("", "lat"));
                            POI station = new POI(id, name, longitude, latitude);
                            stations.add(station);
                        }
                        depth++;
                        break;
                    case javax.xml.stream.XMLStreamConstants.END_ELEMENT:
                        depth--;
                        currentXMLTags.remove(depth);
                        break;
                    default:
                        break;
                }

            }
        } catch (Exception e) {
            throw new MobilITException(e.getMessage(), e.getCause());
        } finally {
            get.releaseConnection();
        }

        // create layer
        EditableLayer cycleLayer;
        if (!this.spatial.containsLayer(Constant.LAYER_CYCLE)) {
            cycleLayer = (EditableLayer) this.spatial.createLayer(Constant.LAYER_CYCLE, SimplePointEncoder.class,
                    EditableLayerImpl.class, "lon:lat");
        }
        else {
            cycleLayer = (EditableLayer) this.spatial.getLayer(Constant.LAYER_CYCLE);
        }
        Transaction tx = this.spatial.getDatabase().beginTx();
        for (int i = 0; i < stations.size(); i++) {
            // create data node
            POI currentStation = stations.get(i);
            Node currentNode = this.spatial.getDatabase().createNode();
            currentNode.setProperty("name", currentStation.getName());
            currentNode.setProperty("lat", currentStation.getLatitude());
            currentNode.setProperty("lon", currentStation.getLongitude());
            currentNode.setProperty("geoid", Constant.NANTES_GEO_CODE);
            currentNode.setProperty("id", currentStation.getId());

            // save geom into layer
            cycleLayer.add(currentNode);
        }
        tx.success();
        tx.finish();
        return stations;
    }

    @Override
    public Map<String, Integer> getStation(String id) throws MobilITException {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        HttpClient client = new HttpClient();
        GetMethod get = null;
        try {
            // we do the http call and parse the xml response
            get = new GetMethod(DETAIL_URL + id);
            client.executeMethod(get);
            javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newInstance();
            javax.xml.stream.XMLStreamReader parser = factory.createXMLStreamReader(get.getResponseBodyAsStream());
            ArrayList<String> currentXMLTags = new ArrayList<String>();
            int depth = 0;
            while (true) {
                int event = parser.next();
                if (event == javax.xml.stream.XMLStreamConstants.END_DOCUMENT) {
                    break;
                }
                switch (event) {
                    case javax.xml.stream.XMLStreamConstants.START_ELEMENT:
                        currentXMLTags.add(depth, parser.getLocalName());
                        String tagPath = currentXMLTags.toString();
                        if (tagPath.equals("[station, available]")) {
                            String num = parser.getElementText();
                            result.put(Constant.CYCLE_AVAIBLE, Integer.valueOf(num));
                            currentXMLTags.remove(depth);
                            depth--;
                        }
                        if (tagPath.equals("[station, free]")) {
                            String num = parser.getElementText();
                            result.put(Constant.CYCLE_FREE, Integer.valueOf(num));
                            currentXMLTags.remove(depth);
                            depth--;
                        }
                        if (tagPath.equals("[station, total]")) {
                            String num = parser.getElementText();
                            result.put(Constant.CYCLE_TOTAL, Integer.valueOf(num));
                            currentXMLTags.remove(depth);
                            depth--;
                        }
                        depth++;
                        break;
                    case javax.xml.stream.XMLStreamConstants.END_ELEMENT:
                        depth--;
                        currentXMLTags.remove(depth);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            throw new MobilITException(e.getMessage(), e.getCause());
        } finally {
            get.releaseConnection();
        }
        return result;
    }

}
