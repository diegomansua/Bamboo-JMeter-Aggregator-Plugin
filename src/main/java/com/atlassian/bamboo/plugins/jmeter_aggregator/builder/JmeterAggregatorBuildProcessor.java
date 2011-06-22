package com.atlassian.bamboo.plugins.jmeter_aggregator.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.atlassian.bamboo.build.CustomBuildProcessor;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plugins.jmeter_aggregator.Sampler;
import com.atlassian.bamboo.plugins.jmeter_aggregator.SimpleSampler;
import com.atlassian.bamboo.plugins.jmeter_aggregator.parser.CsvParser;
import com.atlassian.bamboo.plugins.jmeter_aggregator.parser.JtlSaxHandler;
import com.atlassian.bamboo.plugins.jmeter_aggregator.parser.SampleResultsParser;
import com.atlassian.bamboo.utils.FileVisitor;
import com.atlassian.bamboo.v2.build.BuildContext;

/**
 * Build processor that collects a JMeter build log, and aggregates and stores the results. This runs on the agent, as
 * it needs access to the file system. Technically it is configurable, but we let all configuration be handled by the
 * server build processor, so that it appears to the user that there's only one build processor.
 */
public class JmeterAggregatorBuildProcessor implements CustomBuildProcessor {
    private BuildContext ctx;
    private boolean on;
    private String buildLogFile;
    private String csvLogFile;
    private String csvHeader;
    private boolean csvCustomHeader;

    private static final Logger log = Logger.getLogger(JmeterAggregatorBuildProcessor.class);

    @Override
    @NotNull
    public BuildContext call() throws Exception {
        if (!on) {
            return ctx;
        }
        File source;
        try {
            source = ctx.getBuildDefinition().getRepository().getSourceCodeDirectory(PlanKeys.getPlanKey(ctx.getPlanKey()));
        } catch (final NullPointerException npe) {
            // Don't know why this would happen, but obviously we can't process, just return ctx
            return ctx;
        }

        final Sampler totalSampler = new SimpleSampler();
        final Map<String, Sampler> simpleSamplerMap = new HashMap<String, Sampler>();

        if ((buildLogFile != null) && (buildLogFile.trim().length() > 0)) {
            final JtlSaxHandler jtlParser = new JtlSaxHandler();
            final XMLReader saxParser = XMLReaderFactory.createXMLReader();
            saxParser.setContentHandler(jtlParser);

            final FileVisitor fileVisitor = newFileVisitorBuildLog(source, saxParser);

            visitFilesAndCollectResults(simpleSamplerMap, fileVisitor, jtlParser, buildLogFile);
            PropertyUtils.copyProperties(totalSampler, jtlParser.getTotalSampler());
        }

        if ((csvLogFile != null) && (csvLogFile.trim().length() > 0)) {
            final CsvParser csvParser;
            if (csvCustomHeader) {
                csvParser = new CsvParser(csvHeader);
            } else {
                csvParser = new CsvParser();
            }
            final FileVisitor fileVisitor = newFileVisitorCsvLog(source, csvParser);
            visitFilesAndCollectResults(simpleSamplerMap, fileVisitor, csvParser, csvLogFile);
        }

        // Store
        JmeterBuildDataHelper.putSamplerDataInCustomData(ctx.getBuildResult().getCustomBuildData(), simpleSamplerMap,
                totalSampler);

        return ctx;
    }

    protected FileVisitor newFileVisitorCsvLog(final File source,
            final CsvParser csvParser) {
        return new FileVisitor(source) {
            @Override
            public void visitFile(final File file) {
                try {
                    csvParser.parse(file);
                } catch (final FileNotFoundException fnfe) {
                    // This probably means it's a directory
                    log.warn("Error parsing CSV output file", fnfe);
                    ctx.getErrorCollection().addErrorMessage("Error parsing CSV output file: " + file +
                            "  Is this a directory?  Please check your configuration so that it only matches the CSV files you want to include in the results.");
                } catch (final IOException ioe) {
                    log.warn("Error parsing CSV output file", ioe);
                    ctx.getErrorCollection().addErrorMessage("Error parsing CSV output file: " + file, ioe);
                }
            }
        };
    }

    protected FileVisitor newFileVisitorBuildLog(final File source, final XMLReader saxParser) {
        return new FileVisitor(source) {
            @Override
            public void visitFile(final File file) {
                try {
                    saxParser.parse(new InputSource(new FileReader(file)));
                } catch (final SAXException se) {
                    log.warn("Error parsing JMeter output file", se);
                    ctx.getErrorCollection().addErrorMessage("Error parsing JMeter output file: " + file, se);
                } catch (final IOException ioe) {
                    log.warn("Error parsing JMeter output file", ioe);
                    ctx.getErrorCollection().addErrorMessage("Error parsing JMeter output file: " + file, ioe);
                }
            }
        };
    }

    private void visitFilesAndCollectResults(final Map<String, Sampler> samplers, final FileVisitor fileVisitor,
            final SampleResultsParser parser, final String filePattern) throws Exception {
        fileVisitor.visitFilesThatMatch(filePattern);
        for (final Map.Entry<String, Sampler> samplerMapEntry : parser.getSamplerMap().entrySet()) {
            final Sampler simpleSampler = new SimpleSampler();
            try {
                PropertyUtils.copyProperties(simpleSampler, samplerMapEntry.getValue());
            } catch (final Exception e) {
                log.error("Error copying properties", e);
            }
            samplers.put(samplerMapEntry.getKey(), simpleSampler);
        }

    }

    @Override
    public void init(@NotNull final BuildContext ctx) {
        this.ctx = ctx;
        final Map<String, String> conf = ctx.getBuildDefinition().getCustomConfiguration();
        on = Boolean.valueOf(conf.get("custom.bamboo.jmeter_aggregation.on"));
        if (on) {
            buildLogFile = conf.get("custom.bamboo.jmeter_aggregation.buildLogFile");
            csvLogFile = conf.get("custom.bamboo.jmeter_aggregation.csvLogFile");
            csvHeader = conf.get("custom.bamboo.jmeter_aggregation.csvHeader");
            csvCustomHeader = Boolean.valueOf(conf.get("custom.bamboo.jmeter_aggregation.csvCustomHeader"));
        }
    }

}
