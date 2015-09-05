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
                        <input type="text" class="control-text" name="nodeGroup"/>
                    </div>
                </div>
                <div class="control-group span8">
                    <label class="control-label">节点类型：</label>

                    <div class="controls">
                        <select name="nodeType">
                            <option value="">所有</option>
                            <option value="JOB_CLIENT">JobClient</option>
                            <option value="TASK_TRACKER">TaskTracker</option>
                        </select>
                    </div>
                </div>
                <div class="span3">
                    <button type="button" id="btnSearch" class="button button-primary">搜索</button>
                </div>
                <div class="span3">
                    <button type="button" id="btnAdd" class="button button-primary">添加</button>
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

        $(document).ready(function () {
            $('body').on( "click", "#btnAdd",function () {
                var nodeGroup = $("input[name='nodeGroup']").val();
                var nodeType = $("select[name='nodeType']").val();
                if (!nodeGroup) {
                    BUI.Message.Alert("节点组名称不能为空.");
                    return;
                }
                if (!nodeType) {
                    BUI.Message.Alert("请选择节点类型.");
                    return;
                }
                $.ajax({
                    url: '../api/node/node-group-add',
                    type: 'POST',
                    dataType: 'json',
                    data: {nodeGroup: nodeGroup, nodeType: nodeType},
                    success: function (json) {
                        if (json && json.success) {
                            $("input[name='nodeGroup']").val("");
                            $("select[name='nodeType']").val("");
                            $('#btnSearch').trigger("click");
                        } else {
                            BUI.Message.Alert(json.msg);
                        }
                    }
                });
            });

            $('body').on("click", ".node-group-del-btn", function () {
                var nodeGroup = $(this).attr("nodeGroup");
                var nodeType = $(this).attr("nodeType");
                var _this = $(this);
                BUI.Message.Confirm('确认要删除该NodeGroup吗？对应的该NodeGroup的数据也将被删除，请谨慎操作。', function () {
                    $.ajax({
                        url: '../api/node/node-group-del',
                        type: 'POST',
                        dataType: 'json',
                        data: {nodeGroup: nodeGroup, nodeType: nodeType},
                        success: function (json) {
                            if (json && json.success) {
                                _this.parents("tr").remove();
                            } else {
                                BUI.Message.Alert(json.msg);
                            }
                        }
                    });
                }, 'question');
            });
        });

        var columns = [
            {title: '节点类型', dataIndex: 'nodeType', width: 100},
            {title: '节点组名', dataIndex: 'name', width: 300},
            {
                title: '节点组创建时间', dataIndex: 'gmtCreated', width: 125, renderer: function (v) {
                return DateUtil.formatYMDHMD(v);
            }
            },
            {
                title: '操作', dataIndex: '', width: 100, renderer: function (value, obj) {
                return '<a href="javascript:;" class="node-group-del-btn" nodeGroup="' + obj.name + '" nodeType="' + obj.nodeType + '">删除</a>';
            }
            }
        ];

        var store = new Data.Store({
            url: '../api/node/node-group-get',
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
    }

</script>
</body>
</html>