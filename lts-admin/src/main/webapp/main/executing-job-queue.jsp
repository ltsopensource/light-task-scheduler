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
                <%--<div class="control-group span12">--%>
                <%--<label class="control-label">创建时间：</label>--%>

                <%--<div class="controls">--%>
                <%--<input type="text" class="calendar calendar-time" name="startDate"><span> ---%>
                <%--</span><input name="endDate" type="text" class="calendar calendar-time">--%>
                <%--</div>--%>
                <%--</div>--%>
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

    BUI.use(['bui/common', 'bui/grid', 'bui/form', 'bui/data', 'bui/overlay', 'common/date-util'], buiInit);

    function buiInit(BUI, Grid, Form, Data, Overlay, DateUtil) {

        var columns = [
            {title: '任务ID', dataIndex: 'taskId', width: 230, sortable: false},
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
            }
        ];

        var store = new Data.Store({
            url: '/api/job-queue/executing-job-get',
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

    }

</script>
<body>
</html>
