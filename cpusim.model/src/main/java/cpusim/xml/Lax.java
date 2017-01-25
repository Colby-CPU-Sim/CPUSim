/*
 * Sample code by Mark Johnson, JavaWorld, April 2000.
 * Code is may be used for any legal purpose, including commercial
 * purposes, with no warranty expressed or implied.
 * email: mark.johnson@javaworld.com
 *
 * Modified by Dale Skrien, June, 2001 for use with CPU Sim
 */

package cpusim.xml;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import static com.google.common.base.Preconditions.checkNotNull;

public class Lax extends org.xml.sax.helpers.DefaultHandler
{

    // LAX translates XML content into method calls on this object
    private Vector<Object> _vecHandlers = null;
    private Vector<String> _vecTags = null;
    private static Class[] _caNoArgs = null;
    private static Class[] _caAttrList = null;
    private static Class[] _caString = null;
    private static Class[] _caLocator = null;
    private ErrorHandler errorHandler;

    // Initialize class arrays used for reflection
    static
    {
        _caNoArgs = new Class[]{};
        _caAttrList = new Class[]{org.xml.sax.Attributes.class};
        _caString = new Class[]{java.lang.String.class};
        _caLocator = new Class[]{org.xml.sax.Locator.class};
    }

    //--------------------------
    /**
     * Lax default constructor
     */
    public Lax()
    {
        super();
        _vecHandlers = new Vector<Object>();
        _vecTags = new Vector<String>();
        errorHandler = null;
    }

    //--------------------------
    /**
     * Lax ctor with a single handler
     * @param handler_ the Object that handles the document
     */
    public Lax(Object handler_)
    {
        super();
        _vecHandlers = new Vector<Object>();
        _vecTags = new Vector<String>();
        addHandler(handler_);
    }

    //--------------------------
    /**
     * Add a handler to the list of handler objects.
     * @param objHandler_ java.lang.Object
     */
    public void addHandler(Object objHandler_)
    {
        _vecHandlers.addElement(objHandler_);
    }

    //--------------------------
    /**
     * pass the locator on to the handlers if they
     * have a "setDocumentLocator(Locator locator)"
     * method.
     */
    public void setDocumentLocator(Locator locator)
    {
        // Call every setDocumentLocator method found in the list of handlers.
        for (int i = 0; i < _vecHandlers.size(); i++) {
            Object oThisHandler = _vecHandlers.elementAt(i);
            Method setDocLocMethod =
                    mFindMethod(oThisHandler, "setDocumentLocator", _caLocator);
            if (setDocLocMethod != null) {
                // Call the method
                try {
                    setDocLocMethod.invoke(oThisHandler, locator);
                } catch (Exception mre) {
                    //do nothing since the handler blew it and so doesn't get
                    //to set its locator
                }
            }
        }

    }

    //--------------------------
    /**
     * Add an ErrorHandler
     * @param errorHandler_ ErrorHandler
     */
    public void addErrorHandler(ErrorHandler errorHandler_)
    {
        errorHandler = errorHandler_;
    }

    //--------------------------
    /**
     * Remove all handlers and clear the state for parsing a new XML file.
     */
    public void reset()
    {
        _vecHandlers.removeAllElements();
        _vecTags.removeAllElements();
    }

    //--------------------------
    /**
     * Handle an incoming block of text by calling the textOf method for the
     * current tag.
     */
    public void characters(char[] caChars, int iStart, int iEnd)
            throws SAXException
    {
        String sCurrentTag = sCurrentTag();

        if (sCurrentTag != null) {
            int i;
            String sTextMethodName = "textOf" + sCurrentTag;
            String sArg = null;

            // Call every text method for current tag found in the list
            //of handlers
            for (i = 0; i < _vecHandlers.size(); i++) {
                Object oThisHandler = _vecHandlers.elementAt(i);
                Method mTextMethod =
                        mFindMethod(oThisHandler, sTextMethodName, _caString);
                if (mTextMethod != null) {
                    if (sArg == null) {
                        sArg = new String(caChars, iStart, iEnd);
                    }
                    try {
                        mTextMethod.invoke(oThisHandler, sArg);
                    } catch (InvocationTargetException ite) {
                        //Note:  java.lang.reflect.InvocationTargetException.getMessage()
                        //just returns the ite's message even if it is null.  So
                        //in that case, we'll rewrap the target exception
                        //in a SAXException.
                        if (ite.getMessage() == null
                                && ite.getTargetException() != null
                                && ite.getTargetException() instanceof Exception)
                            throw new SAXException((Exception) ite.getTargetException());
                        else
                            throw new SAXException(ite);
                    } catch (Exception mre) {
                        throw new SAXException(mre);
                    }
                }
            }
        }
    }

    //--------------------------
    /**
     * endDocument method comment.
     */
    public void endDocument() throws org.xml.sax.SAXException
    {
    }

    //--------------------------
    /**
     * Call all end tag methods in the handler list
     * @param qName the ending tag
     * @throws org.xml.sax.SAXException if something goes wrong when parsing
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException
    {
        int i;
        String sEndMethodName = "end" + qName;

        // Call every tag start method for this tag found in the list
        //of handlers.
        for (i = 0; i < _vecHandlers.size(); i++) {
            Object oThisHandler = _vecHandlers.elementAt(i);
            Method mEndMethod =
                    mFindMethod(oThisHandler, sEndMethodName, _caNoArgs);
            if (mEndMethod != null) {
                try {
                    mEndMethod.invoke(oThisHandler);
                } catch (InvocationTargetException ite) {
                    //Note:  java.lang.reflect.InvocationTargetException.getMessage()
                    //just returns the ite's message even if it is null.  So
                    //in that case, we'll rewrap the target exception
                    //in a SAXException.
                    if (ite.getMessage() == null
                            && ite.getTargetException() != null
                            && ite.getTargetException() instanceof Exception)
                        throw new SAXException((Exception) ite.getTargetException());
                    else
                        throw new SAXException(ite);
                } catch (Exception mre) {
                    throw new SAXException(mre);
                }
            }
        }
        popTag();
    }

    //--------------------------
    /**
     * Return a method of object oHandler
     * with the given name and argument list, or null if not found
     * @return java.lang.reflect.Method
     * @param oHandler java.lang.Object - handler object to search for a method
     * @param sMethodName java.lang.String - The tag to find.
     * @param caArgs the array of Class objects forming the arguments
     */
    private Method mFindMethod(Object oHandler, String sMethodName,
                               Class[] caArgs)
    {
        Method m = null;
        Class classOfHandler = oHandler.getClass();

        // Find a method with the given name and argument list
        try {
            m = classOfHandler.getMethod(sMethodName, caArgs);
        } catch (NoSuchMethodException ex) {
            // Ignore exception - no such method exists.
        }
        return m;
    }

    /**
     * Creates a {@link SAXParser} instance using the {@link SAXParserFactory}.
     * @param isValidating
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private SAXParser createSaxParser(boolean isValidating) throws ParserConfigurationException, SAXException {
        // Get a "parser factory", an an object that creates parsers
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        // Set up the factory to fromRootController the appropriate type of parser
        saxParserFactory.setValidating(isValidating);
        saxParserFactory.setNamespaceAware(false); // Not this month...

        return saxParserFactory.newSAXParser();
    }

    //--------------------------
    /**
     * Reimplement this method to use a parser from a different vendor. See your
     * parser package documentation for details.
     * @param isValidating boolean Indicates whether or not the factory is configured to produce parsers which validate
     *                     the XML content during parse.
     * @param handler org.xml.sax.DocumentHandler
     * @param sFile java.io.File -- the file to be parsed
     * @throws Exception if the file is not found
     *
     * @throws NullPointerException if the {@code sFile} is {@code null}
     *
     * @see SAXParserFactory#isValidating()
     */
    public void parseDocument(boolean isValidating,
                              DefaultHandler handler,
                              File sFile)
            throws ParserConfigurationException, SAXException, IOException {
        checkNotNull(sFile);
        SAXParser parser = createSaxParser(isValidating);
        parser.parse(sFile, handler);
    }

    /**
     * Reimplement this method to use a parser from a different vendor. See your
     * parser package documentation for details.
     * @param isValidating boolean
     * @param handler org.xml.sax.DocumentHandler
     * @param instream Stream to be parsed, this does not close the stream.
     * @throws Exception if the file is not found
     * @throws NullPointerException if {@code instream} is null
     */
    public void parseDocument(boolean isValidating, DefaultHandler handler,
                              InputStream instream) throws ParserConfigurationException, SAXException, IOException {
        checkNotNull(instream);

        SAXParser parser = createSaxParser(isValidating);
        parser.parse(instream, handler);
    }

    //--------------------------
    /**
     * Pop tag off of tag stack.
     */
    private void popTag()
    {
        _vecTags.removeElementAt(_vecTags.size() - 1);
    }

    //--------------------------
    /**
     * Push tag onto tag stack.
     * @param sTag java.lang.String
     */
    private void pushTag(String sTag)
    {
        _vecTags.addElement(sTag);
    }

    //--------------------------
    /**
     * Return tag at top of tag stack. At any particular point in the parse,
     * this string represents the tag being processed.
     * @return java.lang.String
     */
    private String sCurrentTag()
    {
        int iIndex = _vecTags.size() - 1;
        if (iIndex >= 0) {
            return _vecTags.elementAt(_vecTags.size() - 1);
        }
        else {
            return null;
        }
    }

    //--------------------------
    /**
     * startDocument method comment.
     */
    public void startDocument() throws org.xml.sax.SAXException
    {
    }

    //--------------------------
    /**
     * Call all start methods for this tag.
     * @param qName the start tag
     * @param alAttrs the attributes
     * @throws org.xml.sax.SAXException if something goes wrong
     */
    public void startElement(String uri, String localName,
                             String qName, Attributes alAttrs)
            throws org.xml.sax.SAXException
    {
        int i;
        String sStartMethodName = "start" + qName;

        pushTag(qName);

        // Call every tag start method for this tag found in the list of handlers.
        for (i = 0; i < _vecHandlers.size(); i++) {
            Object oThisHandler = _vecHandlers.elementAt(i);
            Method mStartMethod = mFindMethod(oThisHandler, sStartMethodName, _caAttrList);
            if (mStartMethod == null) {
                mStartMethod = mFindMethod(oThisHandler, sStartMethodName, _caNoArgs);
            }
            if (mStartMethod != null) {
                // Call start method with or without attribute list
                Class[] caMethodArgs = mStartMethod.getParameterTypes();
                try {
                    if (caMethodArgs.length == 0) {
                        mStartMethod.invoke(oThisHandler);
                    }
                    else {
                        mStartMethod.invoke(oThisHandler, alAttrs);
                    }
                } catch (InvocationTargetException ite) {
                    //Note:  java.lang.reflect.InvocationTargetException.getMessage()
                    //just returns the ite's message even if it is null.  So
                    //in that case, we'll rewrap the target exception
                    //in a SAXException.
                    //ite.printStackTrace();
                    if (ite.getMessage() == null
                            && ite.getTargetException() != null
                            && ite.getTargetException() instanceof Exception)
                        throw new SAXException((Exception) ite.getTargetException());
                    else
                        throw new SAXException(ite);
                } catch (Exception mre) {
                    throw new SAXException(mre);
                }
            }
        }
    }

    //--------------------------
    /**
     * error method comment.
     */
    public void error(SAXParseException ex) throws SAXException
    {
        if (errorHandler != null)
            errorHandler.error(ex);
        else
            throw ex;
    }

    //--------------------------
    /**
     * fatalError method comment.
     */
    public void fatalError(SAXParseException ex) throws SAXException
    {
        if (errorHandler != null)
            errorHandler.fatalError(ex);
        else
            throw ex;
    }

    //--------------------------
    /**
     * warning method.
     */
    public void warning(SAXParseException ex) throws SAXException
    {
        if (errorHandler != null)
            errorHandler.warning(ex);
        //else do nothing since warnings should allow parsing to continue
    }
}