[#if samplerassertions.size() > 0]
    [@ui.bambooInfoDisplay titleKey='JMeter Aggregator Assertions' float=false height='80px']
        <ul>
        [#list samplerassertions as sa]
            <li>Sampler ([#if sa.label == "_TOTAL"]Total of all Samplers[#elseif sa.label == "_ANY"]Any Samplers[#else]${sa.label}[/#if])
                metric (${sa.metric.description})
                assertion (${sa.assertionType.description})
                value (${sa.value})</li>
        [/#list]
        </ul>
    [/@ui.bambooInfoDisplay]
[/#if]