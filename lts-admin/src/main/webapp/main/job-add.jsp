<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML>
<html>
<head>
    <jsp:include page="head.jsp"/>
</head>
<body>
<div class="container">
    <form id="J_Form" class="form-form-horizontal">
        <div class="row mb_10">
            <div class="control-group span8">
                <label class="control-label">TaskId：</label>

                <div class="controls">
                    <input type="text" name="taskId" class="input-large control-text" data-rules="{required : true}">
                </div>
            </div>
        </div>
        <div class="row mb_10">
            <div class="control-group span8">
                <label class="control-label">CronExpression：</label>

                <div class="controls">
                    <input type="text" name="cronExpression" class="input-large control-text">
                </div>
            </div>
        </div>
        <div class="row mb_10">
            <div class="control-group span8">
                <label class="control-label">TriggerTime：</label>

                <div class="controls">
                    <input type="text" class="calendar calendar-time" name="triggerTime">
                </div>
            </div>
        </div>
        <div class="row mb_10">

            <div class="control-group span8">
                <label class="control-label">优先级：</label>

                <div class="controls">
                    <input type="text" name="priority" placeholder="数值越小，优先级越大" class="input-large control-text"
                           value="100" data-rules="{required : true, number:true}">
                </div>
            </div>
        </div>
        <div class="row mb_10">
            <div class="control-group span8">
                <label class="control-label">提交节点组：</label>

                <div class="controls">
                    <select name="submitNodeGroup" class="input-large">
                        <c:forEach items="${jobClientNodeGroups}" var="nodeGroup">
                            <option value="${nodeGroup.name}">${nodeGroup.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>
        <div class="row mb_10">
            <div class="control-group span8">
                <label class="control-label">执行TaskTracker节点组：</label>

                <div class="controls">
                    <select name="taskTrackerNodeGroup" class="input-large">
                        <c:forEach items="${taskTrackerNodeGroups}" var="nodeGroup">
                            <option value="${nodeGroup.name}">${nodeGroup.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>
        <div class="row mb_10">
            <div class="control-group span8">
                <label class="control-label">反馈客户端：</label>

                <div class="controls">
                    <select class="input-large" name="needFeedback">
                        <option value="true">是</option>
                        <option value="false">否</option>
                    </select>
                </div>
            </div>
        </div>
        <div class="row mb_10">
            <div class="control-group span15">
                <label class="control-label">用户参数(JSON)：</label>

                <div class="controls control-row4">
                    <textarea class="input-large" name="extParams" type="text"></textarea>
                </div>
            </div>
        </div>
        <div class="row actions-bar">
            <div class="form-actions span13 offset3">
                <button type="button" class="button button-primary" id="saveBtn">保存</button>
                <button type="reset" class="button" id="resetBtn">重置</button>
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
        var form = new Form.Form({
            srcNode: '#J_Form'
        }).render();

        $(document).on("click", "#saveBtn", function () {
            if (!form.isValid()) {
                return;
            }

            $.ajax({
                url: '../api/job-queue/job-add',
                type: 'POST',
                dataType: 'json',
                data: form.serializeToObject(),
                success: function (json) {
                    if (json && json.success) {
                        BUI.Message.Alert("添加成功");
                        $("#resetBtn").trigger("click");
                    } else {
                        BUI.Message.Alert("添加失败, " + json.msg);
                    }
                }
            });

        });

    }
</script>
</body>
</html>
