package com.lts.job.core.registry;

import com.lts.job.core.cluster.Node;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public interface PathParser {

    public Node parse(String path);

    public String getPath(Node node);

}
