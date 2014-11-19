/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.tracecompass.tmf.core.trace.text.AbstractCustomTrace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Trace object for custom XML trace parsers.
 *
 * @author Patrick Tassé
 * @since 3.0
 */
public class CustomXmlTrace extends AbstractCustomTrace {

    private static final int DEFAULT_CACHE_SIZE = 100;
    private static final int MAX_LINES = 100;
    private static final int CONFIDENCE = 100;

    private final CustomXmlTraceDefinition fDefinition;
    private final CustomXmlEventType fEventType;
    private final CustomXmlInputElement fRecordInputElement;
    private BufferedRandomAccessFile fFile;

    /**
     * Basic constructor
     *
     * @param definition
     *            Trace definition
     */
    public CustomXmlTrace(final CustomXmlTraceDefinition definition) {
        fDefinition = definition;
        fEventType = new CustomXmlEventType(fDefinition);
        fRecordInputElement = getRecordInputElement(fDefinition.rootInputElement);
        setCacheSize(DEFAULT_CACHE_SIZE);
    }

    /**
     * Full constructor
     *
     * @param resource
     *            Trace resource
     * @param definition
     *            Trace definition
     * @param path
     *            Path to the trace/log file
     * @param pageSize
     *            Page size to use
     * @throws TmfTraceException
     *             If the trace/log couldn't be opened
     */
    public CustomXmlTrace(final IResource resource,
            final CustomXmlTraceDefinition definition, final String path,
            final int pageSize) throws TmfTraceException {
        this(definition);
        setCacheSize((pageSize > 0) ? pageSize : DEFAULT_CACHE_SIZE);
        initTrace(resource, path, CustomXmlEvent.class);
    }

    @Override
    public synchronized CustomXmlEvent parseEvent(final ITmfContext tmfContext) {
        ITmfContext context = seekEvent(tmfContext.getLocation());
        return parse(context);
    }

    @Override
    public synchronized CustomXmlEvent getNext(final ITmfContext context) {
        final ITmfContext savedContext = new TmfContext(context.getLocation(), context.getRank());
        final CustomXmlEvent event = parse(context);
        if (event != null) {
            updateAttributes(savedContext, event.getTimestamp());
            context.increaseRank();
        }
        return event;
    }

    private synchronized CustomXmlEvent parse(final ITmfContext tmfContext) {
        if (fFile == null) {
            return null;
        }
        if (!(tmfContext instanceof CustomXmlTraceContext)) {
            return null;
        }

        final CustomXmlTraceContext context = (CustomXmlTraceContext) tmfContext;
        if (context.getLocation() == null || !(context.getLocation().getLocationInfo() instanceof Long) || NULL_LOCATION.equals(context.getLocation())) {
            return null;
        }

        CustomXmlEvent event = null;
        try {
            // Below +1 for the <
            if (fFile.getFilePointer() != (Long) context.getLocation().getLocationInfo() + 1) {
                fFile.seek((Long) context.getLocation().getLocationInfo() + 1);
            }
            final StringBuffer elementBuffer = new StringBuffer("<"); //$NON-NLS-1$
            readElement(elementBuffer, fFile);
            final Element element = parseElementBuffer(elementBuffer);

            event = extractEvent(element, fRecordInputElement);
            ((StringBuffer) event.getContent().getValue()).append(elementBuffer);

            long rawPos = fFile.getFilePointer();
            String line = fFile.getNextLine();
            while (line != null) {
                final int idx = indexOfElement(fRecordInputElement.getElementName(), line, 0);
                if (idx != -1) {
                    context.setLocation(new TmfLongLocation(rawPos + idx));
                    return event;
                }
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
        } catch (final IOException e) {
            Activator.logError("Error parsing event. File: " + getPath(), e); //$NON-NLS-1$

        }
        context.setLocation(NULL_LOCATION);
        return event;
    }

    private Element parseElementBuffer(final StringBuffer elementBuffer) {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            final EntityResolver resolver = new EntityResolver() {
                @Override
                public InputSource resolveEntity(final String publicId, final String systemId) {
                    final String empty = ""; //$NON-NLS-1$
                    final ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
                    return new InputSource(bais);
                }
            };
            db.setEntityResolver(resolver);

            // The following catches xml parsing exceptions
            db.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(final SAXParseException saxparseexception) throws SAXException {
                }

                @Override
                public void warning(final SAXParseException saxparseexception) throws SAXException {
                }

                @Override
                public void fatalError(final SAXParseException saxparseexception) throws SAXException {
                    throw saxparseexception;
                }
            });

            final Document doc = db.parse(new ByteArrayInputStream(elementBuffer.toString().getBytes()));
            return doc.getDocumentElement();
        } catch (final ParserConfigurationException e) {
            Activator.logError("Error parsing element buffer. File:" + getPath(), e); //$NON-NLS-1$
        } catch (final SAXException e) {
            Activator.logError("Error parsing element buffer. File:" + getPath(), e); //$NON-NLS-1$
        } catch (final IOException e) {
            Activator.logError("Error parsing element buffer. File: " + getPath(), e); //$NON-NLS-1$
        }
        return null;
    }

    private static int indexOfElement(String elementName, String line, int fromIndex) {
        final String recordElementStart = '<' + elementName;
        int index = line.indexOf(recordElementStart, fromIndex);
        if (index == -1) {
            return index;
        }
        int nextCharIndex = index + recordElementStart.length();
        if (nextCharIndex < line.length()) {
            char c = line.charAt(nextCharIndex);
            // Check that the match is not just a substring of another element
            if (Character.isLetterOrDigit(c)) {
                return indexOfElement(elementName, line, nextCharIndex);
            }
        }
        return index;
    }

    private void readElement(final StringBuffer buffer, final RandomAccessFile raFile) {
        try {
            int numRead = 0;
            boolean startTagClosed = false;
            int i;
            while ((i = raFile.read()) != -1) {
                numRead++;
                final char c = (char) i;
                buffer.append(c);
                if (c == '"') {
                    readQuote(buffer, raFile, '"');
                } else if (c == '\'') {
                    readQuote(buffer, raFile, '\'');
                } else if (c == '<') {
                    readElement(buffer, raFile);
                } else if (c == '/' && numRead == 1) {
                    break; // found "</"
                } else if (c == '-' && numRead == 3 && buffer.substring(buffer.length() - 3, buffer.length() - 1).equals("!-")) { //$NON-NLS-1$
                    readComment(buffer, raFile); // found "<!--"
                } else if (i == '>') {
                    if (buffer.charAt(buffer.length() - 2) == '/') {
                        break; // found "/>"
                    } else if (startTagClosed) {
                        break; // found "<...>...</...>"
                    }
                    else {
                        startTagClosed = true; // found "<...>"
                    }
                }
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    private static void readQuote(final StringBuffer buffer,
            final RandomAccessFile raFile, final char eq) {
        try {
            int i;
            while ((i = raFile.read()) != -1) {
                final char c = (char) i;
                buffer.append(c);
                if (c == eq)
                {
                    break; // found matching end-quote
                }
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    private static void readComment(final StringBuffer buffer,
            final RandomAccessFile raFile) {
        try {
            int numRead = 0;
            int i;
            while ((i = raFile.read()) != -1) {
                numRead++;
                final char c = (char) i;
                buffer.append(c);
                if (c == '>' && numRead >= 2 && buffer.substring(buffer.length() - 3, buffer.length() - 1).equals("--")) //$NON-NLS-1$
                {
                    break; // found "-->"
                }
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    /**
     * Parse an XML element.
     *
     * @param parentElement
     *            The parent element
     * @param buffer
     *            The contents to parse
     * @return The parsed content
     */
    public static StringBuffer parseElement(final Element parentElement, final StringBuffer buffer) {
        final NodeList nodeList = parentElement.getChildNodes();
        String separator = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (separator == null) {
                    separator = " | "; //$NON-NLS-1$
                } else {
                    buffer.append(separator);
                }
                final Element element = (Element) node;
                if (!element.hasChildNodes()) {
                    buffer.append(element.getNodeName());
                } else if (element.getChildNodes().getLength() == 1 && element.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                    buffer.append(element.getNodeName() + ":" + element.getFirstChild().getNodeValue().trim()); //$NON-NLS-1$
                } else {
                    buffer.append(element.getNodeName());
                    buffer.append(" [ "); //$NON-NLS-1$
                    parseElement(element, buffer);
                    buffer.append(" ]"); //$NON-NLS-1$
                }
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                if (node.getNodeValue().trim().length() != 0) {
                    buffer.append(node.getNodeValue().trim());
                }
            }
        }
        return buffer;
    }

    /**
     * Get an input element if it is a valid record input. If not, we will look
     * into its children for valid inputs.
     *
     * @param inputElement
     *            The main element to check for.
     * @return The record element
     */
    public CustomXmlInputElement getRecordInputElement(final CustomXmlInputElement inputElement) {
        if (inputElement.isLogEntry()) {
            return inputElement;
        } else if (inputElement.getChildElements() != null) {
            for (final CustomXmlInputElement childInputElement : inputElement.getChildElements()) {
                final CustomXmlInputElement recordInputElement = getRecordInputElement(childInputElement);
                if (recordInputElement != null) {
                    return recordInputElement;
                }
            }
        }
        return null;
    }

    /**
     * Extract a trace event from an XML element.
     *
     * @param element
     *            The element
     * @param inputElement
     *            The input element
     * @return The extracted event
     */
    public CustomXmlEvent extractEvent(final Element element, final CustomXmlInputElement inputElement) {
        final CustomXmlEvent event = new CustomXmlEvent(fDefinition, this, TmfTimestamp.ZERO, fEventType);
        event.setContent(new CustomEventContent(event, new StringBuffer()));
        parseElement(element, event, inputElement);
        return event;
    }

    private void parseElement(final Element element, final CustomXmlEvent event, final CustomXmlInputElement inputElement) {
        if (inputElement.getInputName() != null && !inputElement.getInputName().equals(CustomXmlTraceDefinition.TAG_IGNORE)) {
            event.parseInput(parseElement(element, new StringBuffer()).toString(), inputElement.getInputName(), inputElement.getInputAction(), inputElement.getInputFormat());
        }
        if (inputElement.getAttributes() != null) {
            for (final CustomXmlInputAttribute attribute : inputElement.getAttributes()) {
                event.parseInput(element.getAttribute(attribute.getAttributeName()), attribute.getInputName(), attribute.getInputAction(), attribute.getInputFormat());
            }
        }
        final NodeList childNodes = element.getChildNodes();
        if (inputElement.getChildElements() != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node node = childNodes.item(i);
                if (node instanceof Element) {
                    for (final CustomXmlInputElement child : inputElement.getChildElements()) {
                        if (node.getNodeName().equals(child.getElementName())) {
                            parseElement((Element) node, event, child);
                            break;
                        }
                    }
                }
            }
        }
        return;
    }

    @Override
    public CustomTraceDefinition getDefinition() {
        return fDefinition;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation sets the confidence to 100 if any of the first
     * 100 lines of the file contains a valid record input element, and 0
     * otherwise.
     */
    @Override
    public IStatus validate(IProject project, String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CustomTrace_FileNotFound + ": " + path); //$NON-NLS-1$
        }
        try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(path, "r")) { //$NON-NLS-1$
            int lineCount = 0;
            long rawPos = 0;
            String line = rafile.getNextLine();
            while ((line != null) && (lineCount++ < MAX_LINES)) {
                final int idx = indexOfElement(fRecordInputElement.getElementName(), line, 0);
                if (idx != -1) {
                    rafile.seek(rawPos + idx + 1); // +1 is for the <
                    final StringBuffer elementBuffer = new StringBuffer("<"); //$NON-NLS-1$
                    readElement(elementBuffer, rafile);
                    final Element element = parseElementBuffer(elementBuffer);
                    if (element != null) {
                        rafile.close();
                        return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
                    }
                }
                rawPos = rafile.getFilePointer();
                line = rafile.getNextLine();
            }
        } catch (IOException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }
        return new TraceValidationStatus(0, Activator.PLUGIN_ID);
    }

    @Override
    protected BufferedRandomAccessFile getFile() {
        return fFile;
    }

    @Override
    protected void setFile(BufferedRandomAccessFile file) {
        fFile = file;
    }

    @Override
    protected TmfContext match(TmfContext context, long rawPos, String line) throws IOException {
        final int idx = indexOfElement(fRecordInputElement.getElementName(), line, 0);
        if (idx != -1) {
            context.setLocation(new TmfLongLocation(rawPos + idx));
            return context;
        }
        return null;
    }

    /**
     * @since 3.1
     */
    @Override
    protected CustomXmlTraceContext getNullContext() {
        return new CustomXmlTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
    }

}
