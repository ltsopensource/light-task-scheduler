package com.github.ltsopensource.admin.web.vo;

import lombok.Builder;
import lombok.Value;

/**
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
@Builder
@Value
public class NodeInfo {

    private String identity;
    private String nodeGroup;
}
