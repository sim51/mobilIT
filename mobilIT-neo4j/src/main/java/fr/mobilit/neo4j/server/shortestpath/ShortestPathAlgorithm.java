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
package fr.mobilit.neo4j.server.shortestpath;

import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.impl.shortestpath.Dijkstra;
import org.neo4j.graphalgo.impl.util.DoubleAdder;
import org.neo4j.graphalgo.impl.util.DoubleComparator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import fr.mobilit.neo4j.server.exception.MobilITException;
import fr.mobilit.neo4j.server.utils.MobilITRelation;
import fr.mobilit.neo4j.server.utils.SpatialUtils;
import fr.mobilit.neo4j.server.utils.TemplateUtils;

public class ShortestPathAlgorithm {

    /**
     * Algorithm to find the shorestpath from OSM road data.
     * 
     * @param spatial
     * @param lat1
     * @param long1
     * @param lat2
     * @param long2
     * @param eval
     * @return
     * @throws MobilITException
     */
    public static String search(SpatialDatabaseService spatial, Double lat1, Double long1, Double lat2, Double long2,
            CostEvaluator<Double> eval) throws MobilITException {
        try {
            SpatialUtils service = new SpatialUtils(spatial);
            Node start;
            start = service.findNearestWay(lat1, long1);
            Node end = service.findNearestWay(lat2, long2);
            Dijkstra<Double> sp = new Dijkstra<Double>(0.0, start, end, eval, new DoubleAdder(),
                    new DoubleComparator(), Direction.BOTH, MobilITRelation.LINKED);
            sp.calculate();
            return generateResponse(sp.getPathAsRelationships(), sp.getCost());
        } catch (MobilITException e) {
            throw e;
        }
    }

    /**
     * Generate the http response compatible openLS with velocity template.
     * 
     * @param path
     * @param cost
     * @return
     */
    // TODO : doing an openLS compatible response
    private static String generateResponse(List<Relationship> path, Double cost) {
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
