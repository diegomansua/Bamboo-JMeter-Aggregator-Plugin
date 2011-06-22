package com.atlassian.bamboo.plugins.jmeter_aggregator;

public interface Sampler
{
    String getLabel();

    int getSuccessCount();

    long getStartTime();

    long getFinishTime();

    long getMaxValue();

    long getMinValue();

    long getTotalValue();

    long getMedianValue();

    long getNinetyPercentValue();

    long getThroughput();

    long getAverageValue();

    long getCount();

    int getPercentSuccess();

    long getTotalTime();

    long getStandardDeviation();    
}
