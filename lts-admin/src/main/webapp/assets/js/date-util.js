/**
 * 日期工具类
 * @author hugui
 */
!(function () {

    var DateUtil = {

        IS_SAME_DAY: 0,        // 是同一天
        FORMAT_YMD: 'yyyy-MM-dd',
        FORMAT_YMD_HMS: 'yyyy-MM-dd HH:mm:ss',
        ONE_DAY_TIME: 24 * 60 * 60 * 1000,

        /**
         * 格式化日期(可以传 long string date)
         * @param date  日期
         * @param pattern 格式
         * @returns {*}
         */
        format: function (date, pattern) {
            date = this.parse(date);
            var o = {
                "M+": date.getMonth() + 1,
                "d+": date.getDate(),
                "H+": date.getHours(),
                "m+": date.getMinutes(),
                "s+": date.getSeconds(),
                "q+": Math.floor((date.getMonth() + 3) / 3),
                "S": date.getMilliseconds()
            };
            if (/(y+)/.test(pattern)) {
                pattern = pattern.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
            }
            for (var k in o) {
                if (new RegExp("(" + k + ")").test(pattern)) {
                    pattern = pattern.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
                }
            }
            return pattern;
        },

        formatYMD: function (date) {
            if (!date) {
                return date;
            }
            return this.format(date, this.FORMAT_YMD);
        },

        formatYMDHMD: function (date) {
            if (!date) {
                return date;
            }
            return this.format(date, this.FORMAT_YMD_HMS);
        },

        formatMD: function (date) {
            if(!data){
                return date;
            }
            return this.format(date, 'MM-dd');
        },

        /**
         * 验证是不是正确的日期
         * @param dateStr 字符串类型  yyyy-M-d  yyyy-MM-dd
         */
        isDate: function (dateStr) {
            // 先判断格式上是否正确
            var regDate = /^(\d{4})-(\d{1,2})-(\d{1,2})$/;
            if (!regDate.test(dateStr)) {
                return false;
            }
            // 将年、月、日的值取到数组arr中，其中arr[0]为整个字符串，arr[1]-arr[3]为年、月、日
            var arr = regDate.exec(dateStr);
            // 判断年、月、日的取值范围是否正确
            return this.isMonthAndDateCorrect(arr[1], arr[2], arr[3]);

        },

        /**
         * 判断字符串strDateTime是否为一个正确的日期时间格式：
         * yyyy-M-d H:m:s或yyyy-MM-dd HH:mm:ss
         * 时间采用24小时制
         * @param dateTimeStr
         * @returns {boolean}
         */
        isDateTime: function (dateTimeStr) {
            // 先判断格式上是否正确
            var regDateTime = /^(\d{4})-(\d{1,2})-(\d{1,2}) (\d{1,2}):(\d{1,2}):(\d{1,2})$/;
            if (!regDateTime.test(dateTimeStr))
                return false;

            // 将年、月、日、时、分、秒的值取到数组arr中，其中arr[0]为整个字符串，arr[1]-arr[6]为年、月、日、时、分、秒
            var arr = regDateTime.exec(dateTimeStr);

            // 判断年、月、日的取值范围是否正确
            if (!this.isMonthAndDateCorrect(arr[1], arr[2], arr[3]))
                return false;

            // 判断时、分、秒的取值范围是否正确
            if (arr[4] >= 24)
                return false;
            if (arr[5] >= 60)
                return false;
            if (arr[6] >= 60)
                return false;

            // 正确的返回
            return true;
        },

        /**
         * 判断年、月、日的取值范围是否正确
         * @param nYear
         * @param nMonth
         * @param nDay
         * @returns {boolean}
         */
        isMonthAndDateCorrect: function (nYear, nMonth, nDay) {
            // 月份是否在1-12的范围内，注意如果该字符串不是C#语言的，而是JavaScript的，月份范围为0-11
            if (nMonth > 12 || nMonth <= 0)
                return false;

            // 日是否在1-31的范围内，不是则取值不正确
            if (nDay > 31 || nMonth <= 0)
                return false;

            // 根据月份判断每月最多日数
            var bTrue = false;
            switch (nMonth) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    bTrue = true;    // 大月，由于已判断过nDay的范围在1-31内，因此直接返回true
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    bTrue = (nDay <= 30);    // 小月，如果小于等于30日返回true
                    break;
            }
            if (!bTrue)
                return true;
            // 2月的情况
            // 如果小于等于28天一定正确
            if (nDay <= 28)
                return true;
            // 闰年小于等于29天正确
            if (this.isLeapYear(nYear))
                return (nDay <= 29);
            // 不是闰年，又不小于等于28，返回false
            return false;
        },
        /**
         * 是否为闰年，规则：四年一闰，百年不闰，四百年再闰
         */
        isLeapYear: function (nYear) {
            // 如果不是4的倍数，一定不是闰年
            if (nYear % 4 != 0)
                return false;
            // 是4的倍数，但不是100的倍数，一定是闰年
            if (nYear % 100 != 0)
                return true;
            // 是4和100的倍数，如果又是400的倍数才是闰年
            return (nYear % 400 == 0);
        },

        /**
         * 比较两个时间
         * @param time1
         * @param time2
         * @return
         *  time1 > time2 返回 1
         *  time1 = time2 返回 0
         *  time1 < time2 返回-1
         */
        compare: function (time1, time2) {
            time1 = this.parse(time1);
            time2 = this.parse(time2);
            var l1 = time1.getTime();
            var l2 = time2.getTime();
            if (l1 == l2) {
                return 0;
            }
            return l1 > l2 ? 1 : -1;
        },

        /**
         * 和现在比较时间
         * @param date
         */
        compareWithNow: function (time) {
            return this.compare(time, new Date());
        },

        /**
         * 提交两个日期
         * @param date1
         * @param date2
         */
        compareDate: function (date1, date2) {
            if (date1 == date2) {
                return this.IS_SAME_DAY;
            }
            return this.compare(new Date(date1), new Date(date2));
        },

        /**
         * 得到昨天
         * @returns   日期类型  yyyy-MM-dd
         */
        getYesterday: function () {
            var date = new Date();
            var time = date.getTime();
            time -= this.ONE_DAY_TIME;
            date.setTime(time);
            return this.format(date, this.FORMAT_YMD);
        },

        /**
         * 得到日期
         * @param interval 间隔几天
         */
        getDate: function (date, interval) {
            date = this.parse(date);
            var _date = this.clone(date);
            var time = _date.getTime();
            time += interval * this.ONE_DAY_TIME;
            _date.setTime(time);
            return this.format(_date, this.FORMAT_YMD);
        },

        /**
         * 克隆Date
         * @param date
         * @returns {*}
         */
        clone: function (date) {
            if (date instanceof  Date) {
                var copy = new Date();
                copy.setTime(date.getTime());
                return copy;
            }
            return date;
        },

        /**
         * 转换 yyyy-MM-dd hh:mm:ss 为Date类型
         * @param dateStr
         * @returns {Date}
         */
        parse: function (date) {
            if (date instanceof  Date) {
                return date;
            } else if (typeof date == 'number') {
                return new Date(date);
            } else if (typeof date == 'string') {
                return new Date(Date.parse(date.replace(/-/g, "/")));
            }
            throw new Error("Unable to parse date! Its type isn't supported.");
        },

        /**
         * 得到两个日期相差的天数
         */
        getDateDiffDay: function (startDate, endDate) {
            startDate = this.parse(startDate);
            endDate = this.parse(endDate);
            var diffTime = startDate.getTime() - endDate.getTime();
            return Math.floor(Math.abs(diffTime / this.ONE_DAY_TIME));
        },
        /**
         * 得到某个月的天数
         * @param year
         * @param month
         * @returns {number}
         */
        getDaysInMonth: function (year, month) {
            month++;           // month从0开始的
            if (month == 12) {
                year++;
                month = 0;
            }
            var thisMonth = new Date(year + "/" + (month + 1) + "/1");
            month++;           // 下个月
            if (month == 12) {
                year++;
                month = 0;
            }
            var nextMonth = new Date(year + "/" + (month + 1) + "/1");
            return this.getDateDiffDay(thisMonth, nextMonth);
        },

        /**
         * 得到上个月1号
         */
        getLastMonth: function (year, month) {
            if (month == 0) {     // 1月分
                year--;
                month = 11;
            } else {
                month--;
            }
            return new Date(year, month, 1);
        },

        /**
         * 得到指定日期是当年的第几周
         * @param date
         */
        getYearWeek: function (date) {
            date = this.parse(date);
            var beginDay = new Date(date.getFullYear(), 0, 0);
            var weekNum = Math.floor((date.getTime() - beginDay.getTime()) / (7 * 24 * 60 * 60 * 1000));   // 今年的第多少天
            return weekNum + 1;
        },
        /**
         * 得到给定日期的上一周
         * @param date
         */
        getYearLastWeek: function (date) {
            date = this.parse(date);
            var date2 = this.getDate(date, -7);
            return this.getYearWeek(date2);
        },

        /**
         * 得到当月的一号
         */
        getFirstDayInCurrentMonth: function () {
            var date = new Date();
            var day = new Date(date.getFullYear(), date.getMonth(), 1);
            return this.format(day, this.FORMAT_YMD);
        },

        /**
         * 得到今天的日期
         * @returns {*}
         */
        today: function () {
            return this.format(new Date(), this.FORMAT_YMD);
        }

    };

    // RequireJS && SeaJS
    if (typeof define === 'function') {
        define(function () {
            return DateUtil;
        });

// NodeJS
    } else if (typeof exports !== 'undefined') {
        module.exports = DateUtil;
    } else {
        window.DateUtil = DateUtil;
    }

})();



