package mpasinski.commons.xml;

/**
 * Created with IntelliJ IDEA.
 * User: mpasinski
 * Date: 06.12.12
 * Time: 21:57
 * To change this template use File | Settings | File Templates.
 */
public class JaxbUtilsException extends RuntimeException {
    public JaxbUtilsException() {
    }

    public JaxbUtilsException(String message) {
        super(message);
    }

    public JaxbUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

    public JaxbUtilsException(Throwable cause) {
        super(cause);
    }
}
