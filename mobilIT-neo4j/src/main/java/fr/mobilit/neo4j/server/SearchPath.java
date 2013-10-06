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

import fr.mobilit.neo4j.server.pojo.Itinerary;
import fr.mobilit.neo4j.server.pojo.POI;
import fr.mobilit.neo4j.server.service.CycleRentService;
import fr.mobilit.neo4j.server.shortestpath.ShortestPathAlgorithm;
import fr.mobilit.neo4j.server.shortestpath.costEvaluator.CarCostEvaluation;
import fr.mobilit.neo4j.server.shortestpath.costEvaluator.CycleCostEvaluation;
import fr.mobilit.neo4j.server.shortestpath.costEvaluator.PedestrianCostEvaluation;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

@Path("/search")
public class SearchPath {

    private final GraphDatabaseService db;
    private final SpatialDatabaseService spatial;
    private final Logger logger = Logger.getLogger(SearchPath.class);

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
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/car")
    public Response car(@QueryParam("lat1") Double lat1,
                        @QueryParam("long1") Double long1,
                        @QueryParam("lat2") Double lat2,
                        @QueryParam("long2") Double long2) {
        logger.info("lat1 : " + lat1 + " | long1 : " + long1);
        try {
            CarCostEvaluation eval = new CarCostEvaluation();
            List<Itinerary> path = ShortestPathAlgorithm.search(spatial, lat1, long1, lat2, long2, eval);
            logger.info("Path size is : " + path.size());
            return Response.status(Status.OK).entity(ShortestPathAlgorithm.generateResponse(path)).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + " :" + e.getCause()).build();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/cycle")
    public Response cycle(@QueryParam("lat1") Double lat1,
                          @QueryParam("long1") Double long1,
                          @QueryParam("lat2") Double lat2,
                          @QueryParam("long2") Double long2) {
        try {
            CycleCostEvaluation eval = new CycleCostEvaluation();
            List<Itinerary> path = ShortestPathAlgorithm.search(spatial, lat1, long1, lat2, long2, eval);
            return Response.status(Status.OK).entity(ShortestPathAlgorithm.generateResponse(path)).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + " :" + e.getCause()).build();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/pedestrian")
    public Response pedestrian(@QueryParam("lat1") Double lat1,
                               @QueryParam("long1") Double long1,
                               @QueryParam("lat2") Double lat2,
                               @QueryParam("long2") Double long2) {
        try {
            PedestrianCostEvaluation eval = new PedestrianCostEvaluation();
            List<Itinerary> path = ShortestPathAlgorithm.search(spatial, lat1, long1, lat2, long2, eval);
            return Response.status(Status.OK).entity(ShortestPathAlgorithm.generateResponse(path)).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + " :" + e.getCause()).build();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/cyclerent")
    public Response cycleRent(@QueryParam("lat1") Double lat1,
                              @QueryParam("long1") Double long1,
                              @QueryParam("lat2") Double lat2,
                              @QueryParam("long2") Double long2) {
        try {
            // Final var (path & POI)
            List<Itinerary> path = new ArrayList<Itinerary>();
            List<POI> pois = new ArrayList<POI>();

            // searching cycle station
            CycleRentService service = new CycleRentService(spatial);
            POI cycleStation1 = service.getNearest(long1, lat1, null);
            POI cycleStation2 = service.getNearest(long2, lat2, null);
            pois.add(cycleStation1);
            pois.add(cycleStation2);

            // Cost evalutor
            PedestrianCostEvaluation evalPedestrian = new PedestrianCostEvaluation();
            CycleCostEvaluation evalCycle = new CycleCostEvaluation();

            // pedestrian => cycle station
            List<Itinerary> path1 = ShortestPathAlgorithm.search(spatial, lat1, long1, cycleStation1.getGeoPoint().getLatitude(), cycleStation1.getGeoPoint().getLongitude(), evalPedestrian);
            path.addAll(path1);

            // cycle station 1=> cycle station 2
            List<Itinerary> path2 = ShortestPathAlgorithm.search(spatial, cycleStation1.getGeoPoint().getLatitude(), cycleStation1.getGeoPoint().getLongitude(), cycleStation2.getGeoPoint().getLatitude(), cycleStation2.getGeoPoint().getLongitude(), evalCycle);
            path.addAll(path2);

            // cycle station 2 => ending point
            List<Itinerary> path3 = ShortestPathAlgorithm.search(spatial, cycleStation2.getGeoPoint().getLatitude(), cycleStation2.getGeoPoint().getLongitude(), lat2, long2, evalPedestrian);
            path.addAll(path3);

            return Response.status(Status.OK).entity(ShortestPathAlgorithm.generateResponse(path, pois)).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + " :" + e.getCause()).build();
        }
    }
}
