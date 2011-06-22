package com.atlassian.bamboo.plugins.jmeter_aggregator;

/**
 * Simple sampler, for storing sampler results so they can be serialised/deserialised by XStream
 */
public class SimpleSampler implements Sampler
{
    private String label;
    private int successCount;
    private long startTime;
    private long finishTime;
    private long maxValue;
    private long minValue;
    private long totalValue;
    private long medianValue;
    private long ninetyPercentValue;
    private long throughput;
    private long averageValue;
    private long count;
    private int percentSuccess;
    private long totalTime;
    private long standardDeviation;

    public long getStandardDeviation()
    {
        return standardDeviation;
    }
 
    public void setStandardDeviation(long standardDeviation)
    {
        this.standardDeviation = standardDeviation;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public int getSuccessCount()
    {
        return successCount;
    }

    public void setSuccessCount(int successCount)
    {
        this.successCount = successCount;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public long getFinishTime()
    {
        return finishTime;
    }

    public void setFinishTime(long finishTime)
    {
        this.finishTime = finishTime;
    }

    public long getMaxValue()
    {
        return maxValue;
    }

    public void setMaxValue(long maxValue)
    {
        this.maxValue = maxValue;
    }

    public long getMinValue()
    {
        return minValue;
    }

    public void setMinValue(long minValue)
    {
        this.minValue = minValue;
    }

    public long getTotalValue()
    {
        return totalValue;
    }

    public void setTotalValue(long totalValue)
    {
        this.totalValue = totalValue;
    }

    public long getMedianValue()
    {
        return medianValue;
    }

    public void setMedianValue(long medianValue)
    {
        this.medianValue = medianValue;
    }

    public long getNinetyPercentValue()
    {
        return ninetyPercentValue;
    }

    public void setNinetyPercentValue(long ninetyPercentValue)
    {
        this.ninetyPercentValue = ninetyPercentValue;
    }

    public long getThroughput()
    {
        return throughput;
    }

    public void setThroughput(long throughput)
    {
        this.throughput = throughput;
    }

    public long getAverageValue()
    {
        return averageValue;
    }

    public void setAverageValue(long averageValue)
    {
        this.averageValue = averageValue;
    }

    public long getCount()
    {
        return count;
    }

    public void setCount(long count)
    {
        this.count = count;
    }

    public int getPercentSuccess()
    {
        return percentSuccess;
    }

    public void setPercentSuccess(int percentSuccess)
    {
        this.percentSuccess = percentSuccess;
    }

    public long getTotalTime()
    {
        return totalTime;
    }

    public void setTotalTime(long totalTime)
    {
        this.totalTime = totalTime;
    }
}
