package mpasinski.jaxb.xml;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Maciej Zielinski, Rule Financial
 */
public final class JaxbUtils {

    private JaxbUtils() {
        throw new UnsupportedOperationException("JaxbUtils is a utility class that should not be instantiated");
    }

    private static final LoadingCache<String, JAXBContext> jaxbContextsCache = CacheBuilder.newBuilder()
            .build(
                    new CacheLoader<String, JAXBContext>() {
                        @Override
                        public JAXBContext load(String packageName) throws Exception {
                            return JAXBContext.newInstance(packageName);
                        }
                    }
            );

    public static JAXBContext getJAXBContext(Class clazz) {
        String packageName = clazz.getPackage().getName();
        try {
            return jaxbContextsCache.get(packageName);
        } catch (ExecutionException e) {
            throw new JaxbUtilsException("Error when getting JAXBContext from cache", e);
        }
    }

    public static <T> ByteArrayOutputStream marshal(JAXBElement<T> jaxbElement) throws JAXBException {
        checkNotNull(jaxbElement, "jaxbElement");

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        JAXBContext jaxbContext = getJAXBContext(jaxbElement.getDeclaredType());
        jaxbContext.createMarshaller().marshal(jaxbElement, result);
        return result;
    }

    public static <T> Document marshalToDocument(JAXBElement<T> jaxbElement) throws JAXBException,
            ParserConfigurationException {
        checkNotNull(jaxbElement, "jaxbElement");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
        JAXBContext jaxbContext = getJAXBContext(jaxbElement.getDeclaredType());
        jaxbContext.createMarshaller().marshal(jaxbElement, doc);
        return doc;
    }

    public static String marshallToString(Object toBeMarshalled) {
        return doMarshallToString(toBeMarshalled, false);
    }

    public static String marshallToString(Object toBeMarshalled, boolean prettyPrint) {
        return doMarshallToString(toBeMarshalled, prettyPrint);
    }

    private static String doMarshallToString(Object toBeMarshalled, boolean prettyPrint)  {
        checkNotNull(toBeMarshalled, "object to be marshalled");
        Object parsableObjectToBeMarshalled = createJaxbParsableObject(toBeMarshalled);

        if (parsableObjectToBeMarshalled != null) {
            StringWriter sw = new StringWriter();
            JAXBContext ctx = getJAXBContext(toBeMarshalled.getClass());
            try{
                Marshaller marshaller = ctx.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, prettyPrint);
                marshaller.marshal(parsableObjectToBeMarshalled, sw);
                return sw.toString();
            } catch(JAXBException jbe){
                throw new JaxbUtilsException(
                        String.format("Cannot marshall object [%s] of class [%s] to string",
                                toBeMarshalled, toBeMarshalled.getClass()), jbe);
            }
        }
        throw new JaxbUtilsException(
                String.format("Could not create parsable object [%s] of class [%s] to string",
                        toBeMarshalled, toBeMarshalled.getClass()));
    }

    public static <T> T unmarshal(Class<T> clazz, InputStream inputStream) throws JAXBException {
        checkNotNull(clazz, "clazz");
        checkNotNull(inputStream, "inputStream");

        JAXBContext jaxbContext = getJAXBContext(clazz);
        JAXBElement<T> result = jaxbContext.createUnmarshaller().unmarshal(new StreamSource(inputStream), clazz);
        return result.getValue();
    }

    public static <T> T unmarshal(Class<T> clazz, Node node) throws JAXBException {
        checkNotNull(clazz, "clazz");
        checkNotNull(node, "node");

        JAXBContext jaxbContext = getJAXBContext(clazz);
        JAXBElement<T> result = jaxbContext.createUnmarshaller().unmarshal(node, clazz);
        return result.getValue();
    }

    public static <T> T unmarshall(Class<T> clazz, String xmlString) throws JAXBException {
        checkNotNull(clazz, "clazz");
        checkNotNull(xmlString, "xmlString");

        JAXBContext jaxbContext = getJAXBContext(clazz);
        JAXBElement<T> result = jaxbContext.createUnmarshaller().unmarshal(new StreamSource(new ByteArrayInputStream(xmlString.getBytes())), clazz);
        return result.getValue();
    }

    public static <T> T unmarshall(Class<T> clazz, XMLStreamReader xmlStreamReader) throws JAXBException {
        checkNotNull(clazz, "clazz");
        checkNotNull(xmlStreamReader, "xmlStreamReader");

        JAXBContext jaxbContext = getJAXBContext(clazz);
        JAXBElement<T> result = jaxbContext.createUnmarshaller().unmarshal(xmlStreamReader, clazz);
        return result.getValue();
    }

    public static <T> T unmarshallNestedElement(Class<T> clazz, String xmlString, String nestedElemName) throws XMLStreamException, JAXBException {
        XMLStreamReader streamFromString = createXMLStreamReader(xmlString);
        return unmarshallNestedElement(clazz, streamFromString, nestedElemName);
    }

    public static <T> T unmarshallNestedElement(Class<T> clazz, XMLStreamReader xmlStreamReader, String nestedElemName) throws XMLStreamException, JAXBException {
        checkNotNull(clazz, "clazz");
        checkNotNull(xmlStreamReader, "xmlStreamReader");
        checkNotNull(nestedElemName, "nestedElemName");

        XMLStreamReader readerToUnmarshall = findNode(xmlStreamReader, nestedElemName);
        if (readerToUnmarshall != null) {
            JAXBContext jaxbContext = getJAXBContext(clazz);
            JAXBElement<T> result = jaxbContext.createUnmarshaller().unmarshal(readerToUnmarshall, clazz);
            return result.getValue();
        }
        return null;
    }

    private static XMLStreamReader findNode(String xmlString, String nodeName) throws XMLStreamException {
        XMLStreamReader xmlReader = createXMLStreamReader(xmlString);
        return findNode(xmlReader, nodeName);
    }

    public static boolean isJaxbParseable(Object object) {
        return JaxbUtils.isJaxbElementRootAnnotated(object.getClass()) || object instanceof JAXBElement;
    }

    public static boolean isJaxbElementRootAnnotated(Class clazz) {
        return clazz.getAnnotation(XmlRootElement.class) != null;
    }

    public static Object createJaxbParsableObject(Object o) {
        if (isJaxbParseable(o)) {
            return o;
        }
        try {
            Object objectFactory = getObjectFactoryForObject(o);
            Method createJaxbElementMethod = getXmlElementDeclAnnotadedMethod(objectFactory.getClass(), o);
            if (createJaxbElementMethod != null) {
                return createJaxbElementMethod.invoke(objectFactory, o.getClass().cast(o));
            } else {
                throw new JaxbUtilsException("Cannot obtain JaxbParseable object");
            }
        } catch (InvocationTargetException e) {
            throw new JaxbUtilsException("Error occured when invoking ObjectFactory create method by reflection", e);
        } catch (Exception e) {
            throw new JaxbUtilsException("Error occured when trying to create JaxbParsable object");
        }
    }

    private static Object getObjectFactoryForObject(Object o) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class objectFactoryClass = getObjectFactoryClass(o);
        return objectFactoryClass.newInstance();
    }

    private static Class getObjectFactoryClass(Object o) throws ClassNotFoundException {
        Package tPackage = o.getClass().getPackage();
        String objectFactoryClassName = String.format("%s.%s", tPackage.getName(), "ObjectFactory");
        return Class.forName(objectFactoryClassName);
    }

    private static Method getXmlElementDeclAnnotadedMethod(Class clazz, Object o) {
        Method[] allMethods = clazz.getDeclaredMethods();
        for (Method m : allMethods) {
            boolean isXmlElementDeclAnnotated = m.isAnnotationPresent(XmlElementDecl.class);
            boolean isAcceptingCorrectArgument = Arrays.equals(m.getParameterTypes(), new Class[]{o.getClass()});
            if (isXmlElementDeclAnnotated && isAcceptingCorrectArgument) {
                return m;
            }
        }
        return null;
    }

    private static XMLStreamReader findNode(XMLStreamReader xmlReader, String nodeName) throws XMLStreamException {
        int event = 0;
        for (event = xmlReader.next(); event != XMLStreamReader.END_DOCUMENT; event = xmlReader.next()) {
            if (event == XMLStreamReader.START_ELEMENT) {
                if (xmlReader.getLocalName() == nodeName) {
                    break;
                }
            }
        }
        if (isEndDocument(event)) {
            return null;
        }
        return xmlReader;
    }

    private static XMLStreamReader createXMLStreamReader(String xmlString) throws XMLStreamException {
        XMLInputFactory xif = XMLInputFactory.newFactory();
        XMLStreamReader xmlReader = xif.createXMLStreamReader(new StringReader(xmlString));
        return xmlReader;
    }

    private static boolean isEndDocument(int event) {
        return event == XMLStreamReader.END_DOCUMENT;
    }
}
