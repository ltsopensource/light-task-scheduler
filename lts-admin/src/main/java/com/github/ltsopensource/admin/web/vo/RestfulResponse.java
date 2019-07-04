package com.github.ltsopensource.admin.web.vo;

import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * restful response Robert HG (254963746@qq.com) on 6/5/15.
 */
@Builder
@Value
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
}
