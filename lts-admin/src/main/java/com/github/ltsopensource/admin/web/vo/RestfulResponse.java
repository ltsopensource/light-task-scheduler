package com.github.ltsopensource.admin.web.vo;

import java.io.Serializable;
import java.util.List;

/**
 * restful response
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class RestfulResponse implements Serializable {

	private static final long serialVersionUID = -5795089018013798231L;

	private boolean success;

    private String code;

    private String msg;
    /**
     * total recorded
     */
    private int results;
    /**
     * rows
     */
    private List<?> rows;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }
}
