package mpasinski.jaxb.xml;

import mpasinski.commons.xml.JaxbUtils;
import mpasinski.jaxb.service.houses.rooms.Kitchen;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Michal Pasinski
 */
public class JaxbUtilsTest {


    @Test
    public void testGetJaxbContextIsCached() throws JAXBException {
        JAXBContext ctx = JaxbUtils.getJAXBContext(Kitchen.class);
        assertEquals("Both contexts must be equal, as context for Kitchen class should be cached",
                ctx, JaxbUtils.getJAXBContext(Kitchen.class));
        assertFalse("Contexts should not be equal, we are not using cache",
                ctx.equals(JAXBContext.newInstance(Kitchen.class)));
    }
}
