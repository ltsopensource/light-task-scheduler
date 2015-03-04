import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.JobSubmitRequest;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.remoting.RemotingClient;
import com.lts.job.remoting.RemotingServer;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.exception.RemotingConnectException;
import com.lts.job.remoting.exception.RemotingSendRequestException;
import com.lts.job.remoting.exception.RemotingTimeoutException;
import com.lts.job.remoting.netty.*;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.remoting.protocol.RemotingProtos;
import com.lts.job.remoting.protocol.RemotingSerializable;
import com.lts.job.tracker.processor.RemotingDispatcher;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class RemotingTest {

    private static final Logger logger = LoggerFactory.getLogger(RemotingTest.class);

    @Test
    public void test_processor() throws IOException, InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {

        NettyServerConfig serverConfig = new NettyServerConfig();
        RemotingServer remotingServer = new NettyRemotingServer(serverConfig);

        try {
            remotingServer.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        remotingServer.registerDefaultProcessor(new RemotingDispatcher(new RemotingServerDelegate(remotingServer)),
                Executors.newCachedThreadPool());

        System.in.read();
    }

    @Test
    public void test_processor2() throws IOException {

        NettyClientConfig clientConfig = new NettyClientConfig();
        RemotingClient client = new NettyRemotingClient(clientConfig);
        client.start();

        client.registerProcessor(RemotingProtos.ResponseCode.SUCCESS.code(), new NettyRequestProcessor() {
            @Override
            public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {
                System.out.println("client receive server request = " + request);
                request.setRemark("client remark");
                return request;
            }
        }, Executors.newCachedThreadPool());


        JobSubmitRequest header = new JobSubmitRequest();
        header.setNodeType(NodeType.CLIENT.name());
        header.setNodeGroup("CLIENT_GROUP");
        header.putExtParam("测试", "好的");

        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.HEART_BEAT.code(), header);
        RemotingCommand response = null;
        try {
            response = client.invokeSync("127.0.0.1:8888", request, 1000 * 3);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } catch (RemotingConnectException e) {
            logger.error(e.getMessage(), e);
        } catch (RemotingSendRequestException e) {
            logger.error(e.getMessage(), e);
        } catch (RemotingTimeoutException e) {
            e.printStackTrace();
        }
        System.out.println("invoke result = " + response);
        try {
            response = client.invokeSync("127.0.0.1:8888", request, 1000 * 3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemotingConnectException e) {
            e.printStackTrace();
        } catch (RemotingSendRequestException e) {
            e.printStackTrace();
        } catch (RemotingTimeoutException e) {
            e.printStackTrace();
        }


        System.out.println("invoke result = " + response);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            response = client.invokeSync("127.0.0.1:8888", request, 1000 * 3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemotingConnectException e) {
            e.printStackTrace();
        } catch (RemotingSendRequestException e) {
            e.printStackTrace();
        } catch (RemotingTimeoutException e) {
            e.printStackTrace();
        }


        System.out.println("invoke result = " + response);


        System.in.read();
    }


    @Test
    public void testJSON() {

        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("111", "333");

        byte[] json = RemotingSerializable.encode(map2);

        System.out.println(new String(json));

        Map<String, Object> map = RemotingSerializable.decode(json, Map.class);

        System.out.println(map);


        JobSubmitRequest header = new JobSubmitRequest();
        header.setNodeGroup("CLIENT_GROUP");
        header.setNodeType(NodeType.CLIENT.name());
        header.putExtParam("测试", "好的");

        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.HEART_BEAT.code(), header);

        byte[] bytes = RemotingSerializable.encode(request);

        System.out.println(JSONObject.toJSONString(request, false));

        System.out.println(new String(bytes));
    }

    @Test
    public void testJSON2() {

        Boolean json = false;
        Double d = 1d;
        System.out.println(JSON.toJSONString(d, false));
    }


}
