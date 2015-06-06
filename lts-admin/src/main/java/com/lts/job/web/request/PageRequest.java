package com.lts.job.web.request;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public class PageRequest {

    private Integer start;

    private Integer limit;

    private Integer pageIndex;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }
}
