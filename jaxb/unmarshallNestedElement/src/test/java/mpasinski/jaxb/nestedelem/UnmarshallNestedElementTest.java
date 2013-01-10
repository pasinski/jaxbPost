package mpasinski.jaxb.nestedelem;

import mpasinski.commons.TestUtils;
import mpasinski.commons.xml.JaxbUtils;
import mpasinski.jaxb.service.houses.rooms.Kitchen;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * Created with IntelliJ IDEA.
 * User: mpasinski
 * Date: 08.01.13
 * Time: 23:27
 * To change this template use File | Settings | File Templates.
 */
public class UnmarshallNestedElementTest {

    private String houseXML;

    @Before
    public void setUp() throws IOException, JAXBException {
        houseXML = TestUtils.readClasspathFile("house.xml");
    }

    @Test
    public void testUnmarshallNestedElementInvalidWay() throws JAXBException {
        Kitchen unmarshalledKitchen = JaxbUtils.unmarshall(Kitchen.class, houseXML);
        assertNull(unmarshalledKitchen.getCupboard());
    }

    @Test
    public void testUnmarshallNestedElement() throws JAXBException, XMLStreamException {
        Kitchen unmarshalledKitchen = JaxbUtils.unmarshallNestedElement(Kitchen.class, houseXML, "kitchen");
        assertNotNull(unmarshalledKitchen.getCupboard());
    }
}
