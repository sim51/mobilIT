package org.neo4j.gis.spatial.osm.utils;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class TagStats {

    private String                   name;
    private int                      count = 0;
    private HashMap<String, Integer> stats = new HashMap<String, Integer>();

    TagStats(String name) {
        this.name = name;
    }

    int add(String key) {
        count++;
        if (stats.containsKey(key)) {
            int num = stats.get(key);
            stats.put(key, ++num);
            return num;
        }
        else {
            stats.put(key, 1);
            return 1;
        }
    }

    /**
     * Return only reasonably commonly used tags.
     * 
     * @return
     */
    public String[] getTags() {
        if (stats.size() > 0) {
            int threshold = count / (stats.size() * 20);
            ArrayList<String> tags = new ArrayList<String>();
            for (String key : stats.keySet()) {
                if (key.equals("waterway")) {
                    System.out.println("debug[" + key + "]: " + stats.get(key));
                }
                if (stats.get(key) > threshold)
                    tags.add(key);
            }
            Collections.sort(tags);
            return tags.toArray(new String[tags.size()]);
        }
        else {
            return new String[0];
        }
    }

    public String toString() {
        return "TagStats[" + name + "]: " + asList(getTags());
    }
}
