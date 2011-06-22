package com.atlassian.bamboo.plugins.jmeter_aggregator.index;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.atlassian.bamboo.index.CustomIndexReader;
import com.atlassian.bamboo.plugins.jmeter_aggregator.builder.JmeterBuildDataHelper;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;

public class JmeterResultsReader implements CustomIndexReader
{

    public void extractFromDocument(Document doc, BuildResultsSummary summary)
    {
        Map<String, String> results = summary.getCustomBuildData();
        Field dataField = doc.getField(JmeterBuildDataHelper.CUSTOM_BUILD_DATA_MAP);
        if (dataField != null)
        {
            results.put(JmeterBuildDataHelper.CUSTOM_BUILD_DATA_MAP, dataField.stringValue());
        }
        Field totalField = doc.getField(JmeterBuildDataHelper.CUSTOM_BUILD_DATA_TOTAL);
        if (totalField != null)
        {
            results.put(JmeterBuildDataHelper.CUSTOM_BUILD_DATA_TOTAL, totalField.stringValue());
        }
    }

}
