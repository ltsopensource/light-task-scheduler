<div class="row wrapper border-bottom white-bg page-heading" xmlns="http://www.w3.org/1999/html">
    <div class="col-lg-12">
        <h2>TaskTracker监控</h2>
        <ol class="breadcrumb">
            <li><a>监控报警</a></li>
            <li class="active"><b>TaskTracker监控</b></li>
        </ol>
    </div>
</div>

<div class="wrapper wrapper-content animated fadeInRight">
    <div class="row">
        <div class="col-lg-12">
            <div class="ibox">
                <div class="ibox-title">
                    <h3><span class="text-navy">TaskTracker监控数据查询</span></h3>
                </div>
                <div class="ibox-content">
                    <form method="post" id="form" class="form-horizontal" onsubmit="return false">
                        <div class="form-group">

                            <label class="col-sm-1 control-label">节点标识</label>

                            <div class="col-sm-3">
                                <select name="nodeGroup" class="form-control">
                                    <option value="">不限</option>
                                    #foreach($nodeGroup in $taskTrackerNodeGroups)
                                        <option value="$nodeGroup.name">$nodeGroup.name</option>
                                    #end
                                </select>
                            </div>

                            <label class="col-sm-1 control-label">节点标识</label>

                            <div class="col-sm-3">
                                <select name="identity" class="form-control">
                                    <option value="">不限</option>
                                    #foreach($ttMap in $taskTrackerMap.entrySet())
                                        <option value="$ttMap.key">${ttMap.key}(${ttMap.value})</option>
                                    #end
                                </select>
                            </div>

                        </div>
                        <div class="form-group">
                            <label class="col-sm-1 control-label">时间</label>

                            <div class="col-sm-2">
                                <input class="form-control datepicker" type="text" style="width:160px"
                                       id="startTime"
                                       name="startTime"
                                       value="$!startTime"
                                       date-format="yyyy-MM-dd HH:mm:ss"
                                       placeholder="yyyy-MM-dd HH:mm:ss"/>
                            </div>
                            <label class="control-label" style="width: 20px;float: left;">到</label>

                            <div class="col-sm-2">
                                <input class="form-control datepicker" type="text" style="width:160px"
                                       id="endTime"
                                       name="endTime"
                                       value="$!endTime"
                                       date-format="yyyy-MM-dd HH:mm:ss"
                                       placeholder="yyyy-MM-dd HH:mm:ss"/>
                            </div>
                            <div class="col-sm-2">
                                <button class="btn btn-primary" type="button" id="searchBtn">
                                    搜索
                                </button>
                            </div>
                        </div>
                        <div class="hr-line-dashed"></div>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-lg-12">
            <div class="ibox">
                <div class="ibox-title">
                    <h5><span class="text-navy">任务统计</span> / <span class="text-muted" id="idenityLabel">()</span>
                    </h5>

                    <div class="ibox-tools"><a class="collapse-link"><i class="fa fa-chevron-up"></i></a></div>
                </div>
                <div class="ibox-content">
                    <div>
                        <div id="highcharts" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-12">
            <div class="ibox">
                <div class="ibox-title">
                    <h5><span class="text-navy">FailStore大小</span> / <span class="text-muted">单位: KB</span></h5>

                    <div class="ibox-tools"><a class="collapse-link"><i class="fa fa-chevron-up"></i></a></div>
                </div>
                <div class="ibox-content">
                    <div>
                        <div id="failStoreChart" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

</div>

<script>
    var chartConfigs = [
        {"field": "exeSuccessNum", "title": "执行成功个数"},
        {"field": "exeFailedNum", "title": "执行失败个数"},
        {"field": "exeLaterNum", "title": "延迟执行个数"},
        {"field": "exeExceptionNum", "title": "执行异常个数"},
        {"field": "failStoreSize", "title": "FailStore空间大小"}
    ]

    $(document).ready(function () {
        $("#searchBtn").on("click", function () {
            var params = {};
            params.identity = $("select[name='identity']").val();
            params.nodeGroup = $("select[name='nodeGroup']").val();
            var startTime = $("input[name='startTime']").val();
            var endTime = $("input[name='endTime']").val();
            if (!startTime) {
                swal("请选择开始时间", "开始时间不能为空，格式:yyyy-MM-dd HH:mm:ss", "warning");
                return;
            }
            if (!LTS.ReExp.time.test(startTime)) {
                swal("开始时间格式不正确", "格式:yyyy-MM-dd HH:mm:ss", "warning");
                return;
            }
            if (!endTime) {
                swal("请选择结束时间", "结束时间不能为空，格式:yyyy-MM-dd HH:mm:ss", "warning");
                return;
            }
            if (!LTS.ReExp.time.test(endTime)) {
                swal("结束时间格式不正确", "格式:yyyy-MM-dd HH:mm:ss", "warning");
                return;
            }
            params.startTime = DateUtil.parse(startTime).getTime();
            params.endTime = DateUtil.parse(endTime).getTime();
            params.start = 0;
            params.limit = 100000000;
            params.pageIndex = 0;
            params.field = "timestamp";
            params.direction = "ASC";
            params.nodeType = "TASK_TRACKER";

            $.ajax({
                url: 'api/monitor/monitor-data-get.do',
                type: 'GET',
                dataType: 'json',
                data: params,
                success: function (json) {
                    if (json && json.success) {
                        $("#idenityLabel").html("(" + (params.identity ? params.identity : '不限') + ")");
                        showChart(json);
                    } else {
                        json ? swal(json['msg']) : {};
                    }
                }
            });
        });

        $("#searchBtn").trigger("click");

        function showChart(json) {
            if (json.results == 0) {
                $("#highcharts").html("暂无数据，请重新选择条件。");
                $("#failStoreChart").html("暂无数据，请重新选择条件。");
                return;
            }
            $("#highcharts").html("");
            $("#failStoreChart").html("");

            var seriesMap = {};
            for (var i = 0; i < chartConfigs.length; i++) {
                var chartConfig = chartConfigs[i];
                seriesMap[chartConfig['field']] = {
//                    type: 'area',
                    name: chartConfig["title"],
                    data: []
                };
            }

            var rows = json.rows;
            for (var i = 0; i < rows.length; i++) {
                var row = rows[i];
                var timestamp = row['timestamp'];
                for (var seriesKey in seriesMap) {
                    var value = row[seriesKey];
                    if(seriesKey == 'failStoreSize'){
                        value = Math.round(row[seriesKey] / 1024);
                    }
                    seriesMap[seriesKey]['data'].push([timestamp, value]);

                }
            }

            var series1 = [];
            var series2 = [];
            for (var seriesKey in seriesMap) {
                if(seriesKey == 'failStoreSize') {
                    series2.push(seriesMap[seriesKey]);
                }else{
                    series1.push(seriesMap[seriesKey]);
                }
            }

            var colors = ['#FCAF64', '#1bd0dc', '#f9b700', '#eb6100', '#eb6877', '#a98fc2', '#9dd30d', '#1c95bd', '#9999ff', '#5674b9', '#009944'];
            showLineChart("#highcharts", "任务数据统计", "个数", series1, colors);
            showLineChart("#failStoreChart", "FailStore空间大小", "K", series2, colors);
        }


    });
</script>

<script src="assets/js/plugins/highcharts/highcharts-4.1.8.js"></script>