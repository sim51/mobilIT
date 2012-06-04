package fr.mobilit.neo4j.server;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.opengis.gml.v_3_1_1.CoordType;
import net.opengis.gml.v_3_1_1.PointType;
import net.opengis.xls.v_1_2_0.AddressType;
import net.opengis.xls.v_1_2_0.GeocodeRequestType;
import net.opengis.xls.v_1_2_0.GeocodeResponseListType;
import net.opengis.xls.v_1_2_0.GeocodeResponseType;
import net.opengis.xls.v_1_2_0.GeocodedAddressType;
import net.opengis.xls.v_1_2_0.ObjectFactory;
import net.opengis.xls.v_1_2_0.StreetAddressType;
import net.opengis.xls.v_1_2_0.StreetNameType;

import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.gis.spatial.osm.OSMRelation;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

/**
 * User: noootsab Date: 6/2/12 Time: 2:48 PM
 */
@Path("/location")
public class LocationUtility {

    public JAXBContext             context;
    public Marshaller              marshaller;
    public ObjectFactory           factory;
    private GraphDatabaseService   db;
    private SpatialDatabaseService spatial;

    public LocationUtility(@Context GraphDatabaseService db) throws Exception {
        this.db = db;
        this.spatial = new SpatialDatabaseService(db);

        this.context = JAXBContext.newInstance(ObjectFactory.class);
        this.marshaller = context.createMarshaller();
        this.factory = new ObjectFactory();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_XML })
    @Produces(MediaType.APPLICATION_XML)
    public Response execute(JAXBElement<GeocodeRequestType> request) throws JAXBException {

        GeocodeRequestType geocodeRequest = request.getValue();

        if (geocodeRequest == null) {
            Response.status(Response.Status.BAD_REQUEST).entity("Geocode Request is missing !");
        }

        // minimal response
        GeocodeResponseType response = new GeocodeResponseType();
        GeocodeResponseListType geocodeResponseListType = new GeocodeResponseListType();
        geocodeResponseListType.setNumberOfGeocodedAddresses(new BigInteger("0"));
        response.setGeocodeResponseList(Arrays.asList(geocodeResponseListType));

        if (geocodeRequest.getAddress().size() > 0) {
            AddressType addressType = geocodeRequest.getAddress().get(0);
            if (addressType != null && addressType.getStreetAddress() != null
                    && addressType.getStreetAddress().getStreet().size() > 0) {

                StreetNameType streetNameType1 = addressType.getStreetAddress().getStreet().get(0);

                // quick and dirty implementation based on
                // one single address
                // the name only => EXACT MATCH ONLY
                IndexHits<Node> ways = db.index().forNodes(OSMImporter.INDEX_NAME_WAY)
                        .query("name:\"" + streetNameType1.getValue() + "\"");

                // fill response with result
                for (Node way : ways) {
                    double[] bbox = (double[]) way.getRelationships(Direction.OUTGOING, OSMRelation.GEOM).iterator()
                            .next().getEndNode().getProperty("bbox");

                    GeocodedAddressType geocodedAddressType = new GeocodedAddressType();

                    PointType value = new PointType();
                    CoordType value1 = new CoordType();
                    value1.setX(new BigDecimal(bbox[0] + (bbox[1] - bbox[0]) / 2));
                    value1.setY(new BigDecimal(bbox[2] + (bbox[3] - bbox[2]) / 2));
                    value1.setZ(new BigDecimal(0));
                    value.setCoord(value1);
                    geocodedAddressType.setPoint(value);

                    AddressType value2 = new AddressType();
                    StreetAddressType value3 = new StreetAddressType();
                    StreetNameType streetNameType = new StreetNameType();
                    streetNameType.setValue((String) way.getProperty("name"));
                    value3.setStreet(Arrays.asList(streetNameType));
                    value2.setStreetAddress(value3);
                    geocodedAddressType.setAddress(value2);

                    geocodeResponseListType.setGeocodedAddress(Arrays.asList(geocodedAddressType));

                    // I hate the BigInteger API !
                    geocodeResponseListType.setNumberOfGeocodedAddresses(geocodeResponseListType
                            .getNumberOfGeocodedAddresses().add(new BigInteger("1")));
                }

            }
        }

        return Response.status(Response.Status.OK).entity(response).build();
    }
}
