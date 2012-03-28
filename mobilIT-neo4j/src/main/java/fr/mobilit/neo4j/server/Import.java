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

import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.graphdb.GraphDatabaseService;

import fr.mobilit.neo4j.server.utils.Constant;

/**
 * Neo4j REST interface that manage imports.
 * 
 * @author bsimard
 * 
 */
@Path("/import")
public class Import {

    /**
     * Graph database.
     */
    private final GraphDatabaseService db;

    /**
     * Constructor.
     * 
     * @param db
     */
    public Import(@Context GraphDatabaseService db) {
        this.db = db;
    }

    /**
     * Action to import OSM file into the neo4j spatial database.
     * 
     * @param files list of osm file on the FS separate by '@' character.
     * @return OK or the error.
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/osm")
    public Response osm(@FormParam("files") String files) {
        String[] osmFiles = files.split("@");
        try {
            OSMImporter importer = new OSMImporter(Constant.LAYER_OSM);
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
            importer.reIndex(db, 1000, true, true);
            return Response.status(Status.OK).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + " :" + e.getCause()).build();
        }
    }
}
