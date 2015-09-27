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
                    <label class="control-label">节点组：</label>

                    <div class="controls">
                        <input type="text" class="control-text" name="group">
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">节点标识：</label>

                    <div class="controls">
                        <input type="text" class="control-text" name="identity">
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">事件类型：</label>
                    <div class="controls">
                        <select name="event">
                            <option value="">所有</option>
                            <option value="ONLINE">上线</option>
                            <option value="OFFLINE">离线</option>
                        </select>
                    </div>
                </div>
                <div class="control-group span12">
                    <label class="control-label">创建时间：</label>
                    <div class="controls">
                        <input type="text" class="calendar calendar-time" name="startLogTime" value="${startLogTime}"><span> -
                        </span><input name="endLogTime" type="text" class="calendar calendar-time" value="${endLogTime}">
                    </div>
                </div>
                <div class="control-group span3">
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

    BUI.use(['bui/common', 'bui/grid', 'bui/form', 'bui/data', 'bui/overlay','common/date-util'], buiInit);

    function buiInit(BUI, Grid, Form, Data, Overlay, DateUtil) {

        var columns = [
            {
                title: '记录时间', dataIndex: 'logTime', width: 125, renderer: function (v) {
                return DateUtil.formatYMDHMD(v);
                }
            },
            {
                title: '事件', dataIndex: 'event', width: 50, renderer: function (v) {
                return v == "ONLINE" ? "上线" : "离线";
            }
            },
            {title: '集群名称', dataIndex: 'clusterName', width: 120},
            {title: '节点类型', dataIndex: 'nodeType', width: 100},
            {title: '节点组名', dataIndex: 'group', width: 180},
            {title: '节点标识', dataIndex: 'identity', width: 245},
            {
                title: '节点创建时间', dataIndex: 'createTime', width: 125, renderer: function (v) {
                return DateUtil.formatYMDHMD(v);
            }
            },
            {
                title: '机器', dataIndex: 'ip', width: 140, renderer: function (v, obj) {
                if (obj['nodeType'] == 'JOB_TRACKER') {
                    return obj['hostName'] + "<br/>(" + obj['ip'] + ":" + obj['port'] + ")";
                }
                return obj['hostName'] + "<br/>("  + obj['ip'] + ")";
            }
            },
            {
                title: '工作线程数', dataIndex: 'threads', width: 80, renderer: function (v, obj) {
                if (obj['nodeType'] == 'TASK_TRACKER') {
                    return v;
                }
                return '无';
            }
            }
        ];

        var store = new Data.Store({
            url: '../api/node/node-onoffline-log-get',
            autoLoad: true,
            pageSize: 20,
            remoteSort: false
        });

        store.on('exception', function (ev) {
            BUI.Message.Alert(JSON.stringify(ev.error));
        });

        var grid = new Grid.Grid({
            render: "#grid",
            columns: columns,
            loadMask : true,
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
    }

</script>
</body>
</html>