package com.atlassian.bamboo.plugins.jmeter_aggregator.parser;

import java.util.HashMap;
import java.util.Map;

import net.jcip.annotations.NotThreadSafe;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassian.bamboo.plugins.jmeter_aggregator.Sampler;
import com.atlassian.bamboo.plugins.jmeter_aggregator.SamplerImpl;

/**
 * SAX handler for JTL files. The reason for using SAX is that JTL files can be very large, and we don't care about
 * getting all the data, just aggregating it all. So we avoid storing the entire XML file in memory, and rather just
 * store arrays of all the sample times in memory.
 * 
 * This handler can be used multiple times for parsing different JTL files into the one aggregator. However, it is not
 * thread safe.
 */
@NotThreadSafe
public class JtlSaxHandler extends DefaultHandler implements SampleResultsParser {
    private static final Logger log = Logger.getLogger(JtlSaxHandler.class);
    private static final String JTL_VERSION = "1.2";

    private final Map<String, Sampler> samplerMap;
    private final SamplerImpl totalSampler;

    public JtlSaxHandler() {
        samplerMap = new HashMap<String, Sampler>();
        totalSampler = new SamplerImpl(null);
    }

    /**
     * Get the all the samplers, as a map of sampler label to sampler.
     * 
     * @return The map of samplers
     */
    @Override
    public Map<String, Sampler> getSamplerMap() {
        return samplerMap;
    }

    /**
     * Get the total sampler
     * 
     * @return The total sampler.
     */
    @Override
    public SamplerImpl getTotalSampler() {
        return totalSampler;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if ("testResults".equals(localName)) {
            // Check the version and output a warning if it differs
            final String version = attributes.getValue("version");
            if (version == null) {
                log.warn("No version specified in JTL file");
            } else {
                if (!version.equals(JTL_VERSION)) {
                    log.warn("Parsing unknown JTL version: " + version);
                }
            }
        } else if ("sample".equals(localName) || "httpSample".equals(localName)) {
            // Get the label
            String label = attributes.getValue("lb");
            if ((label == null) || (label.length() == 0)) {
                label = "null";
            }
            SamplerImpl sampler = (SamplerImpl)samplerMap.get(label);
            if (sampler == null) {
                sampler = new SamplerImpl(label);
                samplerMap.put(label, sampler);
            }
            final long sampleTime = getLongAttribute(attributes, "t");
            final long startTime = getLongAttribute(attributes, "ts");
            final boolean pass = getBooleanAttribute(attributes, "s");
            sampler.addSample(startTime, sampleTime, pass);
            totalSampler.addSample(startTime, sampleTime, pass);
        }
    }

    private long getLongAttribute(final Attributes attributes, final String qName) {
        final String value = attributes.getValue(qName);
        if (value == null) {
            return 0;
        }
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException nfe) {
            return 0;
        }
    }

    private boolean getBooleanAttribute(final Attributes attributes, final String qName) {
        final String value = attributes.getValue(qName);
        if (value == null) {
            return false;
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (final NumberFormatException nfe) {
            return false;
        }
    }

}
