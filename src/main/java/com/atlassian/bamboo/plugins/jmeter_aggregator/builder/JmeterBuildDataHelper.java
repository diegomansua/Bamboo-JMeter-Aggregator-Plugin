package com.atlassian.bamboo.plugins.jmeter_aggregator.builder;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.bamboo.plugins.jmeter_aggregator.Sampler;
import com.thoughtworks.xstream.XStream;

public class JmeterBuildDataHelper {
    public static final String CUSTOM_BUILD_DATA_MAP = "jmeter.aggregator.map";
    public static final String CUSTOM_BUILD_DATA_TOTAL = "jmeter.aggregator.total";

    public static boolean isJmeterAggregatorOn(final Map<String, String> customConfiguration) {
        return Boolean.valueOf(customConfiguration.get("custom.bamboo.jmeter_aggregation.on"));
    }

    public static void putSamplerDataInCustomData(final Map<String, String> customData, final Map<String, Sampler> samplerMap,
            final Sampler totalSampler) {
        final XStream xstream = new XStream();
        final String samplerMapXml = xstream.toXML(samplerMap);
        final String totalSamplerXml = xstream.toXML(totalSampler);

        customData.put(JmeterBuildDataHelper.CUSTOM_BUILD_DATA_MAP, samplerMapXml);
        customData.put(JmeterBuildDataHelper.CUSTOM_BUILD_DATA_TOTAL, totalSamplerXml);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Sampler> getSamplerMapFromCustomData(final Map<String, String> customData) {
        final XStream xstream = new XStream();
        final String samplerMapXml = customData.get(CUSTOM_BUILD_DATA_MAP);
        if (samplerMapXml != null) {
            try {
                return (Map<String, Sampler>)xstream.fromXML(samplerMapXml);
            } catch (final Exception e) {
                // This is to catch data corruptions. Ignore, and just return an empty map.
                return new HashMap<String, Sampler>();
            }
        } else {
            return new HashMap<String, Sampler>();
        }
    }

    public static Sampler getTotalSamplerFromCustomData(final Map<String, String> customData) {
        final XStream xstream = new XStream();
        final String totalSamplerXml = customData.get(CUSTOM_BUILD_DATA_TOTAL);
        if (totalSamplerXml != null) {
            try {
                return (Sampler)xstream.fromXML(totalSamplerXml);
            } catch (final Exception e) {
                // This is to catch data corruptions. Ignore, and just return an empty map.
                return null;
            }
        } else {
            return null;
        }
    }

}
