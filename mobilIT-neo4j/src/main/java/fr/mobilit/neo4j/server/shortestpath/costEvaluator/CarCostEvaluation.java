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

public class CarCostEvaluation implements CostEvaluator<Double> {

    @Override
    public Double getCost(Relationship relationship, Direction direction) {
        // Boolean isMotor = false;
        // if (relationship.getProperty("motor_vehicule", null).equals("yes")) {
        // isMotor = true;
        // }
        // Boolean isGoodWay = false;
        // String oneway = (String) relationship.getProperty("oneway", null);
        // if (oneway.equals("BOTH") || (direction.equals(Direction.OUTGOING) && oneway.equals("FORWARD"))
        // || (direction.equals(Direction.INCOMING) && oneway.equals("BACKWARD"))) {
        // isGoodWay = true;
        // }
        // if (isMotor && isGoodWay) {
        // Double length = (Double) relationship.getProperty("length", Constant.INFINY);
        // Integer speed = (Integer) relationship.getProperty("maxspeed", Constant.DEFAULT_SPEED);
        // return (length / 1000) / speed;
        // }
        // else {
        // return Constant.INFINY;
        // }
        Double length = Double.valueOf("" + relationship.getProperty("length", Constant.INFINY));
        Integer speed = Integer.valueOf("" + relationship.getProperty("maxspeed", Constant.DEFAULT_SPEED));
        return (length / 1000) / speed;
    }
}
