package com.lts.monitor.cmd;

import com.lts.cmd.HttpCmdProc;
import com.lts.cmd.HttpCmdRequest;
import com.lts.cmd.HttpCmdResponse;
import com.lts.core.cluster.NodeType;
import com.lts.core.cmd.HttpCmdNames;
import com.lts.core.cmd.HttpCmdParamNames;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.domain.monitor.*;
import com.lts.core.json.JSON;
import com.lts.core.json.TypeReference;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.monitor.MonitorAppContext;

import java.util.List;

/**
 * 监控数据添加CMD
 *
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public class MDataAddHttpCmd implements HttpCmdProc {

    private static final Logger LOGGER = LoggerFactory.getLogger(MDataAddHttpCmd.class);

    private MonitorAppContext appContext;

    public MDataAddHttpCmd(MonitorAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String nodeIdentity() {
        return appContext.getConfig().getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_ADD_M_DATA;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        String mNodeJson = request.getParam(HttpCmdParamNames.M_NODE);
        if (StringUtils.isEmpty(mNodeJson)) {
            return HttpCmdResponse.newResponse(false, "mData is empty");
        }
        MNode mNode = JSON.parse(mNodeJson, new TypeReference<MNode>() {
        }.getType());

        HttpCmdResponse response = paramCheck(mNode);
        if (response != null) {
            return response;
        }

        String mDataJson = request.getParam(HttpCmdParamNames.M_DATA);
        if (StringUtils.isEmpty(mDataJson)) {
            return HttpCmdResponse.newResponse(false, "mData is empty");
        }
        try {
            assert mNode != null;
            List<MData> mDatas = getMDataList(mNode.getNodeType(), mDataJson);
            appContext.getMDataSrv().addMDatas(mNode, mDatas);
        } catch (Exception e) {
            LOGGER.error("Add Monitor Data error: " + JSON.toJSONString(request), e);
            return HttpCmdResponse.newResponse(false, "Add Monitor Data error: " + e.getMessage());
        }

        LOGGER.info("Add Monitor Data success, mNode=" + mNodeJson + ", mData=" + mDataJson);

        return HttpCmdResponse.newResponse(true, "Add Monitor Data success");
    }


    private List<MData> getMDataList(NodeType nodeType, String mDataJson) {
        List<MData> mDatas = null;
        if (NodeType.TASK_TRACKER == nodeType) {
            mDatas = JSON.parse(mDataJson, new TypeReference<List<TaskTrackerMData>>() {
            }.getType());
        } else if (NodeType.JOB_TRACKER == nodeType) {
            mDatas = JSON.parse(mDataJson, new TypeReference<List<JobTrackerMData>>() {
            }.getType());
        } else if (NodeType.JOB_CLIENT == nodeType) {
            mDatas = JSON.parse(mDataJson, new TypeReference<List<JobClientMData>>() {
            }.getType());
        }
        return mDatas;
    }

    private HttpCmdResponse paramCheck(MNode mNode) {
        if (mNode == null) {
            return HttpCmdResponse.newResponse(false, "mNode is empty");
        }

        NodeType nodeType = mNode.getNodeType();
        if (nodeType == null || !(nodeType == NodeType.JOB_CLIENT || nodeType == NodeType.TASK_TRACKER || nodeType == NodeType.JOB_TRACKER)) {
            return HttpCmdResponse.newResponse(false, "nodeType error");
        }
        if (StringUtils.isEmpty(mNode.getNodeGroup())) {
            return HttpCmdResponse.newResponse(false, "nodeGroup is empty");
        }
        if (StringUtils.isEmpty(mNode.getIdentity())) {
            return HttpCmdResponse.newResponse(false, "identity is empty");
        }
        return null;
    }

}
