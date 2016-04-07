/**
 * 这里封装一些公共的JS
 * @author Robert HG (254963746@qq.com)
 */

/**
 * @type {{}}
 */
var LTS = {
    colFormatter: {},
    ReExp: {
        time: /^\d{4}-\d{1,2}-\d{1,2} \d{1,2}:\d{1,2}:\d{1,2}$/,         // yyyy-MM-dd HH:mm:ss
        number: /^\d+$/              // 正整数
    }
};

/**
 * 将JSON对象转为字符串
 */
LTS.colFormatter.stringifyJSON = function (v) {
    return v ? JSON.stringify(v) : v;
};

LTS.colFormatter.needFeedbackLabel = function (v) {
    return v ? "需要" : "不需要";
};

LTS.colFormatter.jobTypeFormat = function (v) {
    if (v == 'CRON') {
        return "Cron任务";
    } else if (v == "REPEAT") {
        return "Repeat任务";
    } else if (v == 'REAL_TIME') {
        return "实时任务";
    } else if (v == 'TRIGGER_TIME') {
        return "定时任务";
    }
    return v;
};

LTS.colFormatter.formatRelyOnPrevCycle = function (v) {
    return v ? "依赖" : "不依赖";
};

LTS.colFormatter.formatGroup = function (v, row) {
    if (row.nodeType == 'JOB_CLIENT' || row.nodeType == 'TASK_TRACKER') {
        return v;
    } else {
        return "";
    }
};

LTS.colFormatter.formatRetryTimes = function (v, row) {
    return row['retryTimes'] + "/" + row['maxRetryTimes'];
};

LTS.colFormatter.repeatIntervalFormat = function (v, row) {
    if (!row['repeatInterval']) {
        return "";
    }
    return row['repeatInterval'] + "ms";
};

LTS.colFormatter.repeatCountFormat = function (v, row) {
    if (!row['repeatInterval']) {
        return "";
    }
    if (row['repeatCount'] == -1) {
        return row['repeatedCount'] + '/(无限)';
    }
    return row['repeatedCount'] + '/' + (row['repeatCount'])
};

template.defaults.escape = false; // 关闭转移功能
template.helper('dateFormat', function (date, format) {
    if (!date) {
        return "";
    }
    return DateUtil.format(date, format);
});

template.helper('format', function (v, colFormatter, row) {
    var formatterFn = LTS.colFormatter[colFormatter];
    return formatterFn ? formatterFn(v, row) : obj;
});

/**
 * 封装的分页表格
 */
function LtsTable(options) {
    this.cachedParams = {};

    //var defaultOpts = {
    //    url: '',
    //    templateId: '',
    //    pageSize: 10,
    //    container: null
    //};

    this.container = options.container;
    this.pageSize = options.pageSize || 10;
    this.templateId = options.templateId;
    this.url = options.url;

    var _this = this;

    _this.renderEmpty = function () {
        _this.render({}, 0, {}, 1);
    };

    _this.render = function (rows, results, params, curPage) {
        var html = template(_this.templateId, {rows: rows, results: results, pageSize: _this.pageSize});
        _this.container.html(html);
        _this.container.children('table').footable();

        if (results == 0) results = 1;
        _this.container.find(".pagination-sm").twbsPagination({
            totalPages: (results % _this.pageSize == 0) ? results / _this.pageSize : results / _this.pageSize + 1,
            visiblePages: 7,
            startPage: curPage,
            first: '«',
            prev: '‹',
            next: '›',
            last: '»',
            onPageClick: function (event, page) {
                _this.post(_this.cachedParams, page);
            }
        });
    };

    _this.post = function (params, curPage) {
        params['start'] = (curPage - 1) * _this.pageSize;
        params['limit'] = _this.pageSize;
        _this.showLoading();
        $.ajax({
            url: _this.url,
            type: 'POST',
            dataType: 'json',
            data: params,
            success: function (json) {
                if (json && json.success) {
                    _this.cachedParams = params;
                    var results = json['results'];
                    var rows = json['rows'];
                    _this.render(rows, results, params, curPage);
                } else {
                    if (json) {
                        swal(json['msg']);
                    }
                }
            },
            complete: function () {
                _this.hideLoading();
            }
        });
    };

    _this.showLoading = function () {
        var loading = '<div id="loading" style="width:50px;position:absolute;left: -100px;"><img src="assets/img/loading.gif" style="width:30px;"/></div>';
        var offset = _this.container.offset();
        var left = _this.container.width() / 2 + offset.left;
        if ($("#loading").length == 0) {
            $('body').append(loading);
        }
        $("#loading").offset({top: offset.top + _this.container.height() / 2, left: left});
    };

    _this.hideLoading = function () {
        $("#loading").offset({
            left: -100
        });
    };
}

jQuery.fn.extend({
    ltsTable: function (options) {
        var container = $(this);
        var opts = {};
        $.extend(opts, options, {container: container});
        return new LtsTable(opts);
    }
});


if (window.Highcharts) {
    Highcharts.setOptions({
        global: {
            useUTC: false
        },
        lang: {
            months: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'],
            shortMonths: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'],
            weekdays: ['星期天', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'],
            resetZoom: '查看全图',
            resetZoomTitle: '查看全图',
            downloadPNG: '下载PNG',
            downloadJPEG: '下载JPEG',
            downloadPDF: '下载PDF',
            downloadSVG: '下载SVG',
            exportButtonTitle: '导出成图片',
            printButtonTitle: '打印图表',
            loading: '数据加载中，请稍候...'
        }
    });
}

function showLineChart(chartId, title, yTitle, series, colors, valueSuffix) {
    if (!colors) {
        colors = ['#FCAF64', '#1bd0dc', '#f9b700', '#eb6100', '#eb6877', '#a98fc2', '#9dd30d', '#1c95bd', '#9999ff', '#5674b9', '#009944'];
    }

    $(chartId).highcharts({
        chart: {
            zoomType: 'x',
            type: 'spline'
        },
        colors: colors,
        title: {
            text: title
        },
        xAxis: {
            type: 'datetime'
        },
        yAxis: {
            title: {
                text: yTitle
            },
            min: 0,
            plotLines: [{
                value: 0,
                width: 1,
                color: '#808080'
            }]
        },
        tooltip: {
            valueSuffix: valueSuffix || '',
            dateTimeLabelFormats: {
                minute: "%Y-%m-%d %H:%M"
            },
            crosshairs: true,
            shared: true
        },
        plotOptions: {
            series: {
                fillOpacity: 0.1,
                shadow: false,
                marker: {
                    enabled: false,
                    radius: 4,
                    fillColor: null,
                    lineWidth: 2,
                    lineColor: '#FFFFFF',
                    states: {hover: {enabled: true}}
                }
            }
        },
        credits: {
            enabled: false
        },
        series: series
    });
}