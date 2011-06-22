package com.atlassian.bamboo.plugins.jmeter_aggregator.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.atlassian.bamboo.build.CustomBuildProcessorServer;
import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plugins.jmeter_aggregator.Sampler;
import com.atlassian.bamboo.plugins.jmeter_aggregator.web.SamplerMetric;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.v2.build.BaseConfigurablePlugin;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;

/**
 * This runs after the build processor has collected and stored the results. This will run on the server, so that it
 * can look up results from previous builds in order to run deviation assertions.
 * <p/>
 * This class also handles all the configuration
 */
public class JmeterAggregatorBuildProcessorServer extends BaseConfigurablePlugin implements CustomBuildProcessorServer {
    private static final Logger log = Logger.getLogger(JmeterAggregatorBuildProcessorServer.class);
    private BuildContext ctx;
    private boolean on;
    private Sampler previousTotalSampler;
    private Map<String, Sampler> previousSamplerMap;
    private ResultsSummary previousBuildResultsSummary;

    private PlanManager planManager;

    @Override
    public void init(@NotNull final BuildContext ctx) {
        this.ctx = ctx;
        final Map<String, String> conf = ctx.getBuildDefinition().getCustomConfiguration();
        on = JmeterBuildDataHelper.isJmeterAggregatorOn(conf);
    }

    @Override
    @NotNull
    public BuildContext call() {
        if (!on) {
            return ctx;
        }

        // Get the data
        final Map<String, Sampler> samplerMap = getSamplerMapFromCustomData();
        final Sampler totalSampler = getTotalSamplerFromCustomData();

        // Run assertions
        final Collection<SamplerAssertion> assertions = getSamplerAssertions(
                ctx.getBuildDefinition().getCustomConfiguration());
        for (final SamplerAssertion assertion : assertions) {
            final String label = assertion.getLabel();
            if (SamplerAssertion.TOTAL_LABEL.equals(label)) {
                runAssertion(totalSampler, assertion);
            } else if (SamplerAssertion.ANY_LABEL.equals(label)) {
                for (final Sampler sampler : samplerMap.values()) {
                    runAssertion(sampler, assertion);
                }
                runAssertion(totalSampler, assertion);
            } else {
                runAssertion(samplerMap.get(label), assertion);
            }
        }
        return ctx;
    }

    // /CLOVER:OFF
    protected Sampler getTotalSamplerFromCustomData() {
        return JmeterBuildDataHelper.getTotalSamplerFromCustomData(
                ctx.getBuildResult().getCustomBuildData());
    }

    protected Map<String, Sampler> getSamplerMapFromCustomData() {
        return JmeterBuildDataHelper.getSamplerMapFromCustomData(
                ctx.getBuildResult().getCustomBuildData());
    }

    // /CLOVER:ON

    private void runAssertion(final Sampler sampler, final SamplerAssertion assertion) {
        final long value = getValueFromPreviousSampler(assertion, sampler);
        final CurrentBuildResult buildResult = ctx.getBuildResult();
        final List<String> errors = new ArrayList<String>();

        switch (assertion.getAssertionType()) {
            case EQUALS:
                if (value == assertion.getValue()) {
                    errors.add(buildErrorMessage(assertion, sampler, value));
                }
                break;
            case NOT_EQUALS:
                if (value != assertion.getValue()) {
                    errors.add(buildErrorMessage(assertion, sampler, value));
                }
                break;
            case GREATER_THAN:
                if (value > assertion.getValue()) {
                    errors.add(buildErrorMessage(assertion, sampler, value));
                }
                break;
            case LESS_THAN:
                if (value < assertion.getValue()) {
                    errors.add(buildErrorMessage(assertion, sampler, value));
                }
                break;
            case DEVIATES_BY:
                Sampler previousSampler = getPreviousSampler(sampler);
                if (previousSampler != null) {
                    final long previousValue = getValueFromPreviousSampler(assertion, previousSampler);
                    if (Math.abs(value - previousValue) > assertion.getValue()) {
                        errors.add(buildErrorMessage(assertion, sampler, value));
                    }
                }
                break;
            case DEVIATES_UP_BY:
                previousSampler = getPreviousSampler(sampler);
                if (previousSampler != null) {
                    final long previousValue = getValueFromPreviousSampler(assertion, previousSampler);
                    if (value - previousValue > assertion.getValue()) {
                        errors.add(buildErrorMessage(assertion, sampler, value));
                    }
                }
                break;
            case DEVIATES_DOWN_BY:
                previousSampler = getPreviousSampler(sampler);
                if (previousSampler != null) {
                    final long previousValue = getValueFromPreviousSampler(assertion, previousSampler);
                    if (previousValue - value > assertion.getValue()) {
                        errors.add(buildErrorMessage(assertion, sampler, value));
                    }
                }
                break;
            case DEVIATES_BY_PERCENT:
                previousSampler = getPreviousSampler(sampler);
                if (previousSampler != null) {
                    final long previousValue = getValueFromPreviousSampler(assertion, previousSampler);
                    if (previousValue != 0) {
                        if (Math.abs(value - previousValue) * 100 / previousValue > assertion.getValue()) {
                            errors.add(buildErrorMessage(assertion, sampler, value));
                        }
                    }
                }
                break;
            case DEVIATES_UP_BY_PERCENT:
                previousSampler = getPreviousSampler(sampler);
                if (previousSampler != null) {
                    final long previousValue = getValueFromPreviousSampler(assertion, previousSampler);
                    if (previousValue != 0) {
                        if ((value - previousValue) * 100 / previousValue > assertion.getValue()) {
                            errors.add(buildErrorMessage(assertion, sampler, value));
                        }
                    }
                }
                break;
            case DEVIATES_DOWN_BY_PERCENT:
                previousSampler = getPreviousSampler(sampler);
                if (previousSampler != null) {
                    final long previousValue = getValueFromPreviousSampler(assertion, previousSampler);
                    if (previousValue != 0) {
                        if ((previousValue - value) * 100 / previousValue > assertion.getValue()) {
                            errors.add(buildErrorMessage(assertion, sampler, value));
                        }
                    }
                }
                break;
        }
        if (errors.size() > 0) {
            buildResult.setBuildState(BuildState.FAILED);
            buildResult.addBuildErrors(errors);
        }
    }

    protected long getValueFromPreviousSampler(final SamplerAssertion assertion,
            final Sampler previousSampler) {
        return assertion.getMetric().getValueFromSampler(previousSampler).longValue();
    }

    private String buildErrorMessage(final SamplerAssertion assertion, final Sampler sampler, final long value) {
        String label = sampler.getLabel();
        if (assertion.getLabel().equals(SamplerAssertion.TOTAL_LABEL)) {
            label = "Total of all Samplers";
        }
        return "Load test assertion failed: Sampler (" + label + ") metric (" +
                assertion.getMetric().getDescription() + ") assertion (" + assertion.getAssertionType().getDescription() +
                ") value (" + assertion.getValue() + "). Actual value was " + value + ".";

    }

    private Sampler getPreviousSampler(final Sampler sampler) {
        if (sampler.getLabel() == null) {
            if (previousTotalSampler == null) {
                final ResultsSummary summary = getPreviousBuildResultsSummary();
                if (summary != null) {
                    previousTotalSampler = getTotalSamplerFromCustomData(summary);
                }
            }
            return previousTotalSampler;
        } else {
            if (previousSamplerMap == null) {
                final ResultsSummary summary = getPreviousBuildResultsSummary();
                if (summary != null) {
                    previousSamplerMap = getSamplerMapFromCustomData(summary);
                }
            }
            if (previousSamplerMap != null) {
                return previousSamplerMap.get(sampler.getLabel());
            }
        }
        return null;
    }

    // /CLOVER:OFF
    protected Sampler getTotalSamplerFromCustomData(final ResultsSummary summary) {
        return JmeterBuildDataHelper.getTotalSamplerFromCustomData(
                summary.getCustomBuildData());
    }

    protected Map<String, Sampler> getSamplerMapFromCustomData(
            final ResultsSummary summary) {
        return JmeterBuildDataHelper.getSamplerMapFromCustomData(
                summary.getCustomBuildData());
    }

    // /CLOVER:ON

    private ResultsSummary getPreviousBuildResultsSummary() {
        if (previousBuildResultsSummary == null) {
            final Plan plan = planManager.getPlanByKey(ctx.getPlanKey());
            if (plan != null) {
                previousBuildResultsSummary = plan.getLatestResultsSummary();
            }
        }
        return previousBuildResultsSummary;
    }

    @Override
    protected void populateContextForView(@NotNull final Map<String, Object> stringObjectMap, @NotNull final Plan plan) {
        final Collection<SamplerAssertion> samplerAssertions = getSamplerAssertions(
                plan.getBuildDefinition().getCustomConfiguration());
        stringObjectMap.put("samplerassertions", samplerAssertions);
    }

    @Override
    @NotNull
    public ErrorCollection validate(@NotNull final BuildConfiguration buildConfiguration) {
        final ErrorCollection errors = superClassValidate(buildConfiguration);

        final boolean on = buildConfiguration.getBoolean("custom.bamboo.jmeter_aggregation.on");
        if (on) {
            int assertionCount = 0;
            try {
                final String acs = buildConfiguration.getString("custom.bamboo.jmeter_aggregation.assertionCount");
                if (acs != null) {
                    assertionCount = Integer.parseInt(acs);
                }
            } catch (final NumberFormatException nfe) {
                log.warn("Error parsing assertion count, no assertions will be loaded");
            }
            for (int i = 0; i < assertionCount; i++) {
                final String keyPrefix = "custom.bamboo.jmeter_aggregation.assertion" + i + ".";
                final String valueString = buildConfiguration.getString(keyPrefix + "value");
                if ((valueString == null) || (valueString.length() == 0)) {
                    errors.addError(keyPrefix + "errors", "Value must not be blank");
                } else {
                    try {
                        Long.parseLong(valueString);
                    } catch (final NumberFormatException nfe) {
                        errors.addError(keyPrefix + "errors", "Value must be a number");
                    }
                }
                final String label = buildConfiguration.getString(keyPrefix + "label");
                if ((label == null) || (label.length() == 0)) {
                    errors.addError(keyPrefix + "errors", "Label must not be blank");
                }
            }

            final String buildLogFile = buildConfiguration.getString("custom.bamboo.jmeter_aggregation.buildLogFile");
            if ((buildLogFile == null) || (buildLogFile.length() == 0)) {
                errors.addError("custom.bamboo.jmeter_aggregation.buildLogFile", "Build Log file must not be blank");
            }
        }
        return errors;
    }

    protected ErrorCollection superClassValidate(
            final BuildConfiguration buildConfiguration) {
        return super.validate(buildConfiguration);
    }

    @Override
    protected void populateContextForEdit(@NotNull final Map<String, Object> ctx,
            @NotNull final BuildConfiguration buildConfiguration, @NotNull final Plan plan) {
        super.populateContextForEdit(ctx, buildConfiguration, plan);
        // If we can find a previous build with sampler data, we will put the list of samplers in the map so that the
        // the user can pick from a drop down list, otherwise, they'll use a simple text box
        final ResultsSummary summary = plan.getLatestResultsSummary();
        final Map<String, String> samplerValues = new HashMap<String, String>();
        samplerValues.put(SamplerAssertion.ANY_LABEL, "Any Sampler");
        samplerValues.put(SamplerAssertion.TOTAL_LABEL, "Total of all Samplers");
        if (summary != null) {
            final Map<String, Sampler> samplerMap = getSamplerMapFromCustomData(summary);
            for (final String sampler : samplerMap.keySet()) {
                samplerValues.put(sampler, sampler);
            }
        }

        ctx.put("samplervalues", samplerValues);
        ctx.put("assertionvalues", SamplerAssertion.AssertionType.values());
        ctx.put("metricvalues", SamplerMetric.values());

        // Get the current sampler assertions
        ctx.put("samplerassertions", getSamplerAssertions(buildConfiguration));

        ctx.put("blah", "foo");
    }

    private Collection<SamplerAssertion> getSamplerAssertions(final BuildConfiguration buildConfiguration) {
        final Map<String, String> customConfig = new HashMap<String, String>()
        {
            private static final long serialVersionUID = -6478992489030252059L;

            @Override
            public String get(final Object key)
            {
                return buildConfiguration.getString((String)key);
            }
        };
        // When getting assertions for when we run them, we only have access to the custom build data
        // map, but here we must get it from the buildConfiguration, so that if validation fails, the
        // values are still the same. We don't want too different methods to parse the data, so we
        // just wrap buildConfiguration in a Map
        return getSamplerAssertions(customConfig);
    }

    private Collection<SamplerAssertion> getSamplerAssertions(final Map<String, String> configuration) {
        final List<SamplerAssertion> assertions = new ArrayList<SamplerAssertion>();
        int assertionCount = 0;
        try {
            final String acs = configuration.get("custom.bamboo.jmeter_aggregation.assertionCount");
            if (acs != null) {
                assertionCount = Integer.parseInt(acs);
            }
        } catch (final NumberFormatException nfe) {
            log.warn("Error parsing assertion count");
        }
        for (int i = 0; i < assertionCount; i++) {
            final String keyPrefix = "custom.bamboo.jmeter_aggregation.assertion" + i + ".";
            final String label = configuration.get(keyPrefix + "label");
            final String valueString = configuration.get(keyPrefix + "value");
            final String metricString = configuration.get(keyPrefix + "metric");
            final String assertionString = configuration.get(keyPrefix + "assert");
            if ((label == null) || (valueString == null) || (metricString == null) || (assertionString == null)) {
                log.error("Null values for assertion " + i);
                continue;
            }
            long value = 0;
            try {
                value = Long.parseLong(valueString);
            } catch (final NumberFormatException nfe) {
                log.error("Non numeric value for assertion " + i);
            }
            SamplerMetric metric;
            try {
                metric = getSamplerMetric(metricString);
            } catch (final IllegalArgumentException iae) {
                log.error("Invalid metric " + metricString + " for assertion " + i);
                continue;
            }
            SamplerAssertion.AssertionType assertionType;
            try {
                assertionType = SamplerAssertion.AssertionType.valueOf(assertionString);
            } catch (final IllegalArgumentException iae) {
                log.error("Invalid assertion " + assertionString + " for assertion " + i);
                continue;
            }
            final SamplerAssertion samplerAssertion = new SamplerAssertion(value, assertionType, metric, label);
            assertions.add(samplerAssertion);

        }
        return assertions;
    }

    protected SamplerMetric getSamplerMetric(final String metricString) {
        return SamplerMetric.valueOf(metricString);
    }

    public void setPlanManager(final PlanManager planManager) {
        this.planManager = planManager;
    }
}
