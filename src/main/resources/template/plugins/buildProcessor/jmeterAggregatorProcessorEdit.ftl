<script type="text/javascript">
var jma = function() {
    // Private functions
    function getJmaElement(name, id)
    {
        if (id == null)
        {
            return document.getElementById("jma_sa_" + name);
        }
        else
        {
            return document.getElementById("jma_sa_" + name + "_" + id);
        }
    }
return {
    // Public functions
    addAssertion : function()
    {
        var table = getJmaElement("table", null);
        var newRow = getJmaElement("new_row", null);
        var newId = table.rows.length - 2;
        var tr = table.insertRow(-1);
        tr.id = "jma_sa_row_" + newId;
        // Would love to use innerHTML here, but IE TR's are read only for innerHTML
        for (var i = 0; i < newRow.cells.length; i++)
        {
            var td = tr.insertCell(-1);
            td.innerHTML = newRow.cells[i].innerHTML.replace(/__jma_id__/g, newId);
        }

        var countInput = getJmaElement("count", null);
        var count = parseInt(countInput.value);
        count++;
        countInput.value = count;
    },
    deleteAssertion : function(id)
    {
        // Rather than deleting the row, the easiest thing to do is to move all the data up one row
        // and delete the last one
        var countInput = getJmaElement("count", null);
        var count = parseInt(countInput.value);
        count--;
        countInput.value = count;
        var i;
        for (i = id; i < count; i++)
        {
            var newId = i + 1;
            getJmaElement("label_select", i).value = getJmaElement("label_select", newId).value;
            getJmaElement("label", i).value = getJmaElement("label", newId).value;
            getJmaElement("label", i).style.visibility = getJmaElement("label", newId).style.visibility;
            getJmaElement("metric", i).value = getJmaElement("metric", newId).value;
            getJmaElement("assert", i).value = getJmaElement("assert", newId).value;
            getJmaElement("value", i).value = getJmaElement("value", newId).value;
        }
        var lastRow = getJmaElement("row", i);
        var tbody = getJmaElement("tbody", null);
        tbody.removeChild(lastRow);
    },
    selectLabel : function(id)
    {
        var value = getJmaElement("label_select", id).value;
        var input = getJmaElement("label", id);
        if (value == "_OTHER")
        {
            input.value = "";
            input.style.visibility = "visible";
        }
        else
        {
            input.style.visibility = "hidden";
            input.value = value;
        }
    }
};}();

</script>

[@ui.bambooSection title='JMeter Result Aggregation' ]
    [@ww.checkbox name='custom.bamboo.jmeter_aggregation.on' label='Aggregate JMeter Results'
        description='Check to turn JMeter Result Aggregation on' toggle='true' /]
    [@ui.bambooSection dependsOn='custom.bamboo.jmeter_aggregation.on' showOn='true']
        [@ww.textfield name='custom.bamboo.jmeter_aggregation.buildLogFile' label='Build Log File'
            description='Comma separated list of JTL log files.  You can also use ant style patterns such as **/*.jtl' /]
        [@ww.textfield name='custom.bamboo.jmeter_aggregation.csvLogFile' label='CSV Log File'
            description='(Optional) CSV files contain samples of arbitary data at points in time, one point in time per row, one sampler per column.' /]
        [@ww.checkbox name='custom.bamboo.jmeter_aggregation.csvCustomHeader' label='Use a Custom Header'
            description='Check if the CSV file does not have its own header' toggle='true' /]
        [@ui.bambooSection dependsOn='custom.bamboo.jmeter_aggregation.csvCustomHeader' showOn='true']
            [@ww.textfield name='custom.bamboo.jmeter_aggregation.csvHeader' label='CSV Custom Header'
                description='A custom header for the CSV file' /]
        [/@ui.bambooSection]
    [/@ui.bambooSection]
    [@ui.bambooSection dependsOn='custom.bamboo.jmeter_aggregation.on' showOn='true' title="JMeter Result Assertions"]
    <input id="jma_sa_count" type="hidden" name="custom.bamboo.jmeter_aggregation.assertionCount" value="${samplerassertions.size()}"/>
    <div class="fieldArea">
    <div class="fieldValueArea">
        <p>
            <a href="javascript:jma.addAssertion()">Add Assertion</a>
        </p>
        <table class="grid" id="jma_sa_table"">
            <thead>
            <tr>
                <th>Sampler Label</th>
                <th>Metric</th>
                <th>Assertion</th>
                <th>Value</th>
                <th>&nbsp;</th>
            </tr>
            </thead>
            <tbody id="jma_sa_tbody">
            <tr id="jma_sa_new_row" style="display:none">
                <td>
                    <select id="jma_sa_label_select___jma_id__" onchange="javascript:jma.selectLabel(__jma_id__)">
                    [#list samplervalues.keySet() as sv]
                        <option value="${sv}">${samplervalues[sv]}</option>
                    [/#list]
                        <option value="_OTHER">Other Sampler:</option>
                    </select>
                    <input id="jma_sa_label___jma_id__" type="text" value="_ANY"
                        name="custom.bamboo.jmeter_aggregation.assertion__jma_id__.label"
                        style="visibility:hidden"/>
                 </td>
                 <td>
                    <select id="jma_sa_metric___jma_id__" name="custom.bamboo.jmeter_aggregation.assertion__jma_id__.metric">
                    [#list metricvalues as mv]
                        <option value="${mv.name()}">${mv.description}</option>
                    [/#list]
                    </select>
                 </td>
                 <td>
                    <select id="jma_sa_assert___jma_id__" name="custom.bamboo.jmeter_aggregation.assertion__jma_id__.assert">
                    [#list assertionvalues as av]
                        <option value="${av.name()}">${av.description}</option>
                    [/#list]
                    </select>
                 </td>
                 <td>
                     <input id="jma_sa_value___jma_id__" type="text" value="" name="custom.bamboo.jmeter_aggregation.assertion__jma_id__.value"/>
                 </td>
                 <td>
                     <a href="javascript:jma.deleteAssertion(__jma_id__)">Delete</a>
                 </td>
            </tr>
        [#list samplerassertions as sa]
            [#if fieldErrors["custom.bamboo.jmeter_aggregation.assertion${sa_index}.errors"]??]
                [#list fieldErrors["custom.bamboo.jmeter_aggregation.assertion${sa_index}.errors"] as error]
                    <tr><td colspan="5"><span style="color:#CC0000; font-weight:bold;">${error}</span></td></tr>
                [/#list]
            [/#if]
            <tr id="jma_sa_row_${sa_index}">
                <td>
                    <select id="jma_sa_label_select_${sa_index}" onchange="javascript:jma.selectLabel(${sa_index})">
                    [#list samplervalues.keySet() as sv]
                        <option value="${sv}" [#if sv == sa.label]selected="selected"[/#if]>${samplervalues[sv]}</option>
                    [/#list]
                        <option value="_OTHER" [#if !samplervalues.containsKey(sa.label)]selected="selected"[/#if]>Other Sampler:</option>
                    </select>
                    <input id="jma_sa_label_${sa_index}" type="text" value="${sa.label}"
                        name="custom.bamboo.jmeter_aggregation.assertion${sa_index}.label"
                        [#if samplervalues.containsKey(sa.label)] style="visibility:hidden"[/#if]/>
                 </td>
                 <td>
                    <select id="jma_sa_metric_${sa_index}" name="custom.bamboo.jmeter_aggregation.assertion${sa_index}.metric">
                    [#list metricvalues as mv]
                        [#if mv == sa.metric]
                            <option value="${mv.name()}" selected="selected">${mv.description}</option>
                        [#else]
                            <option value="${mv.name()}">${mv.description}</option>
                        [/#if]
                    [/#list]
                    </select>
                 </td>
                 <td>
                    <select id="jma_sa_assert_${sa_index}" name="custom.bamboo.jmeter_aggregation.assertion${sa_index}.assert">
                    [#list assertionvalues as av]
                        [#if av == sa.assertionType]
                            <option value="${av.name()}" selected="selected">${av.description}</option>
                        [#else]
                            <option value="${av.name()}">${av.description}</option>
                        [/#if]
                    [/#list]
                    </select>
                 </td>
                 <td>
                     <input id="jma_sa_value_${sa_index}" type="text" value="${stack.findValue("custom.bamboo.jmeter_aggregation.assertion${sa_index}.value")}"
                        name="custom.bamboo.jmeter_aggregation.assertion${sa_index}.value"/>
                 </td>
                 <td>
                     <a href="javascript:jma.deleteAssertion(${sa_index})">Delete</a>
                 </td>
            </tr>
        [/#list]
            </tbody>
        </table>
        </div></div>
    [/@ui.bambooSection]
[/@ui.bambooSection ]
