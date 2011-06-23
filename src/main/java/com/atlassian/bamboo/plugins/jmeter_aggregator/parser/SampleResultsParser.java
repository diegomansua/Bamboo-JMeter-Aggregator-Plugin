package com.atlassian.bamboo.plugins.jmeter_aggregator.parser;

import java.util.Map;

import com.atlassian.bamboo.plugins.jmeter_aggregator.Sampler;
import com.atlassian.bamboo.plugins.jmeter_aggregator.SamplerImpl;

/**
 * Interface that all parsers must implement.
 */
public interface SampleResultsParser {
    /**
     * Get the all the samplers, as a map of sampler label to sampler.
     * 
     * @return The map of samplers
     */
    Map<String, Sampler> getSamplerMap();

    /**
     * Get the total sampler
     * 
     * @return The total sampler.
     */
    SamplerImpl getTotalSampler();
}
