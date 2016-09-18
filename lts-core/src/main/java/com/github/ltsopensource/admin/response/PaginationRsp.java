package com.github.ltsopensource.admin.response;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public class PaginationRsp<T> {

    private int results = 0;

    private List<T> rows;

    public PaginationRsp() {

    }

    public PaginationRsp(int results, List<T> rows) {
        this.results = results;
        this.rows = rows;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public List<T> getRows() {
        return rows == null ? new ArrayList<T>(0) : rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
