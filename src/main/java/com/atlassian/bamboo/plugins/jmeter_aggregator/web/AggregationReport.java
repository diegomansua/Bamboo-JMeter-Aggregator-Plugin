package com.atlassian.bamboo.plugins.jmeter_aggregator.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.atlassian.bamboo.build.ViewBuild;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plugins.jmeter_aggregator.Sampler;
import com.atlassian.bamboo.plugins.jmeter_aggregator.builder.JmeterBuildDataHelper;
import com.atlassian.bamboo.plugins.jmeter_aggregator.builder.SamplerSeries;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.core.util.RandomGenerator;

public class AggregationReport extends ViewBuild {
    /**
     * 
     */
    private static final long serialVersionUID = -8449421817220534032L;

    private static final Logger log = Logger.getLogger(AggregationReport.class);

    // Input params
    private List<String> samplers;
    private List<String> metrics;
    private boolean includeTotal;
    private Collection<String> availableSamplers;
    private Map<String, Object> chart;

    public String doSelect() throws Exception {
        return SUCCESS;
    }

    public String doView() throws Exception {
        return SUCCESS;
    }

    public String doGenerate() {
        // Validate
        if (!includeTotal && ((samplers == null) || samplers.isEmpty())) {
            addActionError("You must select at least one sampler");
            return SUCCESS;
        }
        if ((metrics == null) || metrics.isEmpty()) {
            addActionError("You must select at least one metric");
            return SUCCESS;
        }
        final List<? extends ResultsSummary> summaries = getResultsList();
        // Initialise a map of lists every data point that we want
        final Map<SamplerSeries, List<Number[]>> series = getSeriesMap();
        final Map<SamplerSeries, List<Number[]>> totalSeries = getTotalSeriesMap();

        // Extract data, summary by summary, into the map
        extractDataIntoMaps(summaries, series, totalSeries);

        // Convert map into an XYSeriesCollection
        final XYSeriesCollection dataset = convertMapToXySeries(series, totalSeries);

        // Create the chart from the dataset
        createChart(dataset);

        return SUCCESS;
    }

    /**
     * Create the chart
     * 
     * @param dataset The dataset to create the chart from
     */
    private void createChart(final XYSeriesCollection dataset) {
        chart = new HashMap<String, Object>();
        final ChartRenderingInfo chartRenderingInfo = newChartRenderingInfo();
        final JFreeChart jchart = ChartFactory.createXYLineChart("", "Build Number", "Values", dataset,
                PlotOrientation.VERTICAL, true, false, false);

        // Set the tick units of the domain (x) axis so they are always integers, because you can't have
        // half a build.
        final XYPlot plot = (XYPlot)jchart.getPlot();
        final NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        try {
            final String location = getSavedChartLocation(chartRenderingInfo, jchart);

            chart.put("location", location);
            chart.put("width", 600);
            chart.put("height", 500);

            final String mapName = generateRandomString() + "_map";

            chart.put("imageMap", getImageMap(chartRenderingInfo, mapName));
            chart.put("imageMapName", mapName);

        } catch (final IOException e) {
            log.error(e, e);
        }
    }

    protected String getImageMap(final ChartRenderingInfo chartRenderingInfo,
            final String mapName) {
        return ChartUtilities.getImageMap(mapName, chartRenderingInfo);
    }

    protected String generateRandomString() {
        return RandomGenerator.randomString(5);
    }

    protected ChartRenderingInfo newChartRenderingInfo() {
        return new ChartRenderingInfo();
    }

    protected String getSavedChartLocation(
            final ChartRenderingInfo chartRenderingInfo, final JFreeChart jchart)
            throws IOException {
        return ServletUtilities.saveChartAsPNG(jchart, 600, 500, chartRenderingInfo, null);
    }

    /**
     * Convert the data map into a JFreeChart xy series
     * 
     * @param series The series to convert
     * @param totalSeries The total series
     * @return The JFreeChart series collection
     */
    private XYSeriesCollection convertMapToXySeries(final Map<SamplerSeries, List<Number[]>> series,
            final Map<SamplerSeries, List<Number[]>> totalSeries) {
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for (final Map.Entry<SamplerSeries, List<Number[]>> entry : series.entrySet()) {
            final SamplerSeries samplerSeries = entry.getKey();
            final List<Number[]> data = entry.getValue();
            if (!data.isEmpty()) {
                final XYSeries s = new XYSeries(
                        samplerSeries.getLabel() + " - " + samplerSeries.getMetric().getDescription());
                for (final Number[] values : data) {
                    s.add(values[0], values[1]);
                }
                dataset.addSeries(s);
            }
        }
        if (includeTotal) {
            for (final Map.Entry<SamplerSeries, List<Number[]>> entry : totalSeries.entrySet()) {
                final SamplerSeries samplerSeries = entry.getKey();
                final List<Number[]> data = entry.getValue();
                if (!data.isEmpty()) {
                    final XYSeries s = new XYSeries(
                            samplerSeries.getLabel() + " - " + samplerSeries.getMetric().getDescription());
                    for (final Number[] values : data) {
                        s.add(values[0], values[1]);
                    }
                    dataset.addSeries(s);
                }
            }
        }
        return dataset;
    }

    /**
     * Extract the data into the total series maps
     * 
     * @param summaries The summaries
     * @param series The series
     * @param totalSeries The total series
     */
    private void extractDataIntoMaps(final List<? extends ResultsSummary> summaries, final Map<SamplerSeries, List<Number[]>> series,
            final Map<SamplerSeries, List<Number[]>> totalSeries) {
        for (final ResultsSummary summary : summaries) {
            final Map<String, Sampler> samplerMap = JmeterBuildDataHelper.getSamplerMapFromCustomData(
                    summary.getCustomBuildData());
            for (final Map.Entry<SamplerSeries, List<Number[]>> entry : series.entrySet()) {
                final SamplerSeries samplerSeries = entry.getKey();
                final Sampler sampler = samplerMap.get(samplerSeries.getLabel());
                if (sampler != null) {
                    entry.getValue().add(
                            new Number[]{summary.getBuildNumber(), samplerSeries.getMetric().getScaledValueFromSampler(
                                    sampler)});
                }
            }
            if (includeTotal) {
                final Sampler totalSampler = JmeterBuildDataHelper.getTotalSamplerFromCustomData(
                        summary.getCustomBuildData());
                if (totalSampler != null) {
                    for (final Map.Entry<SamplerSeries, List<Number[]>> entry : totalSeries.entrySet()) {
                        final SamplerSeries samplerSeries = entry.getKey();
                        entry.getValue().add(
                                new Number[]{summary.getBuildNumber(), samplerSeries.getMetric().getScaledValueFromSampler(
                                        totalSampler)});
                    }
                }
            }
        }
    }

    /**
     * Create the map for the total sieries
     * 
     * @return The map for the total series
     */
    private Map<SamplerSeries, List<Number[]>> getTotalSeriesMap() {
        final Map<SamplerSeries, List<Number[]>> totalSeries = new HashMap<SamplerSeries, List<Number[]>>();
        if (includeTotal) {
            for (final String metric : metrics) {
                final SamplerMetric sm = SamplerMetric.valueOf(metric);
                if (sm != null) {
                    final SamplerSeries ss = new SamplerSeries("Total", sm);
                    totalSeries.put(ss, new ArrayList<Number[]>());
                }
            }
        }
        return totalSeries;
    }

    /**
     * Create the map for the data series
     * 
     * @return The map for the data series
     */
    private Map<SamplerSeries, List<Number[]>> getSeriesMap() {
        final Map<SamplerSeries, List<Number[]>> series = new HashMap<SamplerSeries, List<Number[]>>();
        if (samplers != null) {
            for (final String sampler : samplers) {
                for (final String metric : metrics) {
                    final SamplerMetric sm = SamplerMetric.valueOf(metric);
                    if (sm != null) {
                        final SamplerSeries ss = new SamplerSeries(sampler, sm);
                        series.put(ss, new ArrayList<Number[]>());
                    }
                }
            }
        }
        return series;
    }

    public Map<String, Object> getChart() {
        return chart;
    }

    public int getNumberOfProjects() {
        return planManager.getAllPlans().size();
    }

    public Collection<String> getAvailableSamplers() {
        if (availableSamplers == null) {
            final BuildResultsSummary summary = getBuild().getLatestBuildSummary();

            try {
                availableSamplers = JmeterBuildDataHelper.getSamplerMapFromCustomData(
                        summary.getCustomBuildData()).keySet();
            } catch (final NullPointerException npe) {
                availableSamplers = Collections.emptyList();
            }
        }
        return availableSamplers;
    }

    public Map<String, String> getAvailableMetrics() {
        final Map<String, String> metrics = new TreeMap<String, String>();
        for (final SamplerMetric metric : SamplerMetric.values()) {
            metrics.put(metric.name(), metric.getDescription());
        }
        return metrics;
    }

    public String getBuildNameFromKey(final String key) {
        if (key != null) {
            final Plan plan = planManager.getPlanByKey(key);
            if (plan != null) {
                return plan.getName();
            }
        }
        return key;
    }

    // --------------------------------------------------------------------------------------------------
    // Getters & Setters
    public List<String> getSamplers() {
        return samplers;
    }

    public void setSamplers(final List<String> samplers) {
        this.samplers = samplers;
    }

    public List<String> getMetrics() {
        return metrics;
    }

    public void setMetrics(final List<String> metrics) {
        this.metrics = metrics;
    }

    public boolean isIncludeTotal() {
        return includeTotal;
    }

    public void setIncludeTotal(final boolean includeTotal) {
        this.includeTotal = includeTotal;
    }
}