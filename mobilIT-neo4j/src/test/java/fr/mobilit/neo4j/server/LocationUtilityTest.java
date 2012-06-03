package fr.mobilit.neo4j.server;

import fr.mobilit.neo4j.server.util.Neo4jTestCase;
import net.opengis.xls.v_1_2_0.*;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User: noootsab
 * Date: 6/2/12
 * Time: 5:45 PM
 */
public class LocationUtilityTest extends Neo4jTestCase {
    private Import importPlugin;

    public LocationUtility locationUtility;

    @Before
    public void setUp() throws Exception {
        super.setUp(true);
        this.importPlugin = new Import(this.graphDb());
        String files = Thread.currentThread().getContextClassLoader().getResource("osm/nantes.osm").getFile();
        importPlugin.osm(files); //if it fails... there is (must exist) a test for it

        locationUtility = new LocationUtility(graphDb());
    }

    @Test
    public void testFindOneWithFullOfAddressInfo() {
        GeocodeRequestType request = new GeocodeRequestType();

        AddressType address1 = new AddressType();
        address1.setCountryCode("FR");
        address1.setPostalCode("44000");
        StreetAddressType street1 = new StreetAddressType();
        StreetNameType streetName1 = new StreetNameType();
        streetName1.setValue("Rue Saint Stanislas");
        street1.setStreet(Arrays.asList(streetName1));
        address1.setStreetAddress(street1);

        request.setAddress(Arrays.asList(address1));

        Response execute = null;
        try {
            execute = locationUtility.execute(locationUtility.factory.createGeocodeRequest(request));
        } catch (JAXBException e) {
            fail("unable to marshal simple request : " + e.getCause().getMessage());
        }

        Object entity = execute.getEntity();
        assertTrue(entity instanceof GeocodeResponseType);
        GeocodeResponseType response = (GeocodeResponseType) entity;
        assertEquals("The response must have one result only", 1, response.getGeocodeResponseList().size());
        assertEquals("Two ways should match the given street name", 2, response.getGeocodeResponseList().get(0).getNumberOfGeocodedAddresses().intValue());
        AddressType address = response.getGeocodeResponseList().get(0).getGeocodedAddress().get(0).getAddress();
        assertEquals("Too many streets for the requested address", 1, address.getStreetAddress().getStreet().size());
        assertEquals("oops street doesn't match ?", "Rue Saint Stanislas", address.getStreetAddress().getStreet().get(0).getValue());
    }

}
