package mpasinski.jaxb.xml;

import mpasinski.commons.TestUtils;
import mpasinski.jaxb.service.furniture.Cupboard;
import mpasinski.jaxb.service.furniture.Fridge;
import mpasinski.jaxb.service.houses.House;
import mpasinski.jaxb.service.houses.ObjectFactory;
import mpasinski.jaxb.service.houses.rooms.Kitchen;
import mpasinski.jaxb.service.houses.rooms.LivingRoom;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author Michal Pasinski
 */
public class JaxbUtilsTest {


    @Test
    public void testGetJaxbContext() throws JAXBException {
        JAXBContext ctx = JaxbUtils.getJAXBContext(Kitchen.class);
        assertEquals(ctx, JaxbUtils.getJAXBContext(Kitchen.class));
        Assert.assertFalse(ctx.equals(JAXBContext.newInstance(Kitchen.class)));
    }


    @Test
    public void testIsJaxbParseable() {
        Kitchen globalAnonymousTypeElement = new Kitchen();
        JAXBElement<House> globalElementWithNamedType = new ObjectFactory().createHouseObject(new House());
        House notParsable = new House();

        assertTrue(JaxbUtils.isJaxbParseable(globalAnonymousTypeElement));
        assertTrue(JaxbUtils.isJaxbParseable(globalElementWithNamedType));
        assertFalse(JaxbUtils.isJaxbParseable(notParsable));
    }

    @Test
    public void testCreateParsableJaxbObjectFromXmlRootAnnotated() {
        Kitchen alreadyParsableKitchen = new Kitchen();
        Kitchen createdByJaxbUtils = (Kitchen) JaxbUtils.createJaxbParsableObject(alreadyParsableKitchen);

        assertEquals(alreadyParsableKitchen, createdByJaxbUtils);
    }

    @Test
    public void testCreateParsalbeWhenPossibleToCreateJAXBElement() {
        House notYetParsable = new House();
        Object parsable = JaxbUtils.createJaxbParsableObject(notYetParsable);

        assertFalse(JaxbUtils.isJaxbParseable(notYetParsable));
        assertTrue(JaxbUtils.isJaxbParseable(parsable));
        assertEquals(notYetParsable, ((JAXBElement) parsable).getValue());
    }

    @Test(expected = JaxbUtilsException.class)
    public void testCreateParsableWhenNotPossible() {
        LivingRoom notPossibleToCreateParsableFromMe = new LivingRoom();
        Object parsable = JaxbUtils.createJaxbParsableObject(notPossibleToCreateParsableFromMe);
    }

    @Test
    public void testUnmarshallCalypsoUploadDocumentFromString() throws IOException, JAXBException {
        String xmlString = TestUtils.readClasspathFile("Kitchen.xml");
        Kitchen unmarshalledKitchen = JaxbUtils.unmarshall(Kitchen.class, xmlString);

        Kitchen newKitchen = new Kitchen();
        newKitchen.setIsLocked(true);
        Cupboard cupboard = new Cupboard();
        cupboard.setCapacity(7);
        newKitchen.setCupboard(cupboard);
        Fridge fridge = new Fridge();
        //fridge.setBoughtOn();
        fridge.setInnerTemperature(-4);
        newKitchen.setFridge(fridge);

        String marshalledKitchen = JaxbUtils.marshallToString(newKitchen, true);

        Assert.assertNotNull(unmarshalledKitchen);
    }

}
