package com.atlassian.bamboo.plugins.jmeter_aggregator;

import com.atlassian.bamboo.plugins.jmeter_aggregator.web.SamplerMetric;

public class SampleResult {
    private Number value;
    private SamplerMetric metric;

    public Number getValue() {
        return value;
    }

    public void setValue(final Number value) {
        this.value = value;
    }

    public SamplerMetric getMetric() {
        return metric;
    }

    public void setMetric(final SamplerMetric metric) {
        this.metric = metric;
    }

    public SampleResult(final SamplerMetric metric, final Number value) {
        super();
        this.value = value;
        this.metric = metric;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((metric == null) ? 0 : metric.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SampleResult other = (SampleResult)obj;
        if (metric == null) {
            if (other.metric != null) {
                return false;
            }
        } else if (!metric.equals(other.metric)) {
            return false;
        }
        return true;
    }

}
