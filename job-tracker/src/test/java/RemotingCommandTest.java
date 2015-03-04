import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.JobSubmitRequest;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.protocol.RemotingCommand;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class RemotingCommandTest {

    @Test
    public void test() throws RemotingCommandException {
        JobSubmitRequest header = new JobSubmitRequest();
        header.setNodeGroup("CLIENT");
        header.setNodeType(NodeType.CLIENT.name());
        header.putExtParam("测试", "好的");

        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.HEART_BEAT.code(), header);


        ByteBuffer buffer = request.encode();

        RemotingCommand command = RemotingCommand.decode(buffer);

        System.out.println(command);

//        String json = JSON.toJSONString(request);
//
//        RemotingCommand remotingCommand = JSON.parseObject(json, RemotingCommand.class);

    }
}
