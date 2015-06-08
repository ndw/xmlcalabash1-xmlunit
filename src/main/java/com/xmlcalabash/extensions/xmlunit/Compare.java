/*
* Compare.java (XMLUnit Version)
*
* Copyright 2009 Mark Logic Corporation.
* Portions Copyright 2007 Sun Microsystems, Inc.
* All rights reserved.
*
* The contents of this file are subject to the terms of either the GNU
* General Public License Version 2 only ("GPL") or the Common
* Development and Distribution License("CDDL") (collectively, the
* "License"). You may not use this file except in compliance with the
* License. You can obtain a copy of the License at
* https://xproc.dev.java.net/public/CDDL+GPL.html or
* docs/CDDL+GPL.txt in the distribution. See the License for the
* specific language governing permissions and limitations under the
* License. When distributing the software, include this License Header
* Notice in each file and include the License file at docs/CDDL+GPL.txt.
*/

package com.xmlcalabash.extensions.xmlunit;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;

import com.xmlcalabash.core.XMLCalabash;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.util.S9apiUtils;
import com.xmlcalabash.util.TreeWriter;
import com.xmlcalabash.util.XProcURIResolver;
import net.sf.saxon.s9api.*;

import com.xmlcalabash.runtime.XAtomicStep;

// ---- XML Unit dependencies ----
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import junit.framework.AssertionFailedError;
// -------------------------------

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@XMLCalabash(
        name = "cxu:compare",
        type = "{http://xmlcalabash.com/ns/extensions/xmlunit}compare")

public class Compare extends DefaultStep {
    private static final QName c_result = new QName("c", XProcConstants.NS_XPROC_STEP, "result");

    private static final QName _fail_if_not_equal        = new QName("","fail-if-not-equal");
    private static final QName _compare_unmatched        = new QName("","compare-unmatched");
    private static final QName _ignore_comments          = new QName("","ignore-comments");
    private static final QName _ignore_whitespace        = new QName("","ignore-whitespace");
    private static final QName _normalize                = new QName("","normalize");
    private static final QName _normalize_whitespace     = new QName("","normalize-whitespace");
    private static final QName _ignore_diff_between_text_and_cdata = new QName("","ignore-diff-between-text-and-cdata");

    private static final boolean default_compare_unmatched        = false;
    private static final boolean default_ignore_comments          = false;
    private static final boolean default_ignore_whitespace        = false;
    private static final boolean default_normalize                = false;
    private static final boolean default_normalize_whitespace     = false;
    private static final boolean default_ignore_diff_between_text_and_cdata = false;

    private static final String library_xpl = "http://xmlcalabash.com/extension/steps/xmlunit.xpl";
    private static final String library_url = "/com/xmlcalabash/extensions/xmlunit/library.xpl";

    private ReadablePipe source = null;
    private ReadablePipe alternate = null;
    private WritablePipe result = null;

    static
    {
        XMLUnit.setTransformerFactory("net.sf.saxon.TransformerFactoryImpl");
        XMLUnit.setXPathFactory("net.sf.saxon.xpath.XPathFactoryImpl");
        // XMLUnit.setXSLTVersion("2.0"); // XML Unit has been tested with XSLT version "1.0"
    }

    /**
     * Creates a new instance of XML Unit Compare
     */
    public Compare(XProcRuntime runtime, XAtomicStep step) {
        super(runtime, step);
    }

    public void setInput(String port, ReadablePipe pipe) {
        if("source".equals(port))
            source = pipe;
        else
            alternate = pipe;
    }

    public void setOutput(String port, WritablePipe pipe) {
        result = pipe;
    }

    public void reset() {
        source.resetReader();
        result.resetWriter();
    }

    private String getXMLDocument(XdmNode saxonNode) throws SaxonApiException
    {
        Serializer serializer = makeSerializer();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        serializer.setOutputStream(stream);
        S9apiUtils.serialize(runtime, saxonNode, serializer);

        try {
            return stream.toString("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            // This can't happen...
            throw new XProcException(uee);
        }
    }

    public void run() throws SaxonApiException {
        super.run();

        String sourceDoc = getXMLDocument(source.read());
        String alternateDoc = getXMLDocument(alternate.read());

        boolean same = false;

        try
        {
            XMLUnit.setCompareUnmatched(getOption(_compare_unmatched, default_compare_unmatched));
            XMLUnit.setIgnoreComments(getOption(_ignore_comments, default_ignore_comments));
            XMLUnit.setIgnoreDiffBetweenTextAndCDATA(getOption(_ignore_diff_between_text_and_cdata, default_ignore_diff_between_text_and_cdata));
            XMLUnit.setIgnoreWhitespace(getOption(_ignore_whitespace, default_ignore_whitespace));
            XMLUnit.setNormalize(getOption(_normalize, default_normalize));

            XMLAssert.assertXMLEqual(sourceDoc, alternateDoc);

            same = true;
        }
        catch(AssertionFailedError e) { }
        catch(SAXException e) { throw new SaxonApiException(e.getMessage()); }
        catch(IOException e) { throw new SaxonApiException(e.getMessage()); }
        catch(Exception e) { throw new SaxonApiException(e.getMessage());  }

        if (!same && getOption(_fail_if_not_equal, false)) {
            throw XProcException.stepError(19);
        }

        TreeWriter treeWriter = new TreeWriter(runtime);
        treeWriter.startDocument(step.getNode().getBaseURI());
        treeWriter.addStartElement(c_result);
        treeWriter.startContent();
        treeWriter.addText(String.valueOf(same));
        treeWriter.addEndElement();
        treeWriter.endDocument();

        result.write(treeWriter.getResult());
    }

    public static void configureStep(XProcRuntime runtime) {
        XProcURIResolver resolver = runtime.getResolver();
        URIResolver uriResolver = resolver.getUnderlyingURIResolver();
        URIResolver myResolver = new StepResolver(uriResolver);
        resolver.setUnderlyingURIResolver(myResolver);
    }

    private static class StepResolver implements URIResolver {
        Logger logger = LoggerFactory.getLogger(Compare.class);
        URIResolver nextResolver = null;

        public StepResolver(URIResolver next) {
            nextResolver = next;
        }

        @Override
        public Source resolve(String href, String base) throws TransformerException {
            try {
                URI baseURI = new URI(base);
                URI xpl = baseURI.resolve(href);
                if (library_xpl.equals(xpl.toASCIIString())) {
                    URL url = Compare.class.getResource(library_url);
                    logger.debug("Reading library.xpl for cxu:compare from " + url);
                    InputStream s = Compare.class.getResourceAsStream(library_url);
                    if (s != null) {
                        SAXSource source = new SAXSource(new InputSource(s));
                        return source;
                    } else {
                        logger.info("Failed to read " + library_url + " for cxu:compare");
                    }
                }
            } catch (URISyntaxException e) {
                // nevermind
            }

            if (nextResolver != null) {
                return nextResolver.resolve(href, base);
            } else {
                return null;
            }
        }
    }
}


