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
                    <label class="control-label">任务ID：</label>

                    <div class="controls">
                        <input type="text" class="control-text" name="taskId" value="${taskId}"
                               data-rules="{required : true}">
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">执行节点组：</label>

                    <div class="controls">
                        <select name="taskTrackerNodeGroup">
                            <c:forEach items="${taskTrackerNodeGroups}" var="nodeGroup">
                                <option value="${nodeGroup.name}" ${taskTrackerNodeGroup eq nodeGroup.name ? "selected":''}>${nodeGroup.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="control-group span12">
                    <label class="control-label">日志记录时间：</label>

                    <div class="controls">
                        <input type="text" class="calendar calendar-time" name="startLogTime"
                               value="${startLogTime}"><span> -
                        </span><input name="endLogTime" value="${endLogTime}" type="text"
                                      class="calendar calendar-time">
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

<script type="text/javascript">
    BUI.use('common/page');
</script>
<script type="text/javascript">

    var LOG_TYPE = {
        SENT: '派发任务',
        FINISHED: '完成任务',
        FIXED_DEAD: '修复死任务',
        BIZ: '业务日志',
        RESEND: '重新反馈任务'
    };

    BUI.use(['bui/common', 'bui/grid', 'bui/form', 'bui/data', 'bui/overlay', 'common/date-util'], buiInit);

    function buiInit(BUI, Grid, Form, Data, Overlay, DateUtil) {

        var columns = [
            {
                title: '日志记录时间', dataIndex: 'logTime', width: 125, renderer: function (v) {
                return DateUtil.formatYMDHMD(v);
            }
            },
            {
                title: '日志创建时间', dataIndex: 'gmtCreated', width: 125, renderer: function (v) {
                return DateUtil.formatYMDHMD(v);
            }
            },
            {
                title: '执行节点组', dataIndex: 'taskTrackerNodeGroup', sortable: false, width: 145
            },
            {
                title: '任务ID', dataIndex: 'taskId', sortable: false, width: 240
            },
            {
                title: '日志类型', dataIndex: 'logType', width: 60, renderer: function (v) {
                return LOG_TYPE[v];
            }
            },
            {
                title: '执行结果', dataIndex: 'success', width: 60, renderer: function (v) {
                return v ? '成功' : '失败';
            }
            },
            {
                title: '日志级别', dataIndex: 'level', width: 60
            },
            {
                title: '重试次数', dataIndex: 'retryTimes', width: 60
            },
            {
                title: '优先级', dataIndex: 'priority', sortable: false, width: 60
            },
            {title: '提交节点组', dataIndex: 'submitNodeGroup', sortable: false, width: 150},
            {
                title: '执行时间', dataIndex: 'triggerTime', sortable: false, width: 125, renderer: function (v) {
                if(v){
                    return DateUtil.formatYMDHMD(v);
                }
                return v;
            }
            },
            {title: 'cron表达式', dataIndex: 'cronExpression', sortable: false, width: 100},
            {
                title: '反馈客户端', dataIndex: 'needFeedback', sortable: false, width: 80, renderer: function (v) {
                return v ? '需要' : '不需要';
            }
            },
            {
                title: '用户参数', dataIndex: 'extParams', sortable: false, width: 140, renderer: function (v, obj) {
                if (v) {
                    return JSON.stringify(v);
                }
                return '';
            }
            },
            {
                title: '内容', dataIndex: 'msg', sortable: false, width: 200
            }
        ];

        var store = new Data.Store({
            url: '../api/job-logger/job-logger-get',
            autoLoad: false,
            pageSize: 10,
            remoteSort: false
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
            emptyDataTpl: '<div class="centered">查询的数据不存在</div>',
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
        var taskId = $("#searchForm").find("input[name='taskId']").val();
        if (taskId) {
            $('#btnSearch').trigger("click");
        }
    }

</script>
</body>
</html>
