package com.github.ltsopensource.admin.request;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public class PaginationReq {

    private Integer start = 0;

    private Integer limit = 10;

    private String field;

    private String direction;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

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

}
