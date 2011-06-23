package com.atlassian.bamboo.plugins.jmeter_aggregator.builder;

import com.atlassian.bamboo.plugins.jmeter_aggregator.web.SamplerMetric;

public class SamplerAssertion {
    public final static String ANY_LABEL = "_ANY";
    public final static String TOTAL_LABEL = "_TOTAL";

    private final long value;
    private final AssertionType assertionType;
    private final SamplerMetric metric;
    private final String label;

    public SamplerAssertion(final long value, final AssertionType assertionType, final SamplerMetric metric, final String label) {
        this.value = value;
        this.assertionType = assertionType;
        this.metric = metric;
        this.label = label;
    }

    public long getValue() {
        return value;
    }

    public AssertionType getAssertionType() {
        return assertionType;
    }

    public SamplerMetric getMetric() {
        return metric;
    }

    public String getLabel() {
        return label;
    }

    public static enum AssertionType {
        GREATER_THAN("greater than"),
        LESS_THAN("less than"),
        EQUALS("equals"),
        NOT_EQUALS("doesn't equal"),
        DEVIATES_BY("deviates by"),
        DEVIATES_BY_PERCENT("deviates by %"),
        DEVIATES_UP_BY("deviates up by"),
        DEVIATES_UP_BY_PERCENT("deviates up by %"),
        DEVIATES_DOWN_BY("deviates down by"),
        DEVIATES_DOWN_BY_PERCENT("deviates down by %");

        private final String description;

        private AssertionType(final String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
