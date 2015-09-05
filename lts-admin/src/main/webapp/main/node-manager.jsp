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
                    <label class="control-label">节点标识：</label>

                    <div class="controls">
                        <input type="text" class="control-text" name="identity">
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">IP：</label>

                    <div class="controls">
                        <input type="text" class="control-text" name="ip">
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">节点组：</label>

                    <div class="controls">
                        <input type="text" class="control-text" name="nodeGroup">
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">节点类型：</label>

                    <div class="controls">
                        <select name="nodeType">
                            <option value="">所有</option>
                            <option value="JOB_CLIENT">JobClient</option>
                            <option value="TASK_TRACKER">TaskTracker</option>
                            <option value="JOB_TRACKER">JobTracker</option>
                        </select>
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">状态：</label>

                    <div class="controls">
                        <select name="available">
                            <option value="">所有</option>
                            <option value="true">正常</option>
                            <option value="false">禁用</option>
                        </select>
                    </div>
                </div>
                <div class="control-group span12">
                    <label class="control-label">创建时间：</label>

                    <div class="controls">
                        <input type="text" class="calendar calendar-time" name="startDate"><span> -
                        </span><input name="endDate" type="text" class="calendar calendar-time">
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

    BUI.use(['bui/common', 'bui/grid', 'bui/form', 'bui/data', 'bui/overlay','common/date-util'], buiInit);

    function buiInit(BUI, Grid, Form, Data, Overlay, DateUtil) {

        var columns = [
            {title: '节点标识', dataIndex: 'identity', width: 230},
            {title: '集群名称', dataIndex: 'clusterName', width: 100},
            {title: '节点类型', dataIndex: 'nodeType', width: 100},
            {title: '节点组名', dataIndex: 'group', width: 150},
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
                title: '工作线程数', dataIndex: 'threads', width: 60, renderer: function (v, obj) {
                if (obj['nodeType'] == 'TASK_TRACKER') {
                    return v;
                }
                return '';
            }
            },
            {
                title: '状态', dataIndex: 'available', width: 60,
                renderer: function (v, obj) {
                    if (v) {
                        return "正常";
                    } else {
                        return "禁用";
                    }
                }
            },
            {
                title: '操作', dataIndex: '', width: 100, renderer: function (value, obj) {
                return "";
            }
            }
        ];

        var store = new Data.Store({
            url: '../api/node/node-list-get',
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