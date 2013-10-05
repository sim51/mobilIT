package fr.mobilit.neo4j.server.service.jcdecaux;

import com.google.gson.stream.JsonReader;
import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.service.AbstractCycleRent;
import fr.mobilit.neo4j.server.utils.Constant;
import fr.mobilit.neo4j.server.utils.SpatialUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.neo4j.gis.spatial.SpatialDatabaseService;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: marcducobu
 * Date: 05/10/13
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */
public class JCDecauxCycleRentImpl extends AbstractCycleRent {

    /**
     * JCDecaux pamameters for cycle rent api.
     */
    public String jcdecauxApiKey = "";
    public String jcdecauxContractName = "";

    /**
     * Constructor.
     *
     * @param spatial
     */
    public JCDecauxCycleRentImpl(SpatialDatabaseService spatial) {
        super();
        this.spatial = spatial;
    }

    /**
     * Set the JCDecaux API key used to access to real-time information.
     *
     * @param anApiKey
     */
    public void setJcdecauxApiKey (String anApiKey) {
        jcdecauxApiKey = anApiKey;
    }

    /**
     * Set the JCDecaux API contract name used to identify the town.
     *
     * @param aContractName
     */
    public void setJcdecauxContractName (String aContractName) {
        jcdecauxContractName = aContractName;
    }

    /**
     * Retuns the url of the json document where all the station are listed.
     *
     * @return The url of the json document where all the station are listed.
     */
    public String getImportUrl () {
        return "https://api.jcdecaux.com/vls/v1/stations?contract=" + jcdecauxContractName
                + "&apiKey=" + jcdecauxApiKey;
    }

    /**
     * Retuns the url of the json document where the details of the station aStationId are listed.
     *
     * @param aStationId the station that we want the details
     * @return the url of the json document where the details of the station aStationId is listed.
     */
    public String getDetailStationUrl (String aStationId) {
        return "https://api.jcdecaux.com/vls/v1/stations/" + aStationId
                + "?contract=" + jcdecauxContractName
                + "&apiKey=" + jcdecauxApiKey;
    }

    @Override
    public List<POI> importStation() throws MobilITException  {
        List<POI> stations = new ArrayList<POI>();
        HttpClient client = new HttpClient();
        GetMethod get = null;
        try {
            get = new GetMethod(getImportUrl());
            client.executeMethod(get);
            JsonReader reader = new JsonReader(new InputStreamReader(get.getResponseBodyAsStream(), "UTF-8"));
            reader.beginArray();
            while (reader.hasNext()) {
                String id = null, name_poi = null;
                Double latitude = -1., longitude = -1.;
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("number")) {
                        id = Integer.toString(reader.nextInt());
                    }
                    else if (name.equals("name")) {
                        name_poi = reader.nextString();
                    }
                    else if (name.equals("position")) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String position_name = reader.nextName();
                            if (position_name.equals("lat")) {
                                latitude = reader.nextDouble();
                            }
                            else if (position_name.equals("lng")) {
                                longitude = reader.nextDouble();
                            }
                            else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    }
                    else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
                if(id != null && name_poi != null && latitude != -1. && longitude != -1.) {
                    POI station = new POI(id, name_poi, longitude, latitude, Constant.NAMUR_GEO_CODE);
                    stations.add(station);
                }
            }
            reader.endArray();
            reader.close();
        }
        catch (Exception e) {
            throw new MobilITException(e.getMessage(), e.getCause());
        } finally {
            get.releaseConnection();
        }
        SpatialUtils spatial = new SpatialUtils(this.spatial);
        spatial.savePOIToLayer(Constant.CYCLE_LAYER, stations, Constant.NAMUR_GEO_CODE);
        return stations;
    }

    @Override
    public Map<String, Integer> getStation(String id) throws MobilITException {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        HttpClient client = new HttpClient();
        GetMethod get = null;
        try {
            get = new GetMethod(getDetailStationUrl(id));
            client.executeMethod(get);
            JsonReader reader = new JsonReader(new InputStreamReader(get.getResponseBodyAsStream(), "UTF-8"));
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("bike_stands")) {
                    result.put(Constant.CYCLE_TOTAL, reader.nextInt());
                }
                else if (name.equals("available_bike_stands")) {
                    result.put(Constant.CYCLE_FREE, reader.nextInt());
                }
                else if (name.equals("available_bikes")) {
                    result.put(Constant.CYCLE_AVAIBLE, reader.nextInt());
                }
                else {
                    reader.skipValue();
                }
            }
            reader.endObject();

        } catch (Exception e) {
            throw new MobilITException(e.getMessage(), e.getCause());
        } finally {
            get.releaseConnection();
        }
        return result;
    }
}