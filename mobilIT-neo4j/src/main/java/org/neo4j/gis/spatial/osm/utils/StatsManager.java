package org.neo4j.gis.spatial.osm.utils;

import java.util.Collection;
import java.util.HashMap;

import org.neo4j.gis.spatial.Constants;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.Node;

public class StatsManager {

    private HashMap<String, TagStats> tagStats  = new HashMap<String, TagStats>();
    private HashMap<Integer, Integer> geomStats = new HashMap<Integer, Integer>(); ;

    public TagStats getTagStats(String type) {
        if (!tagStats.containsKey(type)) {
            tagStats.put(type, new TagStats(type));
        }
        return tagStats.get(type);
    }

    public int addToTagStats(String type, String key) {
        getTagStats("all").add(key);
        return getTagStats(type).add(key);
    }

    public int addToTagStats(String type, Collection<String> keys) {
        int count = 0;
        for (String key : keys) {
            count += addToTagStats(type, key);
        }
        return count;
    }

    public void printTagStats() {
        System.out.println("Tag statistics for " + tagStats.size() + " types:");
        for (String key : tagStats.keySet()) {
            TagStats stats = tagStats.get(key);
            System.out.println("\t" + key + ": " + stats);
        }
    }

    public void addGeomStats(Node geomNode) {
        if (geomNode != null) {
            addGeomStats((Integer) geomNode.getProperty(Constants.PROP_TYPE, null));
        }
    }

    public void addGeomStats(Integer geom) {
        Integer count = geomStats.get(geom);
        geomStats.put(geom, count == null ? 1 : count + 1);
    }

    public void dumpGeomStats() {
        System.out.println("Geometry statistics for " + geomStats.size() + " geometry types:");
        for (Object key : geomStats.keySet()) {
            Integer count = geomStats.get(key);
            System.out.println("\t" + SpatialDatabaseService.convertGeometryTypeToName((Integer) key) + ": " + count);
        }
        geomStats.clear();
    }

}
