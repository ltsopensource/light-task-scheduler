<div class="row wrapper border-bottom white-bg page-heading">
    <div class="col-lg-12">
        <h2>节点管理</h2>
        <ol class="breadcrumb">
            <li><a>节点管理</a></li>
            <li class="active"><b>节点管理</b></li>
        </ol>
    </div>
</div>

<div class="wrapper wrapper-content animated fadeInRight">
    <div class="row">
        <div class="col-lg-12">
            <div class="ibox">
                <div class="ibox-title">
                    <h3><span class="text-navy">节点管理</span></h3>
                </div>
                <div class="ibox-content">
                    <form method="post" id="form" class="form-horizontal" onsubmit="return false">
                        <div class="form-group">
                            <label class="col-sm-1 control-label">节点标识</label>

                            <div class="col-sm-2">
                                <input type="text" class="form-control" name="identity"
                                       placeholder="请输入节点标识">
                            </div>
                            <label class="col-sm-1 control-label">IP</label>

                            <div class="col-sm-2">
                                <input type="text" class="form-control" name="ip"
                                       placeholder="请输入IP">
                            </div>
                            <label class="col-sm-1 control-label">节点组</label>

                            <div class="col-sm-2">
                                <input type="text" class="form-control" name="nodeGroup"
                                       placeholder="请输入节点组">
                            </div>
                            <label class="col-sm-1 control-label">节点类型</label>

                            <div class="col-sm-2">
                                <select name="nodeType" class="form-control">
                                    <option value="">所有</option>
                                    <option value="JOB_CLIENT">JOB_CLIENT</option>
                                    <option value="TASK_TRACKER">TASK_TRACKER</option>
                                    <option value="JOB_TRACKER">JOB_TRACKER</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-1 control-label">状态</label>

                            <div class="col-sm-2">
                                <select name="available" class="form-control">
                                    <option value="">所有</option>
                                    <option value="true">正常</option>
                                    <option value="false">禁用</option>
                                </select>
                            </div>
                            <label class="col-sm-1 control-label">创建时间</label>

                            <div class="col-sm-2">
                                <input class="form-control datepicker" type="text" style="width:160px"
                                       id="startDate"
                                       name="startDate"
                                       date-format="yyyy-MM-dd HH:mm:ss"
                                       placeholder="yyyy-MM-dd HH:mm:ss"/>
                            </div>
                            <label class="control-label" style="width: 20px;float: left;">到</label>

                            <div class="col-sm-2">
                                <input class="form-control datepicker" type="text" style="width:160px"
                                       id="endDate"
                                       name="endDate"
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
            <th data-toggle="true">节点标识</th>
            <th data-hide="all">集群名称</th>
            <th>节点类型</th>
            <th>节点组名</th>
            <th data-hide="phone,tablet">节点创建时间</th>
            <th>机器</th>
            <th>工作线程数</th>
            <th data-hide="phone,tablet">状态</th>
            <th data-hide="all">command端口</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody>
        {{each rows as row index}}
        <tr>
            <td>{{row.identity}}</td>
            <td>{{row.clusterName}}</td>
            <td>{{row.nodeType}}</td>
            <td>{{row.group}}</td>
            <td>{{row.createTime | dateFormat:'yyyy-MM-dd HH:mm:ss'}}</td>
            <td>{{row.ip | format:'ipLabel',row}}</td>
            <td>{{row.threads | format:'threadLabel',row}}</td>
            <td>{{row.available | format:'availableLabel'}}</td>
            <td>{{row.commandPort }}</td>
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

        LTS.colFormatter.ipLabel = function (v, row) {
            if (row['nodeType'] == 'JOB_TRACKER') {
                return row['hostName'] + "<br/>(" + row['ip'] + ":" + row['port'] + ")";
            }
            return row['hostName'] + "<br/>(" + row['ip'] + ")";
        }

        LTS.colFormatter.threadLabel = function (v, row) {
            if (row['nodeType'] == 'TASK_TRACKER') {
                return v;
            }
            return '无';
        }
        LTS.colFormatter.availableLabel = function (v) {
            if (v) {
                return "正常";
            } else {
                return "禁用";
            }
        }
        LTS.colFormatter.optFormat = function (v, row) {
            return '<a href="{url}" target="_blank" title="JVM信息"><span class="label label-info"><i class="fa fa-info-circle"></i> JVM</span></a>'
                    .replace("{url}", "node-jvm-info.htm?identity=" + row.identity);
        }

        var ltsTable = $("#ltstableContainer").ltsTable({
            url: 'api/node/node-list-get',
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