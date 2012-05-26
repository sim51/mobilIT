package org.neo4j.gis.spatial.osm.utils;

import java.util.Map;

import org.neo4j.collections.rtree.Envelope;
import org.neo4j.gis.spatial.Constants;

public class GeometryMetaData {

    private Envelope bbox     = new Envelope();
    private int      vertices = 0;
    private int      geometry = -1;

    public GeometryMetaData(int type) {
        this.geometry = type;
    }

    public int getGeometryType() {
        return geometry;
    }

    public void expandToIncludePoint(double[] location) {
        bbox.expandToInclude(location[0], location[1]);
        vertices++;
        geometry = -1;
    }

    public void expandToIncludeBBox(Map<String, Object> nodeProps) {
        double[] sbb = (double[]) nodeProps.get("bbox");
        bbox.expandToInclude(sbb[0], sbb[2]);
        bbox.expandToInclude(sbb[1], sbb[3]);
        vertices += (Integer) nodeProps.get("vertices");
    }

    public void checkSupportedGeometry(Integer memGType) {
        if ((memGType == null || memGType != Constants.GTYPE_LINESTRING) && geometry != Constants.GTYPE_POLYGON) {
            geometry = -1;
        }
    }

    public void setPolygon() {
        geometry = Constants.GTYPE_POLYGON;
    }

    public boolean isValid() {
        return geometry > 0;
    }

    public int getVertices() {
        return vertices;
    }

    public Envelope getBBox() {
        return bbox;
    }
}
