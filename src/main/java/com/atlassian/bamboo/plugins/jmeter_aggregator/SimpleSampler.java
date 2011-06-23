package com.atlassian.bamboo.plugins.jmeter_aggregator;

/**
 * Simple sampler, for storing sampler results so they can be serialised/deserialised by XStream
 */
public class SimpleSampler implements Sampler {
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

    @Override
    public long getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(final long standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    @Override
    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(final int successCount) {
        this.successCount = successCount;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    @Override
    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(final long finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(final long maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public long getMinValue() {
        return minValue;
    }

    public void setMinValue(final long minValue) {
        this.minValue = minValue;
    }

    @Override
    public long getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(final long totalValue) {
        this.totalValue = totalValue;
    }

    @Override
    public long getMedianValue() {
        return medianValue;
    }

    public void setMedianValue(final long medianValue) {
        this.medianValue = medianValue;
    }

    @Override
    public long getNinetyPercentValue() {
        return ninetyPercentValue;
    }

    public void setNinetyPercentValue(final long ninetyPercentValue) {
        this.ninetyPercentValue = ninetyPercentValue;
    }

    @Override
    public long getThroughput() {
        return throughput;
    }

    public void setThroughput(final long throughput) {
        this.throughput = throughput;
    }

    @Override
    public long getAverageValue() {
        return averageValue;
    }

    public void setAverageValue(final long averageValue) {
        this.averageValue = averageValue;
    }

    @Override
    public long getCount() {
        return count;
    }

    public void setCount(final long count) {
        this.count = count;
    }

    @Override
    public int getPercentSuccess() {
        return percentSuccess;
    }

    public void setPercentSuccess(final int percentSuccess) {
        this.percentSuccess = percentSuccess;
    }

    @Override
    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(final long totalTime) {
        this.totalTime = totalTime;
    }
}
