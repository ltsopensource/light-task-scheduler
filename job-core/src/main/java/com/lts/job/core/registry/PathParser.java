package com.lts.job.core.registry;

import com.lts.job.core.cluster.Node;

/**
 * Created by hugui on 3/27/15.
 */
public interface PathParser {

    public Node parse(String path);

    public String getPath(Node node);

}
