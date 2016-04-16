package com.github.ltsopensource.tasktracker.processor;

import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.RemotingProcessor;
import com.github.ltsopensource.remoting.exception.RemotingCommandException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.remoting.protocol.RemotingProtos;
import com.github.ltsopensource.tasktracker.domain.TaskTrackerAppContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         task tracker 总的处理器, 每一种命令对应不同的处理器
 */
public class RemotingDispatcher extends AbstractProcessor {

    private final Map<JobProtos.RequestCode, RemotingProcessor> processors = new HashMap<JobProtos.RequestCode, RemotingProcessor>();

    public RemotingDispatcher(TaskTrackerAppContext appContext) {
        super(appContext);
        processors.put(JobProtos.RequestCode.PUSH_JOB, new JobPushProcessor(appContext));
        processors.put(JobProtos.RequestCode.JOB_ASK, new JobAskProcessor(appContext));
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {

        JobProtos.RequestCode code = JobProtos.RequestCode.valueOf(request.getCode());
        RemotingProcessor processor = processors.get(code);
        if (processor == null) {
            return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(),
                    "request code not supported!");
        }
        return processor.processRequest(channel, request);
    }

}
