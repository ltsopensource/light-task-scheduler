<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML>
<html>
<head>
    <jsp:include page="head.jsp"/>
</head>
<body>
<div class="container">
    <div class="row">
        <form id="searchForm" class="form-horizontal span24">
            <div class="row">
                <div class="control-group span8">
                    <label class="control-label">执行节点组：</label>

                    <div class="controls">
                        <select name="taskTrackerNodeGroup">
                            <option value="">不限</option>
                            <c:forEach items="${taskTrackerMap}" var="map">
                                <option value="${map.key}">${map.key}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">执行节点标识：</label>

                    <div class="controls">
                        <select name="taskTrackerIdentity">
                            <option value="">不限</option>
                            <c:forEach items="${taskTrackerMap}" var="map">
                                <c:forEach items="${map.value}" var="identity">
                                    <option value="${identity}">${identity}(${map.key})</option>
                                </c:forEach>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="control-group span18">
                    <label class="control-label">时间：</label>

                    <div class="controls">
                        <input type="text" class="calendar" name="startTime"
                               value="${startTime}" data-rules="{required : true}"><span> -
                        </span><input name="endTime" value="${endTime}" type="text" data-rules="{required : true}"
                                      class="calendar">
                    </div>
                </div>
                <div class="span3 offset2">
                    <button type="button" id="btnSearch" class="button button-primary">搜索</button>
                </div>
            </div>
        </form>
    </div>
    <div class="search-grid-container">
        <div id="successNumContainer" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
        <div id="totalFreeMemoryContainer" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
        <div id="failedNumContainer" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
        <div id="failStoreSizeContainer" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
    </div>
</div>
<script type="text/javascript" src="../assets/js/highcharts-4.1.8.js"></script>

<script type="text/javascript">
    BUI.use('common/page');
</script>
<script type="text/javascript">
    BUI.use(['bui/common', 'bui/form', 'bui/data', 'common/date-util'], buiInit);

    function buiInit(BUI, Form, Data, DateUtil) {

        // initForm
        var form = new Form.HForm({
            srcNode: '#searchForm'
        }).render();

        $('#btnSearch').on('click', function (ev) {
            ev.preventDefault();
            form.valid();
            if (form.isValid()) {
                var param = form.serializeToObject();
                param.startTime = DateUtil.parse(param.startTime + " 00:00:00").getTime(); // 转化成秒
                param.endTime = DateUtil.parse(param.endTime + " 23:59:59").getTime();
                param.start = 0;
                param.limit = 100000000;
                param.pageIndex = 0;
                param.field = "timestamp";
                param.direction = "ASC";
                console.log(param);
                $.ajax({
                    url: '/api/monitor/tasktracker-mi-get.do',
                    type: 'GET',
                    dataType: 'json',
                    data: param,
                    success: function (json) {
                        if (json && json.success) {
                            renderChart(json);
                        } else {
                            BUI.Message.Alert(json.msg);
                        }
                    }
                });
            }
        });

        $('#btnSearch').trigger("click");
    }

    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });

    function renderChart(json) {
        console.log(json);
        var config = {
            "successNum":{
                containerId:'successNumContainer',
                title:'执行成功任务数',
                yTitle:'任务数'
            },
            "failedNum":{
                containerId:'failedNumContainer',
                title:'执行失败任务数',
                yTitle:'任务数'
            },
            "totalFreeMemory":{
                containerId:'totalFreeMemoryContainer',
                title:'空闲内存',
                yTitle:'内存'
            },
            "failStoreSize":{
                containerId:'failStoreSizeContainer',
                title:'FailStore占用空间大小',
                yTitle:'空间'
            }
        };

        $.each(config, function(key, v){
            var rows = json.rows;
            var chartData = [];
            $.each(rows, function (index, row) {
                var item = [];
                item.push(row['timestamp']);
                item.push(row[key]);
                chartData.push(item);
            });
            showChart(v.containerId, v.title, v.yTitle, chartData);

        });
    }




    function showChart(containerId, title, yTitle, chartData) {
        $('#'+containerId).highcharts({
            chart: {
                zoomType: 'x'
            },
            title: {
                text: title
            },
            xAxis: {
                type: 'datetime'
            },
            yAxis: {
                title: {
                    text: yTitle
                }
            },
            legend: {
                enabled: false
            },
            plotOptions: {
                area: {
                    fillColor: {
                        linearGradient: {
                            x1: 0,
                            y1: 0,
                            x2: 0,
                            y2: 1
                        },
                        stops: [
                            [0, Highcharts.getOptions().colors[0]],
                            [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                        ]
                    },
                    marker: {
                        radius: 2
                    },
                    lineWidth: 1,
                    states: {
                        hover: {
                            lineWidth: 1
                        }
                    },
                    threshold: null
                }
            },
            credits: {
                enabled: false
            },
            series: [{
                type: 'area',
                name: title,
                data: chartData
            }]
        });
    }

</script>
</body>
</html>
