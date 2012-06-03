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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import fr.mobilit.neo4j.server.pojo.GeoPoint;
import fr.mobilit.neo4j.server.pojo.Itinerary;
import fr.mobilit.neo4j.server.utils.MobilITRelation;
import fr.mobilit.neo4j.server.utils.SpatialUtils;

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
    public static List<Itinerary> search(SpatialDatabaseService spatial, Double lat1, Double long1, Double lat2,
            Double long2, CostEvaluator<Double> eval) throws MobilITException {
        try {
            SpatialUtils service = new SpatialUtils(spatial);
            Node start;
            start = service.findNearestWay(lat1, long1);
            Node end = service.findNearestWay(lat2, long2);
            Dijkstra<Double> sp = new Dijkstra<Double>(0.0, start, end, eval, new DoubleAdder(),
                    new DoubleComparator(), Direction.BOTH, MobilITRelation.LINKED);
            sp.calculate();

            // generate the itinerary
            List<Itinerary> itinerary = new ArrayList<Itinerary>();
            Map<String, Integer> alreadyIn = new HashMap<String, Integer>();
            for (Relationship relation : sp.getPathAsRelationships()) {
                String name = (String) relation.getProperty("name", null);
                if (name != null) {
                    if (alreadyIn.containsKey(name)) {
                        Integer index = alreadyIn.get(name);
                        Itinerary path = itinerary.get(index);
                        Double lng = (Double) relation.getEndNode().getProperty("lon", null);
                        Double lat = (Double) relation.getEndNode().getProperty("lat", null);
                        path.getLine().add(new GeoPoint(lng, lat));
                        Double distance = (Double) relation.getProperty("length", 0.0);
                        path.setDistance(path.getDistance() + distance);
                    }
                    else {
                        Itinerary path = new Itinerary();
                        path.setName(name);
                        Double lng_1 = (Double) relation.getStartNode().getProperty("lon", null);
                        Double lat_1 = (Double) relation.getStartNode().getProperty("lat", null);
                        path.getLine().add(new GeoPoint(lng_1, lat_1));
                        Double lng_2 = (Double) relation.getEndNode().getProperty("lon", null);
                        Double lat_2 = (Double) relation.getEndNode().getProperty("lat", null);
                        path.getLine().add(new GeoPoint(lng_2, lat_2));
                        Double distance = (Double) relation.getProperty("length", 0.0);
                        path.setDistance(distance);
                        itinerary.add(path);
                        alreadyIn.put(name, itinerary.size() - 1);
                    }
                }
            }
            return itinerary;
        } catch (MobilITException e) {
            throw e;
        }
    }

    /**
     * Generate the http response compatible openLS with velocity template.
     * 
     * @param path
     * @return
     */
    public static String generateResponse(List<Itinerary> path) {
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
        // get the template
        Template template = null;
        template = Velocity.getTemplate("templates/result.vm");
        // render template
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString();
    }

}
