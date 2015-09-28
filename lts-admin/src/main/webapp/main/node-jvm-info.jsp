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
                        <input type="text" class="control-text" name="identity" value="${identity}" placeholder="请输入节点标识">
                    </div>
                </div>
                <div class="control-group span3">
                    <button type="button" id="btnSearch" class="button button-primary">搜索</button>
                </div>
            </div>
        </form>
    </div>
<!--
Arch: "x86_64"
AvailableProcessors: 4
FileEncode: "UTF-8"
HostName: "huguis-Macintosh.local"
InputArguments: "[-agentlib:jdwp=transport=dt_socket,address=127.0.0.1:56405,suspend=y,server=n, -Dfile.encoding=UTF-8]"
JVM: "Java HotSpot(TM) 64-Bit Server VM (25.51-b03, mixed mode)"
JavaHome: "/Library/Java/JavaVirtualMachines/jdk1.8.0_51.jdk/Contents/Home/jre"
JavaLibraryPath: "/Users/hugui/Library/Java/Extensions:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:."
JavaSpecificationVersion: "1.8"
JavaVersion: "1.8.0_51"
LoadedClassCount: 2762
LocalIp: "192.168.31.208"
OSName: "Mac OS X"
OSVersion: "10.10.5"
PID: "18743"
StartTime: 1443455380389
TotalCompilationTime: 4652
TotalLoadedClassCount: 2762
UnloadedClassCount: 0
-->
    <div class="container">
        <table>
            <tr><td>节点类型:</td><td id="NodeType"></td></tr>
            <tr><td>节点组:</td><td id="NodeGroup"></td></tr>
            <tr><td>主机名:</td><td id="HostName"></td></tr>
            <tr><td>主机IP:</td><td id="LocalIp"></td></tr>
            <tr><td>进程PID:</td><td id="PID"></td></tr>
            <tr><td>启动时间:</td><td id="StartTime"></td></tr>
            <tr><td>启动参数:</td><td id="InputArguments"></td></tr>
            <tr><td>硬件平台:</td><td id="Arch"></td></tr>
            <tr><td>可用CPU个数:</td><td id="AvailableProcessors"></td></tr>
            <tr><td>操作系统:</td><td id="OSName"></td></tr>
            <tr><td>文件编码:</td><td id="FileEncode"></td></tr>
            <tr><td>JVM名称:</td><td id="JVM"></td></tr>
            <tr><td>JavaVersion:</td><td id="JavaVersion"></td></tr>
            <tr><td>JavaSpecVersion:</td><td id="JavaSpecificationVersion"></td></tr>
            <tr><td>JavaHome:</td><td id="JavaHome"></td></tr>
            <tr><td>JavaLibraryPath:</td><td id="JavaLibraryPath"></td></tr>
            <tr><td>当前装载的类总数:</td><td id="LoadedClassCount"></td></tr>
            <tr><td>总共装载过的类总数:</td><td id="TotalLoadedClassCount"></td></tr>
            <tr><td>卸载的类总数:</td><td id="UnloadedClassCount"></td></tr>
            <tr><td>总共编译时间:</td><td id="TotalCompilationTime"></td></tr>
        </table>
    </div>
</div>

<script type="text/javascript">
    BUI.use('common/page');
</script>
<script type="text/javascript">

    BUI.use(['bui/common','common/date-util'], buiInit);

    function buiInit(BUI, DateUtil) {

        $('#btnSearch').on('click', function (ev) {
            var identity = $("input[name='identity']").val();
            if (!identity) {
                BUI.Message.Alert("请输入节点标识.");
                return;
            }
            $.ajax({
                url: '/api/jvm/node-jvm-info-get.do',
                type: 'GET',
                dataType: 'JSON',
                data: {identity: identity},
                success: function (json) {
                    if (json && json['success'] && json['results'] == 1) {
                        var obj = json.rows[0];
                        $("#NodeType").html(obj['nodeType']);
                        $("#NodeGroup").html(obj['nodeGroup']);

                         var data = JSON.parse(obj['jvmInfo']);
                        console.log(data);
                        $.each(data, function(key, value){
                            if(key == 'StartTime'){
                                value = DateUtil.formatYMDHMD(value);
                            }
                            $("#" + key).html(value);
                        });
                    }
                }
            });
        });
    }

</script>
</body>
</html>