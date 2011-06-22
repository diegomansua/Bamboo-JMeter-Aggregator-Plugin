package com.atlassian.bamboo.plugins.jmeter_aggregator.web;

import java.util.Map;

import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plugins.jmeter_aggregator.builder.JmeterBuildDataHelper;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

public class BambooLoadTestReportCondition implements Condition {
    private PlanManager planManager;

    /**
     * Called after creation and autowiring.
     * 
     * @param params The optional map of parameters specified in XML.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void init(final Map params) throws PluginParseException {
        // Do nothing
    }

    /**
     * Determine whether the web fragment should be displayed
     * 
     * @return true if the user should see the fragment, false otherwise
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean shouldDisplay(final Map context) {
        final String buildKey = (String)context.get("buildKey");
        if (buildKey != null) {
            return JmeterBuildDataHelper.isJmeterAggregatorOn(planManager.getPlanByKey(
                    buildKey).getBuildDefinition().getCustomConfiguration());
        }
        return false;
    }

    public void setPlanManager(final PlanManager planManager) {
        this.planManager = planManager;
    }
}
