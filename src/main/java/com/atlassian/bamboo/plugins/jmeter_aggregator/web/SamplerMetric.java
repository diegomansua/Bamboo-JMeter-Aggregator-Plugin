package com.atlassian.bamboo.plugins.jmeter_aggregator.web;

import com.atlassian.bamboo.plugins.jmeter_aggregator.Sampler;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

/**
 * Metric that can be used in a graph
 */
public enum SamplerMetric
{
    COUNT("count", "Number of samples"),
    SUCCESS_COUNT("successCount", "Number of successful samples"),
    PERCENT_SUCCESS("percentSuccess", "Percentage of successful samples"),
    MAX_VALUE("maxValue", "Value of largest sample"),
    MIN_VALUE("minValue", "Value of smallest sample"),
    TOTAL_VALUE("totalValue", "Total value of all samples"),
    MEDIAN_VALUE("medianValue", "Median sample value"),
    NINETY_PERCENT_VALUE("ninetyPercentValue", "Ninety precent line value"),
    AVERAGE_VALUE("averageValue", "Mean sample value"),
    THROUGHPUT("throughput", "Number of samples per minute", 1.0),
    TOTAL_TIME("totalTime", "Total real time of test run"),
    STANDARD_DEVIATION("standardDeviation", "Standard deviation of sample values");

    private static final Logger log = Logger.getLogger(SamplerMetric.class);

    private SamplerMetric(String property, String description)
    {
        this.property = property;
        this.description = description;
        this.scale = 1.0;
    }

    private SamplerMetric(String property, String description, double scale)
    {
        this.property = property;
        this.description = description;
        this.scale = scale;
    }

    private final String property;
    private final String description;
    private final double scale;

    public String getDescription()
    {
        return description;
    }

    public Number getValueFromSampler(Sampler sampler)
    {
        try
        {
            return (Number) PropertyUtils.getProperty(sampler, property);
        }
        catch (Exception e)
        {
            // This should never happen
            log.error("Error copying properties", e);
            return null;
        }
    }

    public long getScaledValueFromSampler(Sampler sampler)
    {
        return (long) (scale * getValueFromSampler(sampler).doubleValue());
    }

}
