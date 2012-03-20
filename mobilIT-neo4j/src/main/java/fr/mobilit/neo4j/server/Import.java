package fr.mobilit.neo4j.server;

import java.nio.charset.Charset;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.neo4j.gis.spatial.ConsoleListener;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.graphdb.GraphDatabaseService;

@Path("/import")
public class Import {

    private final GraphDatabaseService db;

    public Import(@Context GraphDatabaseService db) {
        this.db = db;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/osm")
    public Response osm() {
        try {
            Properties properties = new Properties();
            properties.load(Import.class.getResourceAsStream("/import.properties"));
            OSMImporter importer = new OSMImporter("OSM", new ConsoleListener());
            int nb = 0;
            while (properties.getProperty("import.osm.path." + nb) != null) {
                String OSMFilePath = properties.getProperty("import.osm.path." + nb);
                long start = System.currentTimeMillis();
                importer.setCharset(Charset.forName("UTF-8"));
                importer.importFile(db, OSMFilePath, true, 5000);
                // Weird hack to force GC on large loads
                if (System.currentTimeMillis() - start > 300000) {
                    for (int i = 0; i < 3; i++) {
                        System.gc();
                        Thread.sleep(1000);
                    }
                }
                nb++;
            }
            importer.reIndex(db, 1000, true, false);
            return Response.status(Status.OK).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + " :" + e.getCause()).build();
        }
    }
}
