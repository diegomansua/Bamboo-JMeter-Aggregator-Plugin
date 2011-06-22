package com.atlassian.bamboo.plugins.jmeter_aggregator.index;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.jetbrains.annotations.NotNull;

import com.atlassian.bamboo.index.CustomPostBuildIndexWriter;
import com.atlassian.bamboo.plugins.jmeter_aggregator.builder.JmeterBuildDataHelper;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;

public class JmeterResultsWriter implements CustomPostBuildIndexWriter {
    public static final String JMETER_AGGREGATOR_NINETY_PERECENT = "jmeter.aggregator.ninety.percent";
    public static final String JMETER_AGGREGATOR_MEDIAN = "jmeter.aggregator.median";
    public static final String JMETER_AGGREGATOR_THROUGHPUT = "jmeter.aggregator.throughput";

    @Override
    public void updateIndexDocument(@NotNull final Document doc, @NotNull final BuildResultsSummary summary) {
        final Map<String, String> data = summary.getCustomBuildData();
        final String xmlData = data.get(JmeterBuildDataHelper.CUSTOM_BUILD_DATA_MAP);
        if (xmlData != null) {
            final String xmlTotal = data.get(JmeterBuildDataHelper.CUSTOM_BUILD_DATA_TOTAL);

            // Store the total and xmldata unindexed.
            doc.add(new Field(JmeterBuildDataHelper.CUSTOM_BUILD_DATA_MAP, xmlData, Store.COMPRESS, Index.NO));
            doc.add(new Field(JmeterBuildDataHelper.CUSTOM_BUILD_DATA_TOTAL, xmlTotal, Store.COMPRESS,
                    Index.NO));
        }
    }

}
