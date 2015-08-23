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
                    <label class="control-label">TaskId：</label>

                    <div class="controls">
                        <input type="text" class="control-text" name="taskId">
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">提交节点组：</label>

                    <div class="controls">
                        <select name="submitNodeGroup">
                            <option value="">不限</option>
                            <c:forEach items="${jobClientNodeGroups}" var="nodeGroup">
                                <option value="${nodeGroup.name}">${nodeGroup.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">执行节点组：</label>

                    <div class="controls">
                        <select name="taskTrackerNodeGroup">
                            <option value="">不限</option>
                            <c:forEach items="${taskTrackerNodeGroups}" var="nodeGroup">
                                <option value="${nodeGroup.name}">${nodeGroup.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">是否需要反馈：</label>

                    <div class="controls">
                        <select name="needFeedback">
                            <option value="">不限</option>
                            <option value="true">是</option>
                            <option value="false">否</option>
                        </select>
                    </div>
                </div>
                <div class="span3 offset2">
                    <button type="button" id="btnSearch" class="button button-primary">搜索</button>
                </div>
            </div>
        </form>
    </div>
    <div class="search-grid-container">
        <div id="grid"></div>
    </div>
</div>

<div id="jobEditDlgContent" class="hidden">
    <form id="jobEditDlgForm" class="form-horizontal">
        <div class="row">
            <span class="hidden" id="oldJobInfo">
            </span>
            <input type="hidden" name="jobId" value=""/>

            <div class="control-group span8">
                <label class="control-label">CronExpression：</label>

                <div class="controls">
                    <input type="text" name="cronExpression" class="input-normal control-text"
                           data-rules="{required : true}">
                </div>
            </div>
            <div class="control-group span8">
                <label class="control-label">优先级：</label>

                <div class="controls">
                    <input type="text" name="priority" placeholder="数值越小，优先级越大" class="input-normal control-text"
                           data-rules="{required : true, number:true}">
                </div>
            </div>
            <div class="control-group span8">
                <label class="control-label">提交节点组：</label>

                <div class="controls">
                    <input type="text" name="submitNodeGroup" class="input-normal control-text"
                           data-rules="{required : true}">
                </div>
            </div>
            <div class="control-group span8">
                <label class="control-label">执行TaskTracker节点组：</label>

                <div class="controls">
                    <input type="text" name="taskTrackerNodeGroup" class="input-normal control-text"
                           data-rules="{required : true}">
                </div>
            </div>
            <div class="control-group span8">
                <label class="control-label">反馈客户端：</label>

                <div class="controls">
                    <select class="input-normal" name="needFeedback">
                        <option value="true">是</option>
                        <option value="false">否</option>
                    </select>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="control-group span15">
                <label class="control-label">用户参数(JSON)：</label>

                <div class="controls control-row4">
                    <textarea class="input-large" name="extParams" type="text"></textarea>
                </div>
            </div>
        </div>
    </form>
</div>

<script type="text/javascript">
    BUI.use('common/page');
</script>
<script type="text/javascript">

    BUI.use(['bui/common', 'bui/grid', 'bui/form', 'bui/data', 'bui/overlay', 'common/date-util'], buiInit);

    function buiInit(BUI, Grid, Form, Data, Overlay, DateUtil) {

        var columns = [
            {title: '任务ID(TaskId)', dataIndex: 'taskId', width: 240, sortable: false},
            {title: '提交节点组', dataIndex: 'submitNodeGroup', width: 150},
            {title: '执行节点组', dataIndex: 'taskTrackerNodeGroup', width: 150},
            {title: 'cron表达式', dataIndex: 'cronExpression', width: 100},
            {title: '优先级', dataIndex: 'priority', width: 60},
            {
                title: '反馈客户端', dataIndex: 'needFeedback', width: 80, renderer: function (v) {
                return v ? '需要' : '不需要';
            }
            },
            {
                title: '用户参数', dataIndex: 'extParams', width: 140, sortable: false, renderer: function (v, obj) {
                if (v) {
                    return JSON.stringify(v);
                }
                return '';
            }
            },
            {
                title: '创建时间', dataIndex: 'gmtCreated', width: 125, renderer: function (v) {
                return DateUtil.formatYMDHMD(v);
            }
            },
            {
                title: '修改时间', dataIndex: 'gmtModified', width: 125, renderer: function (v) {
                return DateUtil.formatYMDHMD(v);
            }
            },
            {
                title: '操作', dataIndex: '', width: 90, sortable: false, renderer: function (value, obj) {
                var logUrl = "../job-logger/job-logger.htm?taskId=" + obj.taskId + "&taskTrackerNodeGroup=" + obj.taskTrackerNodeGroup;
                return '<a target="_blank" href="'+ logUrl +'">日志</a>&nbsp;' +
                        '<a href="javascript:;" class="job-edit-btn">编辑<span class="hidden">' + JSON.stringify(obj) + '</span></a>&nbsp;' +
                        '<a href="javascript:;" class="job-del-btn" jobId="' + obj.jobId + '">删除</a>';
            }
            }
        ];

        var store = new Data.Store({
            url: '../api/job-queue/cron-job-get',
            autoLoad: false,
            pageSize: 10,
            remoteSort: true
        });

        store.on('exception', function (ev) {
            BUI.Message.Alert(JSON.stringify(ev.error));
        });

        var grid = new Grid.Grid({
            render: "#grid",
            columns: columns,
            loadMask: true,
            bbar: {
                pagingBar: true
            },
            emptyDataTpl : '<div class="centered">查询的数据不存在</div>',
            store: store
        });
        grid.render();

        // initForm
        var form = new Form.HForm({
            srcNode: '#searchForm'
        }).render();


        $('#btnSearch').on('click', function (ev) {
            ev.preventDefault();
            form.valid();
            if (form.isValid()) {
                var param = form.serializeToObject();
                param.start = 0;
                param.pageIndex = 0;
                store.load(param);
            }
        });
        $('#btnSearch').trigger("click");

        var jobEditDlgForm = new Form.HForm({
            srcNode: '#jobEditDlgForm'
        }).render();

        var editForm = $("#jobEditDlgForm");

        var jobEditDlg = new Overlay.Dialog({
            title: '修改定时任务',
            width: 500,
            height: 320,
            contentId: 'jobEditDlgContent',
            success: function () {

                if (!jobEditDlgForm.isValid()) {
                    return;
                }

                // 提交表单，查看修改了哪些属性
                var oldJob = JSON.parse(editForm.find("#oldJobInfo").text());
                var updateJSON = {jobId: oldJob.jobId};
                var cronExpression = editForm.find("input[name='cronExpression']").val();
                if (cronExpression.trim() != oldJob.cronExpression) {
                    // 表示修改了
                    updateJSON['cronExpression'] = cronExpression;
                }
                var priority = editForm.find("input[name='priority']").val();
                if (priority.trim() != (oldJob.priority + '')) {
                    updateJSON['priority'] = priority;
                }
                var needFeedback = editForm.find("select[name='needFeedback']").val();
                if (needFeedback.trim() != (oldJob.needFeedback + '')) {
                    updateJSON['needFeedback'] = needFeedback;
                }
                var extParams = editForm.find("textarea[name='extParams']").val();
                if (!extParams) {
                    extParams = '{}';
                }
                // 验证是不是JSON
                try {
                    JSON.parse(extParams.trim());
                } catch (e) {
                    BUI.Message.Alert("请输入正确的用户参数,JSON格式!");
                    return;
                }
                if (extParams.trim() != JSON.stringify(oldJob.extParams)) {
                    updateJSON['extParams'] = extParams;
                }
                var taskTrackerNodeGroup = editForm.find("input[name='taskTrackerNodeGroup']").val();
                if (taskTrackerNodeGroup.trim() != oldJob.taskTrackerNodeGroup) {
                    updateJSON['taskTrackerNodeGroup'] = taskTrackerNodeGroup;
                }
                var submitNodeGroup = editForm.find("input[name='submitNodeGroup']").val();
                if (submitNodeGroup.trim() != oldJob.submitNodeGroup) {
                    updateJSON['submitNodeGroup'] = submitNodeGroup;
                }

                // 判断是否修改过
                console.log(updateJSON);
                var modified = false;
                for (var key in updateJSON) {
                    if (key != 'jobId') {
                        modified = true;
                        break;
                    }
                }
                if (modified) {
                    // 请求修改数据
                    $.ajax({
                        url: '../api/job-queue/cron-job-update',
                        type: 'POST',
                        dataType: 'json',
                        data: updateJSON,
                        success: function (json) {
                            if (json && json.success) {
                                BUI.Message.Alert("修改成功");
                                location.reload();
                            } else {
                                BUI.Message.Alert("修改失败, " + json.msg);
                            }
                        }
                    });
                } else {
                    jobEditDlg.close();
                }
            }
        });

        // 编辑按钮
        $(document).on("click", ".job-edit-btn", function () {
            var jobText = $(this).children("span").text();
            var job = JSON.parse(jobText);
            editForm.find("#oldJobInfo").html(jobText);
            editForm.find("input[name='jobId']").val(job.jobId);
            editForm.find("input[name='cronExpression']").val(job.cronExpression);
            editForm.find("input[name='priority']").val(job.priority);
            editForm.find("input[name='taskTrackerNodeGroup']").val(job.taskTrackerNodeGroup);
            editForm.find("input[name='submitNodeGroup']").val(job.submitNodeGroup);
            editForm.find("select[name='needFeedback']").val("" + job.needFeedback);
            editForm.find("textarea[name='extParams']").val(JSON.stringify(job.extParams));
            jobEditDlg.show();
        });

        $(document).on("click", ".job-del-btn", function () {
            var jobId = $(this).attr("jobId");
            var that = $(this);
            BUI.Message.Confirm('确认要删除该条CronJob吗？', function () {
                $.ajax({
                    url: '../api/job-queue/cron-job-delete',
                    type: 'POST',
                    dataType: 'json',
                    data: {jobId: jobId},
                    success: function (json) {
                        if (json && json.success) {
                            BUI.Message.Alert("删除成功!");
                            that.parents(".bui-grid-row").remove();
                        } else {
                            BUI.Message.Alert("删除失败, " + json.msg);
                        }
                    }
                });
            }, 'question');
        });
    }

</script>
</body>
</html>
