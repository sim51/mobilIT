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

import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphalgo.impl.shortestpath.Dijkstra;
import org.neo4j.graphalgo.impl.util.DoubleAdder;
import org.neo4j.graphalgo.impl.util.DoubleComparator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import fr.mobilit.neo4j.server.shortestpath.costEvaluator.CarCostEvaluation;
import fr.mobilit.neo4j.server.utils.MobilITRelation;
import fr.mobilit.neo4j.server.utils.SpatialUtils;
import fr.mobilit.neo4j.server.utils.TemplateUtils;

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
    public Response car(Double lat1, Double long1, Double lat2, Double long2, Long time) {
        try {
            SpatialUtils service = new SpatialUtils(spatial);
            Node start = service.findNearestWay(lat1, long1);
            Node end = service.findNearestWay(lat2, long2);
            CarCostEvaluation eval = new CarCostEvaluation();
            Dijkstra<Double> sp = new Dijkstra<Double>(0.0, start, end, eval, new DoubleAdder(),
                    new DoubleComparator(), Direction.BOTH, MobilITRelation.LINKED);
            sp.calculate();
            return Response.status(Status.OK).entity(toResponse(sp.getPathAsRelationships(), sp.getCost())).build();
        } catch (Exception e) {
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

    private String toResponse(List<Relationship> path, Double cost) {
        // initialize velocity
        Properties props = new Properties();
        props.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
        props.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
        props.setProperty("runtime.log.logsystem.log4j.logger", "VELOCITY");
        props.setProperty("classpath." + VelocityEngine.RESOURCE_LOADER + ".class",
                ClasspathResourceLoader.class.getName());
        Velocity.init(props);
        VelocityContext context = new VelocityContext();
        // put parameter for template
        context.put("path", path);
        context.put("cost", cost);
        context.put("Utils", TemplateUtils.class);
        // get the template
        Template template = null;
        template = Velocity.getTemplate("templates/result.vm");
        // render template
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString();
    }

}
