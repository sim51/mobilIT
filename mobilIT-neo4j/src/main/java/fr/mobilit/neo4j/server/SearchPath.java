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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.osm.OSMGeometryEncoder;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.osm.OSMGeoPipeline;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphalgo.impl.path.Dijkstra;
import org.neo4j.graphdb.Expander;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import com.vividsolutions.jts.geom.Coordinate;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.shortestpath.costEvaluator.CarCostEvaluation;
import fr.mobilit.neo4j.server.utils.Constant;

@Path("/search")
public class SearchPath {

    private final GraphDatabaseService   db;
    private final SpatialDatabaseService spatial;
    private static Expander              expander;
    private OSMLayer                     osm;

    /**
     * Constructor.
     * 
     * @param db
     */
    public SearchPath(@Context GraphDatabaseService db) {
        this.db = db;
        this.spatial = new SpatialDatabaseService(db);
        this.osm = (OSMLayer) spatial.getOrCreateLayer(Constant.LAYER_OSM, OSMGeometryEncoder.class, OSMLayer.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/car")
    public Response car(Double lat1, Double long1, Double lat2, Double long2, Long time) {
        try {
            Node start = setupStart(lat1, long1);
            Node end = setupEnd(lat2, long2);
            CarCostEvaluation eval = new CarCostEvaluation();
            Dijkstra dijkstra = new Dijkstra(expander, eval);
            WeightedPath path = dijkstra.findSinglePath(start, end);
            return Response.status(Status.OK).entity(path).build();
        } catch (MobilITException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage() + " :" + e.getCause()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/searchpath/publictransport")
    public Response publicTransport(Double lat1, Double long1, Double lat2, Double long2, Double time) {
        return Response.status(Status.OK).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/searchpath/cycle")
    public Response cycle(Double lat1, Double long1, Double lat2, Double long2, Double time) {
        return Response.status(Status.OK).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/searchpath/biclou")
    public Response biclou(Double lat1, Double long1, Double lat2, Double long2, Double time) {
        return Response.status(Status.OK).build();
    }

    private Node setupStart(Double lat, Double lon) throws MobilITException {
        return getNode(lat, lon);
    }

    private Node setupEnd(Double lat, Double lon) throws MobilITException {
        return getNode(lat, lon);
    }

    private Node getNode(Double lat, Double lon) throws MobilITException {
        Coordinate myPosition = new Coordinate(lat, lon);
        List<GeoPipeFlow> results = OSMGeoPipeline.startNearestNeighborLatLonSearch(osm, myPosition, 100)
                .sort("OrthodromicDistance").toList();
        if (results.size() > 0) {
            GeoPipeFlow node = results.get(0);
            Node startNode = db.getNodeById(node.getRecord().getNodeId());
            return startNode;
        }
        else {
            throw new MobilITException("Start Node not found");
        }
    }
}
