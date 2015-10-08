<div class="row wrapper border-bottom white-bg page-heading">
    <div class="col-lg-12">
        <h2>节点JVM信息</h2>
        <ol class="breadcrumb">
            <li><a>节点管理</a></li>
            <li class="active"><b>节点JVM信息</b></li>
        </ol>
    </div>
</div>

<div class="row wrapper wrapper-content animated fadeInRight">
    <div class="panel-body" style="padding-top:0px;">
        <div class="input-group">
            <input type="text" placeholder="请输入节点标识 (可以在节点上下线信息中或者节点管理中找到)"
                   name="identity"
                   value="$!identity"
                   class="form-control input-lg">

            <div class="input-group-btn">
                <button class="btn btn-lg btn-primary" type="button" id="searchBtn">&nbsp;搜&nbsp;&nbsp;索&nbsp;</button>
            </div>
        </div>
    </div>
    <div class="col-lg-12" id="jvmInfo">

    </div>
</div>

<script id="jvm-info-template" type="text/html">
    <div class="ibox border-bottom">
        <div class="ibox-title">
            <h5>JVM信息 <span class="text-navy">{{identity}}</span></h5>
        </div>
        <div class="ibox-content" id="cloud-unit">
            <table class="table table-stripped footable footable-loaded no-paging">
                <thead>
                <tr>
                    <th class="footable-sortable" style="cursor: default;width:140px;">属性名称</th>
                    <th class="footable-sortable" style="cursor: default;">属性值</th>
                </tr>
                </thead>
                <tbody>
                <div class="hide">{{index=0}}</div>
                {{each KYE_LABEL_MAP as value key}}
                <tr class="{{index++ % 2 == 0 ? 'footable-odd' : 'footable-even'}}" style="display: table-row;">
                    <td><span class="footable-toggle"></span>{{value}}</td>
                    <td>{{data[key]}}</td>
                </tr>
                {{/each}}
                </tbody>
            </table>
        </div>
    </div>
</script>

<script>
    var KYE_LABEL_MAP = {
        NodeType: "节点类型",
        Alive: "是否存活",
        NodeGroup: "节点组",
        HostName: "主机名",
        LocalIp: "主机IP",
        PID: " 进程PID",
        StartTime: "启动时间",
        InputArguments: "启动参数",
        Arch: "硬件平台",
        AvailableProcessors: "可用CPU个数",
        OSName: "操作系统",
        OSVersion: "操作系统版本",
        FileEncode: "文件编码",
        JVM: "JVM名称",
        JavaVersion: "JavaVersion",
        JavaSpecificationVersion: "JavaSpecVersion",
        JavaHome: "JavaHome",
        JavaLibraryPath: "JavaLibraryPath",
        LoadedClassCount: "当前装载的类总数",
        TotalLoadedClassCount: "总共装载过的类总数",
        UnloadedClassCount: "卸载的类总数",
        TotalCompilationTime: "总共编译时间"
    };

    $(document).ready(function () {

        $(document).on("click", "#searchBtn", function () {
            var identity = $("input[name='identity']").val();
            if (!identity) {
                sweetAlert("请输入节点标识", "可以在节点上下线信息中或者节点管理中找到。", "error");
                return;
            }
            $.ajax({
                url: 'api/jvm/node-jvm-info-get.do',
                type: 'GET',
                dataType: 'JSON',
                data: {identity: identity},
                success: function (json) {
                    if (json && json['success'] && json['results'] == 1) {
                        var obj = json.rows[0];
                        var data = JSON.parse(obj['jvmInfo']);
                        data['NodeType'] = obj['nodeType'];
                        data['NodeGroup'] = obj['nodeGroup'];
                        data['Alive'] = obj['alive'] ? "<font color='green'>是</font>" : "<font color='red'>否</font>";
                        $.each(data, function (key, value) {
                            if (key == 'StartTime') {
                                value = DateUtil.formatYMDHMD(value);
                            }
                            showJVMInfo(identity, data);
                        });
                    } else {
                        if (json && json['msg']) {
                            swal(json['msg']);
                        }
                        showJVMInfo('', {});
                    }
                }
            });
        });

        function showJVMInfo(identity, data) {
            var html = template("jvm-info-template", {
                identity: identity,
                data: data,
                KYE_LABEL_MAP: KYE_LABEL_MAP
            });
            $("#jvmInfo").html(html);
        }

        var identity = $("input[name='identity']").val();
        if (identity) {
            $("#searchBtn").trigger("click");
        } else {
            showJVMInfo('', {});
        }
    });
</script>