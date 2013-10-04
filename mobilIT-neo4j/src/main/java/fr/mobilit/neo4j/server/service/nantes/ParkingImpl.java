/**
 * This file is part of MobilIT.
 *
 * MobilIT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MobilIT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MobilIT. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @See https://github.com/sim51/mobilIT
 */
package fr.mobilit.neo4j.server.service.nantes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.neo4j.gis.spatial.SpatialDatabaseService;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.service.AbstractParking;
import fr.mobilit.neo4j.server.utils.Cache;
import fr.mobilit.neo4j.server.utils.Constant;
import fr.mobilit.neo4j.server.utils.SpatialUtils;

public class ParkingImpl extends AbstractParking {

    /**
     * Nantes URL service for cycle rent.
     */
    private final static String IMPORT_PARKING_URL = "http://datastore.opendatasoft.com/api/fetch/dataset/equipementsdeplacelementnantes2012?format=json";
    private final static String DETAIL_URL         = "http://data.nantes.fr/api/getDisponibiliteParkingsPublics/1.0/ATMPSTDOTJCNTJ2";
    private final static String CACHE_KEY          = "NTS_PARKING";
    private final static int    CACHE_DURATION     = 300;

    /**
     * Constructor.
     * 
     * @param spatial
     */
    public ParkingImpl(SpatialDatabaseService spatial) {
        super();
        this.spatial = spatial;
    }

    @Override
    public List<POI> importParking() throws MobilITException {
        List<POI> stations = new ArrayList<POI>();
        HttpClient client = new HttpClient();
        GetMethod get = null;
        try {
            // we do the http call and parse the xml response
            get = new GetMethod(IMPORT_PARKING_URL);
            client.executeMethod(get);
            InputStream json = get.getResponseBodyAsStream();
            InputStreamReader reader = new InputStreamReader(json);
            JSONArray list = (JSONArray) JSONValue.parse(reader);
            for (int i = 0; i < list.size(); i++) {
                Map map = (JSONObject) list.get(i);
                Double category = (Double) map.get("CATEGORIE");
                if (category == 1001.0 || category == 1002.0) {
                    String id = String.valueOf(map.get("recordid"));
                    String name = (String) map.get("NOM_COMPLE");
                    JSONArray point = (JSONArray) map.get("geom_x_y");
                    Double lng = (Double) point.get(0);
                    Double lat = (Double) point.get(1);
                    POI poi = new POI(id, name, lng, lat, Constant.NANTES_GEO_CODE);
                    stations.add(poi);
                }
            }
            list.size();
        } catch (Exception e) {
            throw new MobilITException(e.getMessage(), e.getCause());
        } finally {
            get.releaseConnection();
        }
        SpatialUtils spatial = new SpatialUtils(this.spatial);
        spatial.savePOIToLayer(Constant.PARKING_LAYER, stations, Constant.NANTES_GEO_CODE);
        return stations;
    }

    @Override
    public Map<String, Integer> getParking(String id) throws MobilITException {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        HttpClient client = new HttpClient();
        GetMethod get = null;
        try {
            // we llok in cache if there the response !
            String xmlResponse = (String) Cache.getInstance().get(CACHE_KEY);
            if (xmlResponse != null && xmlResponse.isEmpty()) {
                // we do the http call and put it into ehcache & parse the xml response
                get = new GetMethod(DETAIL_URL);
                client.executeMethod(get);
                xmlResponse = get.getResponseBodyAsString();
                Cache.getInstance().add(CACHE_KEY, xmlResponse, CACHE_DURATION);
            }

            InputStream is = new ByteArrayInputStream(xmlResponse.getBytes());
            javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newInstance();
            javax.xml.stream.XMLStreamReader parser = factory.createXMLStreamReader(is);
            ArrayList<String> currentXMLTags = new ArrayList<String>();
            int depth = 0;
            Boolean again = Boolean.TRUE;
            String ident = null;
            Integer free = null;
            Integer total = null;
            while (again) {
                int event = parser.next();
                if (event == javax.xml.stream.XMLStreamConstants.END_DOCUMENT) {
                    break;
                }
                switch (event) {
                    case javax.xml.stream.XMLStreamConstants.START_ELEMENT:
                        currentXMLTags.add(depth, parser.getLocalName());
                        String tagPath = currentXMLTags.toString();
                        if (tagPath.equals("[opendata, answer, data, Groupes_Parking, Groupe_Parking]")) {
                            ident = null;
                            free = null;
                            total = null;

                        }
                        if (tagPath.equals("[opendata, answer, data, Groupes_Parking, Groupe_Parking, Grp_disponible]")) {
                            free = Integer.valueOf(parser.getElementText());
                            currentXMLTags.remove(depth);
                            depth--;
                        }
                        if (tagPath
                                .equals("[opendata, answer, data, Groupes_Parking, Groupe_Parking, Grp_exploitation]")) {
                            total = Integer.valueOf(parser.getElementText());
                            currentXMLTags.remove(depth);
                            depth--;

                        }
                        if (tagPath.equals("[opendata, answer, data, Groupes_Parking, Groupe_Parking, IdObj]")) {
                            ident = parser.getElementText();
                            currentXMLTags.remove(depth);
                            depth--;

                        }
                        depth++;
                        break;
                    case javax.xml.stream.XMLStreamConstants.END_ELEMENT:
                        if (currentXMLTags.toString().equals(
                                "[opendata, answer, data, Groupes_Parking, Groupe_Parking]")) {
                            if (ident.equals(id)) {
                                result.put(Constant.PARKING_FREE, free);
                                result.put(Constant.PARKING_TOTAL, total);
                                again = Boolean.FALSE;
                            }
                        }
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
