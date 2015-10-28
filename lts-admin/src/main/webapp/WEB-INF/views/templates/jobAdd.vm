<div class="row wrapper border-bottom white-bg page-heading" xmlns="http://www.w3.org/1999/html">
    <div class="col-lg-12">
        <h2>任务添加</h2>
        <ol class="breadcrumb">
            <li><a>任务队列管理</a></li>
            <li class="active"><b>任务添加</b></li>
        </ol>
    </div>
</div>

<div class="wrapper wrapper-content animated fadeInRight">
    <div class="row">
        <div class="col-lg-12">
            <div class="ibox">
                <div class="ibox-title">
                    <h3><span class="text-navy">任务添加</span></h3>
                </div>
                <div class="ibox-content">
                    <form method="post" id="form" class="form-horizontal" onsubmit="return false">
                        <div class="form-group">
                            <label class="col-sm-2 control-label">任务ID</label>

                            <div class="col-sm-4">
                                <input type="text" class="form-control" name="taskId"
                                       placeholder="请输入任务ID(TaskId)【必填】">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">任务类型</label>

                            <div class="col-sm-2">
                                <select name="jobType" class="form-control">
                                    <option value="REAL_TIME_JOB">实时任务</option>
                                    <option value="TRIGGER_TIME_JOB">定时任务</option>
                                    <option value="CRON_JOB">Cron任务</option>
                                </select>
                            </div>
                            <div class="col-sm-3" id="cronJob" style="display:none;">
                                <input type="text" class="form-control" name="cronExpression"
                                       placeholder="请输入CronExpression">
                            </div>
                            <div class="col-sm-3" id="triggerTimeJob" style="display:none;">
                                <input class="form-control datepicker" type="text" style="width:160px"
                                       id="triggerTime"
                                       name="triggerTime"
                                       date-format="yyyy-MM-dd HH:mm:ss"
                                       placeholder="请输入触发时间"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">反馈客户端</label>

                            <div class="col-sm-3">
                                <select name="needFeedback" class="form-control">
                                    <option value="true">需要</option>
                                    <option value="false" selected>不需要</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">优先级</label>

                            <div class="col-sm-3">
                                <input type="text" class="form-control" name="priority" value="100"
                                       placeholder="必须为数字，数值越小，优先级越大【必填】">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">提交节点组</label>

                            <div class="col-sm-3">
                                <select name="submitNodeGroup" class="form-control">
                                    <option value="">-- 请选择提交节点组 --</option>
                                    #foreach($nodeGroup in $jobClientNodeGroups)
                                        <option value="$nodeGroup.name">$nodeGroup.name</option>
                                    #end
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">执行节点组</label>

                            <div class="col-sm-3">
                                <select name="taskTrackerNodeGroup" class="form-control">
                                    <option value="">-- 请选择执行节点组 --</option>
                                    #foreach($nodeGroup in $taskTrackerNodeGroups)
                                        <option value="$nodeGroup.name">$nodeGroup.name</option>
                                    #end
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">用户参数</label>

                            <div class="col-sm-4">
                                <textarea type="text" class="form-control" name="extParams"
                                          placeholder="请输入用户参数 JSON格式【非必填】"></textarea>
                            </div>
                        </div>
                        <div class="hr-line-dashed"></div>
                        <div class="form-group">
                            <div class="col-sm-1 col-sm-offset-2" style="width:70px;">
                                <button class="btn btn-primary" type="button" id="addBtn">
                                    添加
                                </button>
                            </div>
                            <div class="col-sm-1">
                                <button class="btn btn-warning" type="reset" id="resetBtn">
                                    重置
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>


<script>
    $(document).ready(function () {

        $(document).on("change", "select[name='jobType']", function () {
            var jobType = $(this).val();
            if (jobType == 'REAL_TIME_JOB') {
                $("#cronJob").hide();
                $("#triggerTimeJob").hide();
            } else if (jobType == 'TRIGGER_TIME_JOB') {
                $("#cronJob").hide();
                $("#triggerTimeJob").show();
            } else if (jobType == 'CRON_JOB') {
                $("#cronJob").show();
                $("#triggerTimeJob").hide();
            }
        });

        $(document).on("click", "#addBtn", function () {
            var params = {};
            $.each($('#form').parent().find(".form-control"), function () {
                var name = $(this).attr("name");
                var value = $(this).val();
                params[name] = value;
            });

            var jobType = params['jobType'];
            params['jobType'] = '';
            if (jobType == 'REAL_TIME_JOB') {
                params['cronExpression'] = '';
                params['triggerTime'] = '';
            } else if (jobType == 'TRIGGER_TIME_JOB') {
                params['cronExpression'] = '';
                var triggerTime = params['triggerTime'];
                if (!triggerTime) {
                    sweetAlert("请输入任务触发时间", "", "error");
                    return;
                }
            } else if (jobType == 'CRON_JOB') {
                var cronExpression = params['cronExpression'];
                params['triggerTime'] = '';
                if (!cronExpression) {
                    sweetAlert("请输入Cron表达式", "", "error");
                    return;
                }
            }

            // check form
            if (!params['taskId']) {
                sweetAlert("请输入任务ID", "请输入任务ID(TaskId)【必填】", "error");
                return;
            }
            var priority = params['priority'];
            if (!priority) {
                sweetAlert("请输入优先级", "必须为数字，数值越小，优先级越大【必填】", "error");
                return;
            }
            if (!LTS.ReExp.number.test(priority)) {
                sweetAlert("优先级格式错误", "必须为数字，数值越小，优先级越大【必填】", "error");
                return;
            }
            if (!params['submitNodeGroup']) {
                sweetAlert("请选择提交节点组", "如果列表中没有，请在节点组管理中添加，并启动改节点。", "error");
                return;
            }
            if (!params['taskTrackerNodeGroup']) {
                sweetAlert("请选择执行节点组", "如果列表中没有，请在节点组管理中添加，并启动改节点。", "error");
                return;
            }

            var extParams = params['extParams'];
            if (extParams) {
                try {
                    JSON.parse(extParams)
                } catch (e) {
                    sweetAlert("用户参数格式错误", "必须为JSON格式", "error");
                    return;
                }
            }

            $.ajax({
                url: 'api/job-queue/job-add',
                type: 'POST',
                dataType: 'json',
                data: params,
                success: function (json) {
                    if (json && json.success) {
                        swal('添加成功');
                        $("#resetBtn").trigger("click");
                        $("#cronJob").hide();
                        $("#triggerTimeJob").hide();
                    } else {
                        if (json) {
                            swal(json['msg']);
                        }
                    }
                }

            });
        });
    });
</script>