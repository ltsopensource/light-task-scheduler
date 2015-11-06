package com.lts.core.protocol.command;

import com.lts.core.domain.BizLog;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class BizLogSendRequest extends AbstractRemotingCommandBody {

    private List<BizLog> bizLogs;

    public List<BizLog> getBizLogs() {
        return bizLogs;
    }

    public void setBizLogs(List<BizLog> bizLogs) {
        this.bizLogs = bizLogs;
    }
}
