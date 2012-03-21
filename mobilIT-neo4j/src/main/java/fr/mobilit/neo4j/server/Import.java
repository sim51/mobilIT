package fr.mobilit.neo4j.server;

import java.io.File;
import java.nio.charset.Charset;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
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

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/osm")
    public Response osm(@FormParam("osmFiles") String[] osmFiles) {
        try {
            OSMImporter importer = new OSMImporter("OSM", new ConsoleListener());
            for (int i = 0; i < osmFiles.length; i++) {
                String OSMFilePath = osmFiles[i];
                File osmFile = new File(OSMFilePath);
                if (!osmFile.exists()) {
                    throw new Exception("OSM file " + OSMFilePath + " doesn't found");
                }
                long start = System.currentTimeMillis();
                importer.setCharset(Charset.forName("UTF-8"));
                importer.importFile(db, OSMFilePath, true, 5000);
                // Weird hack to force GC on large loads
                if (System.currentTimeMillis() - start > 300000) {
                    for (int j = 0; j < 3; j++) {
                        System.gc();
                        Thread.sleep(1000);
                    }
                }
            }
            importer.reIndex(db, 1000, true, false);
            return Response.status(Status.OK).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + " :" + e.getCause()).build();
        }
    }
}
