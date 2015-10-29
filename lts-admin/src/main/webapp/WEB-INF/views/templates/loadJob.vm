<div class="row wrapper border-bottom white-bg page-heading">
    <div class="col-lg-12">
        <h2>手动加载任务</h2>
        <ol class="breadcrumb">
            <li><a>任务队列管理</a></li>
            <li class="active"><b>手动加载任务</b></li>
        </ol>
    </div>
</div>

<div class="wrapper wrapper-content animated fadeInRight">
    <div class="row">
        <div class="col-lg-12">
            <div class="ibox">
                <div class="ibox-title">
                    <h3><span class="text-navy">手动加载任务</span></h3>
                </div>
                <div class="ibox-content">
                    <form method="post" id="form" class="form-horizontal" onsubmit="return false">
                        <div class="form-group">
                            <label class="col-sm-2 control-label" style="width:10%">执行节点组</label>

                            <div class="col-sm-3">
                                <select name="taskTrackerNodeGroup" class="form-control">
                                    <option value="">所有</option>
                                    #foreach($nodeGroup in $taskTrackerNodeGroups)
                                        <option value="$nodeGroup.name">$nodeGroup.name</option>
                                    #end
                                </select>
                            </div>
                            <div class="col-sm-1" style="width:70px;">
                                <button class="btn btn-primary" type="button" id="loadBtn">
                                    加载
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
                <div class="ibox-content">
                    1. 主要适用于，当紧急添加一个任务的时候，由于JobTracker是采取的预加载的形式，会将一批到了执行任务时间的任务加载到内存中，只有当内存中的数据低于一定数量的
                    时候才会去加载(内存中的数据也是有序的)。<br/>
                    2. 所以有可能紧急添加的任务需要排队很久，那么这里提供可以手动通知JobTracker去加载最新的任务到内存中的操作。<br/>
                    3. 要注意一点的是，如果需要紧急执行某一个任务，需要把该任务的执行时间设置早于当前时间（最好能预估一个比其他任务都最早的时间），然后优先级设置小一点(相同执行时间的情况下，数值越小，优先级越大)。<br/>
                    4. 如果选择所有，将会加载所有队列，如果选择指定节点组，那么只会加载指定节点组。
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function () {

        $(document).on("click", "#loadBtn", function () {
            var taskTrackerNodeGroup = $("select[name='taskTrackerNodeGroup']").val();

            $.ajax({
                url: 'api/job-queue/load-add.do',
                type: 'POST',
                dataType: 'json',
                data: {"taskTrackerNodeGroup": taskTrackerNodeGroup},
                success: function (json) {
                    if (json) {
                        swal(json['msg']);
                    }
                }
            });
        });
    });
</script>