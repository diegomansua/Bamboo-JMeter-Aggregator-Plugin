package com.atlassian.bamboo.plugins.jmeter_aggregator.parser;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

import com.atlassian.bamboo.plugins.jmeter_aggregator.Sampler;
import com.atlassian.bamboo.plugins.jmeter_aggregator.SamplerImpl;
import net.jcip.annotations.NotThreadSafe;

/**
 * SAX handler for JTL files.  The reason for using SAX is that JTL files can be very large, and we don't care about
 * getting all the data, just aggregating it all.  So we avoid storing the entire XML file in memory, and rather just
 * store arrays of all the sample times in memory.
 *
 * This handler can be used multiple times for parsing different JTL files into the one aggregator.  However, it is not
 * thread safe.
 */
@NotThreadSafe
public class JtlSaxHandler extends DefaultHandler implements SampleResultsParser
{
    private static final Logger log = Logger.getLogger(JtlSaxHandler.class);
    private static final String JTL_VERSION = "1.2";

    private final Map<String, Sampler> samplerMap;
    private final SamplerImpl totalSampler;

    public JtlSaxHandler()
    {
        samplerMap = new HashMap<String, Sampler>();
        totalSampler = new SamplerImpl(null);
    }

    /**
     * Get the all the samplers, as a map of sampler label to sampler.
     *
     * @return The map of samplers
     */
    public Map<String, Sampler> getSamplerMap()
    {
        return samplerMap;
    }

    /**
     * Get the total sampler
     *
     * @return The total sampler.
     */
    public SamplerImpl getTotalSampler()
    {
        return totalSampler;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if ("testResults".equals(localName))
        {
            // Check the version and output a warning if it differs
            String version = attributes.getValue("version");
            if (version == null)
            {
                log.warn("No version specified in JTL file");
            }
            else
            {
                if (!version.equals(JTL_VERSION))
                {
                    log.warn("Parsing unknown JTL version: " + version);
                }
            }
        }
        else if ("sample".equals(localName) || "httpSample".equals(localName))
        {
            // Get the label
            String label = attributes.getValue("lb");
            if (label == null || label.length() == 0)
            {
                label = "null";
            }
            SamplerImpl sampler = (SamplerImpl) samplerMap.get(label);
            if (sampler == null)
            {
                sampler = new SamplerImpl(label);
                samplerMap.put(label, sampler);
            }
            long sampleTime = getLongAttribute(attributes, "t");
            long startTime = getLongAttribute(attributes, "ts");
            boolean pass = getBooleanAttribute(attributes, "s");
            sampler.addSample(startTime, sampleTime, pass);
            totalSampler.addSample(startTime, sampleTime, pass);
        }
    }

    private long getLongAttribute(Attributes attributes, String qName)
    {
        String value = attributes.getValue(qName);
        if (value == null)
        {
            return 0;
        }
        try
        {
            return Long.parseLong(value);
        }
        catch (NumberFormatException nfe)
        {
            return 0;
        }
    }

    private boolean getBooleanAttribute(Attributes attributes, String qName)
    {
        String value = attributes.getValue(qName);
        if (value == null)
        {
            return false;
        }
        try
        {
            return Boolean.parseBoolean(value);
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }
    }

}
