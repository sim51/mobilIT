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
package fr.mobilit.neo4j.server.shortestpath.costEvaluator;

import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;

import fr.mobilit.neo4j.server.utils.Constant;

public class PedestrianCostEvaluation implements CostEvaluator<Double> {

    @Override
    public Double getCost(Relationship relationship, Direction direction) {
        Double length = Double.valueOf("" + relationship.getProperty("length", Constant.INFINY));
        Integer speed = Constant.DEFAULT_PEDESTRIAN_SPEED;
        return (length / 1000) / speed;
    }

}
