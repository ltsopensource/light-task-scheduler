<div class="row wrapper border-bottom white-bg page-heading">
    <div class="col-lg-12">
        <h2>执行中的任务</h2>
        <ol class="breadcrumb">
            <li><a>任务队列管理</a></li>
            <li class="active"><b>执行中的任务</b></li>
        </ol>
    </div>
</div>

<div class="wrapper wrapper-content animated fadeInRight">
    <div class="row">
        <div class="col-lg-12">
            <div class="ibox">
                <div class="ibox-title">
                    <h3><span class="text-navy">执行中的任务</span></h3>
                </div>
                <div class="ibox-content">
                    <form method="post" id="form" class="form-horizontal" onsubmit="return false">
                        <div class="form-group">
                            <label class="col-sm-2 control-label" style="width:10%">任务ID</label>

                            <div class="col-sm-3">
                                <input type="text" class="form-control" name="taskId"
                                       placeholder="请输入任务ID(TaskId)">
                            </div>

                            <label class="col-sm-2 control-label" style="width:10%">提交节点组</label>

                            <div class="col-sm-3">
                                <select name="submitNodeGroup" class="form-control">
                                    <option value="">-- 不限 --</option>
                                    #foreach($nodeGroup in $jobClientNodeGroups)
                                        <option value="$nodeGroup.name">$nodeGroup.name</option>
                                    #end
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label" style="width:10%">执行节点组</label>

                            <div class="col-sm-3">
                                <select name="taskTrackerNodeGroup" class="form-control">
                                    <option value="">-- 不限 --</option>
                                    #foreach($nodeGroup in $taskTrackerNodeGroups)
                                        <option value="$nodeGroup.name">$nodeGroup.name</option>
                                    #end
                                </select>
                            </div>
                            <label class="col-sm-2 control-label" style="width:10%">反馈客户端</label>

                            <div class="col-sm-2">
                                <select name="needFeedback" class="form-control">
                                    <option value="">-- 不限 --</option>
                                    <option value="true">需要</option>
                                    <option value="false">不需要</option>
                                </select>
                            </div>
                            <div class="col-sm-1" style="width:70px;">
                                <button class="btn btn-primary" type="button" id="searchBtn">
                                    搜索
                                </button>
                            </div>
                            <div class="col-sm-1">
                                <button class="btn btn-warning" type="reset" id="resetBtn">
                                    重置
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
                <div class="ibox-content" id="ltstableContainer">
                </div>
            </div>
        </div>
    </div>
</div>

<script id="ltstable" type="text/html">
    <table class="table table-stripped toggle-arrow-tiny footable" data-page-size="10">
        <thead>
        <tr>
            <th data-toggle="true">任务ID</th>
            <th data-hide="all">提交节点组</th>
            <th>执行节点组</th>
            <th>Cron表达式</th>
            <th data-hide="all">优先级</th>
            <th>反馈客户端</th>
            <th>重试次数</th>
            <th data-hide="all">用户参数</th>
            <th data-hide="phone,tablet">创建时间</th>
            <th data-hide="all">修改时间</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody>
        {{each rows as row index}}
        <tr>
            <td>{{row.taskId}}</td>
            <td>{{row.submitNodeGroup}}</td>
            <td>{{row.taskTrackerNodeGroup}}</td>
            <td>{{row.cronExpression}}</td>
            <td>{{row.priority}}</td>
            <td>{{row.needFeedback | format:'needFeedbackLabel',row}}</td>
            <td>{{row.retryTimes}}</td>
            <td>{{row.extParams | format:'stringifyJSON'}}</td>
            <td>{{row.gmtCreated | dateFormat:'yyyy-MM-dd HH:mm:ss'}}</td>
            <td>{{row.gmtModified | dateFormat:'yyyy-MM-dd HH:mm:ss'}}</td>
            <td>{{row.opt | format:'optFormat',row}}</td>
        </tr>
        {{/each}}
        {{if results == 0}}
        <tr>
            <td colspan="15">暂无数据</td>
        </tr>
        {{/if}}
        </tbody>
        <tfoot>
        <tr>
            <td colspan="9">
                <span>共{{results}}条记录，每页展示{{pageSize}}条</span>
                <ul class="pagination-sm pull-right"></ul>
            </td>
        </tr>
        </tfoot>
    </table>
</script>

<script>
    $(document).ready(function () {

        LTS.colFormatter.optFormat = function (v, row) {
            var logUrl = "job-logger.htm?taskId=" + row.taskId + "&taskTrackerNodeGroup=" + row.taskTrackerNodeGroup;
            return '<a target="_blank" href="' + logUrl + '"><span class="label label-info"><i class="fa fa-file-code-o"></i> 日志</span></a>&nbsp;';
        }

        var ltsTable = $("#ltstableContainer").ltsTable({
            url: 'api/job-queue/executing-job-get',
            templateId: 'ltstable'
        });

        $(document).on("click", "#searchBtn", function () {
            var params = {};
            $.each($('#form').parent().find(".form-control"), function () {
                var name = $(this).attr("name");
                var value = $(this).val();
                params[name] = value;
            });
            ltsTable.post(params, 1);
        });
        $("#searchBtn").trigger("click");
    });
</script>