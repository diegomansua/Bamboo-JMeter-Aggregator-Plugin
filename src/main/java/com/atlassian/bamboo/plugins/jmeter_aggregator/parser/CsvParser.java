package com.atlassian.bamboo.plugins.jmeter_aggregator.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.bamboo.plugins.jmeter_aggregator.Sampler;
import com.atlassian.bamboo.plugins.jmeter_aggregator.SamplerImpl;

public class CsvParser implements SampleResultsParser {
    private static final String TIMESTAMP = "timestamp";
    private final Map<String, Sampler> samplerMap = new HashMap<String, Sampler>();
    private final String columns;
    private static Logger log = Logger.getLogger(CsvParser.class);

    /**
     * Create that uses the column headings (sampler names) as the first row
     */
    public CsvParser() {
        this.columns = null;
    }

    /**
     * Create a parser for CSV files that have no first row, and use the columns as the sampler names.
     * <p/>
     * A blank column heading means ignore this column, and a column heading of "timestamp" is a special heading name used for
     * the timestamp.
     * 
     * @param columns The Sampler names of the input file
     */
    public CsvParser(final String columns) {
        this.columns = columns;
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
     * <p/>
     * It doesn't make sense to have a total sampler for the CSV parser because the values are all of different metrics, eg CPU
     * usage
     * 
     * @return The total sampler.
     */
    @Override
    public SamplerImpl getTotalSampler() {
        return null;
    }

    /**
     * Parse the input file
     * 
     * @throws IOException If an error was encountered reading the input file
     */
    public void parse(final File file) throws IOException {
        final CsvReader reader = new CsvReader(new FileReader(file));
        try {
            // First, initialise the columns
            final List<String> columnNames = getColumnNames(reader);

            // Initialise the map
            final int timestampColumn = initialiseSamplerMap(columnNames);
            if (timestampColumn == -1) {
                // Should probably fail build, but for now, just output an error
                log.error(
                        "No timestamp column found in CSV data.  Please ensure you have a column called " + TIMESTAMP);
                return;
            }

            // Extrat values from the CSV file into the map
            extractValues(reader, columnNames, timestampColumn);
        } finally {
            reader.close();
        }
    }

    private int initialiseSamplerMap(final List<String> columnNames) {
        int timestampColumn = -1;
        for (int i = 0; i < columnNames.size(); i++) {
            final String columnName = columnNames.get(i);
            if ((columnName != null) && !TIMESTAMP.equals(columnName.toLowerCase())) {
                if (!samplerMap.containsKey(columnName)) {
                    samplerMap.put(columnName, new SamplerImpl(columnName));
                }
            } else if ((columnName != null) && TIMESTAMP.equals(columnName.toLowerCase())) {
                timestampColumn = i;
            }
        }
        return timestampColumn;
    }

    private void extractValues(final CsvReader reader, final List<String> columnNames, final int timestampColumn) throws IOException {
        String[] csvLine;
        while ((csvLine = reader.readNext()) != null) {
            // Get the timestamp
            long timestamp = 0;
            if (csvLine.length > timestampColumn) {
                timestamp = parseLong(csvLine[timestampColumn]);
            }
            for (int i = 0; i < csvLine.length; i++) {
                if ((i != timestampColumn) && (columnNames.size() > i)) {
                    final String samplerName = columnNames.get(i);
                    if (samplerName != null) {
                        final SamplerImpl sampler = (SamplerImpl)samplerMap.get(samplerName);
                        final long value = parseLong(csvLine[i]);
                        sampler.addSample(timestamp, value, true);
                    }
                }
            }
        }
    }

    private List<String> getColumnNames(final CsvReader reader) throws IOException {
        String[] columnNames;
        if ((columns != null) && (columns.trim().length() != 0)) {
            final CsvReader singleLineReader = new CsvReader(new StringReader(columns));
            columnNames = singleLineReader.readNext();
        } else {
            columnNames = reader.readNext();
        }
        final List<String> results = new ArrayList<String>();
        for (final String columnName : columnNames) {
            if ((columnName == null) || (columnName.trim().length() == 0)) {
                results.add(null);
            } else {
                results.add(columnName.trim());
            }
        }
        return results;
    }

    private long parseLong(final String str) {
        if (str == null) {
            return 0;
        }
        try {
            return Long.parseLong(str);
        } catch (final NumberFormatException nfe) {
            return 0;
        }
    }

}
