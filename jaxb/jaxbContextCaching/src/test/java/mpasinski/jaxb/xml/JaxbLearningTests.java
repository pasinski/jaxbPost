package mpasinski.jaxb.xml;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Created with IntelliJ IDEA.
 * User: mpasinski
 * Date: 08.01.13
 * Time: 20:16
 * To change this template use File | Settings | File Templates.
 */
public class JaxbLearningTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreateJaxbContextWithInvalidPackage() throws JAXBException {
        thrown.expect(JAXBException.class);
        thrown.expectMessage("\"mpasinski.jaxb.service\" doesnt contain ObjectFactory.class or jaxb.index");

        String invalidPackage = "mpasinski.jaxb.service";
        JAXBContext.newInstance(invalidPackage);
    }

    @Test
    public void testCreateJaxbContextWithValidPackage() throws JAXBException {
        String validPackage = "mpasinski.jaxb.service.furniture";
        JAXBContext ctx = JAXBContext.newInstance(validPackage);
        Assert.assertNotNull("JAXBContext should be created properly", ctx);
    }
}
