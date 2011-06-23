package com.atlassian.bamboo.plugins.jmeter_aggregator.builder;

import com.atlassian.bamboo.plugins.jmeter_aggregator.web.SamplerMetric;

public class SamplerSeries {

    private String label;

    private SamplerMetric metric;

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public SamplerMetric getMetric() {
        return metric;
    }

    public void setMetric(final SamplerMetric metric) {
        this.metric = metric;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((metric == null) ? 0 : metric.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
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
        final SamplerSeries other = (SamplerSeries)obj;
        if (metric == null) {
            if (other.metric != null) {
                return false;
            }
        } else if (!metric.equals(other.metric)) {
            return false;
        }
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        } else if (!label.equals(other.label)) {
            return false;
        }
        return true;
    }

    public SamplerSeries(final String label, final SamplerMetric metric) {
        super();
        this.label = label;
        this.metric = metric;
    }

}
