package mpasinski.jaxb.nestedelem;

import mpasinski.commons.TestUtils;
import mpasinski.commons.xml.JaxbUtils;
import mpasinski.jaxb.service.houses.rooms.Kitchen;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

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
    public void testUnmarshallNestedElementInvalidWay() throws JAXBException, IOException {
        String houseRequest = TestUtils.readClasspathFile("houseRequest.xml");

        JAXBContext jaxbContext = JaxbUtils.getJAXBContext(Kitchen.class);
        JAXBElement<Kitchen> result = jaxbContext.createUnmarshaller().unmarshal(new StreamSource(new ByteArrayInputStream(houseRequest.getBytes())), Kitchen.class);
        Kitchen unmarshalledKitchen =  result.getValue();
        assertNull(unmarshalledKitchen.getCupboard());
        printObject(unmarshalledKitchen);
        printObject(result);
    }

    @Test
    public void testUnmarshallNestedElement() throws JAXBException, XMLStreamException, IOException {
        String houseRequest = TestUtils.readClasspathFile("houseRequest.xml");
        String nodeName = "kitchen";
        Kitchen unmarshalledKitchen = null;

        //create XMLStreamReader (StAX)
        XMLInputFactory xif = XMLInputFactory.newFactory();
        XMLStreamReader xmlReader = xif.createXMLStreamReader(new StringReader(houseRequest));

        int event = 0;
        //here we advance to next tag untill we find node called "kitchen"
        for (event = xmlReader.next(); event != XMLStreamReader.END_DOCUMENT; event = xmlReader.next()) {
            if (event == XMLStreamReader.START_ELEMENT) {
                if (xmlReader.getLocalName() == nodeName) {
                    break;
                }
            }
        }

        if (event != XMLStreamReader.END_DOCUMENT) {
            JAXBContext jaxbContext = JaxbUtils.getJAXBContext(Kitchen.class);
            JAXBElement<Kitchen> result = jaxbContext.createUnmarshaller().unmarshal(xmlReader, Kitchen.class);
            unmarshalledKitchen = result.getValue();
        }
        assertNotNull(unmarshalledKitchen.getCupboard());
        assertNotNull(unmarshalledKitchen.getFridge());

        printObject(unmarshalledKitchen);
        printObject(unmarshalledKitchen.getCupboard());
        printObject(unmarshalledKitchen.getFridge());
    }

    private void printObject(Object o){
        System.out.println(ReflectionToStringBuilder.reflectionToString(o));
    }
}
