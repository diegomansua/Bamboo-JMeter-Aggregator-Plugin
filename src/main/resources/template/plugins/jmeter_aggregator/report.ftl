<html>
<head>
    <meta name="tab" content="Load Test Reports"/>
    <meta name="decorator" content="plan"/>
</head>

<body>
    <h1>
        ${build.name}: JMeter Load Test Reports
        [#if build.suspendedFromBuilding]<span class="subGrey">[@ww.text name='build.summary.title.long.suspended'/]</span>[/#if]
    </h1>

    <div class="reportParam">
    [@ww.form action='generateLoadTestReport' submitLabelKey='global.buttons.submit' titleKey='report.input.title' cssClass='bambooForm narrowForm'
              method='get' ]
    	[@ww.hidden name='buildKey']
    	[/@ww.hidden]
        [@ww.select label='Samplers'
                    name='samplers'
                    list='availableSamplers'
                    multiple='true']
        [/@ww.select]
        <br/>
        [@ww.checkbox name='includeTotal' label='Include total' /]
        <br/>
        [@ww.select label='Metrics'
                    name='metrics'
                    list='availableMetrics'
                    multiple='true']
        [/@ww.select]
        <br/>
    [/@ww.form]
    </div>

    [#if chart??]
        <div class="reportDisplay">
            <div class="">
                <h2>JMeter Load Test Report</h2>
                <br/>
                <div class="fullyCentered">
                ${chart.imageMap}
<img id="chart" src="${req.contextPath}/chart?filename=${chart.location}" border="0" height="${chart.height}" width="${chart.width}" usemap="${chart.imageMapName}"/>
                </div>
            </div>              
        </div>
    [/#if]
</body>
</html>
