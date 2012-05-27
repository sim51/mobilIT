package fr.mobilit.neo4j.server.utils;

import org.neo4j.graphdb.Relationship;

public class TemplateUtils {

    public static String getRoadName(Relationship relation) {
        return "" + relation.getProperty("name", "");
    }

    public static String getRoadLength(Relationship relation) {
        return "" + relation.getProperty("length", "");
    }
}
