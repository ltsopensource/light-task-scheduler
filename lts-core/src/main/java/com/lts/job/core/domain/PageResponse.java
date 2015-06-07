package com.lts.job.core.domain;

import java.util.List;

/**
 * Created by hugui on 6/7/15.
 */
public class PageResponse<T> {

    private int results = 0;

    private List<T> rows;

    public PageResponse() {

    }

    public PageResponse(int results, List<T> rows) {
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
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
