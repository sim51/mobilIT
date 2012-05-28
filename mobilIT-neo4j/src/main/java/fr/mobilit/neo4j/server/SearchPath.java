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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.GraphDatabaseService;

import fr.mobilit.neo4j.server.shortestpath.ShortestPathAlgorithm;
import fr.mobilit.neo4j.server.shortestpath.costEvaluator.CarCostEvaluation;
import fr.mobilit.neo4j.server.shortestpath.costEvaluator.CycleCostEvaluation;
import fr.mobilit.neo4j.server.shortestpath.costEvaluator.PedestrianCostEvaluation;

@Path("/search")
public class SearchPath {

    private final GraphDatabaseService   db;
    private final SpatialDatabaseService spatial;

    /**
     * Constructor.
     * 
     * @param db
     */
    public SearchPath(@Context GraphDatabaseService db) {
        this.db = db;
        this.spatial = new SpatialDatabaseService(db);
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/car")
    public Response car(Double lat1, Double long1, Double lat2, Double long2) {
        try {
            CarCostEvaluation eval = new CarCostEvaluation();
            String result = ShortestPathAlgorithm.search(spatial, lat1, long1, lat2, long2, eval);
            return Response.status(Status.OK).entity(result).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + " :" + e.getCause()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/searchpath/cycle")
    public Response cycle(Double lat1, Double long1, Double lat2, Double long2) {
        try {
            CycleCostEvaluation eval = new CycleCostEvaluation();
            String result = ShortestPathAlgorithm.search(spatial, lat1, long1, lat2, long2, eval);
            return Response.status(Status.OK).entity(result).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + " :" + e.getCause()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/searchpath/pedestrian")
    public Response biclou(Double lat1, Double long1, Double lat2, Double long2) {
        try {
            PedestrianCostEvaluation eval = new PedestrianCostEvaluation();
            String result = ShortestPathAlgorithm.search(spatial, lat1, long1, lat2, long2, eval);
            return Response.status(Status.OK).entity(result).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + " :" + e.getCause()).build();
        }
    }

}
