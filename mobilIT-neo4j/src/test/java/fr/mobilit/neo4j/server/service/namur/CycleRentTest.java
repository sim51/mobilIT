package fr.mobilit.neo4j.server.service.namur;

import com.google.gson.stream.JsonReader;
import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.service.jcdecaux.JCDecauxCycleRentImpl;
import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStreamReader;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcducobu
 * Date: 05/10/13
 * Time: 12:21
 * To change this template use File | Settings | File Templates.
 */
public class CycleRentTest extends Neo4jTestCase {
    private int numberOfStations = 0;

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp(true);
        HttpClient client = new HttpClient();
        GetMethod get = null;
        try {
            // we do the http call and parse the xml response
            JCDecauxCycleRentImpl jcDecauxCycleRent = new JCDecauxCycleRentImpl(this.spatial());
            get = new GetMethod(jcDecauxCycleRent.getImportUrl());
            client.executeMethod(get);
            JsonReader reader = new JsonReader(new InputStreamReader(get.getResponseBodyAsStream(), "UTF-8"));
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                while (reader.hasNext()) {
                    reader.skipValue();
                }
                reader.endObject();
                numberOfStations ++;
            }
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        } finally {
            get.releaseConnection();
        }
    }

    @Test
    public void testImport() throws MobilITException {
        JCDecauxCycleRentImpl jcdecaux = new JCDecauxCycleRentImpl(this.spatial());
        List<POI> bicloo = jcdecaux.importStation();
        assertEquals(numberOfStations, bicloo.size());
    }
}
