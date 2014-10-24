package com.lts.job.remoting;


import com.lts.job.remoting.annotation.Nullable;
import com.lts.job.remoting.exception.*;
import com.lts.job.remoting.netty.*;
import com.lts.job.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;

/**
 * @author Robert HG (254963746@qq.com) on 7/21/14.
 */
public class NettyRPCTest {

    private static final Logger logger = LoggerFactory.getLogger(NettyRPCTest.class);

    public static RemotingClient createRemotingClient() {
        NettyClientConfig config = new NettyClientConfig();
        RemotingClient client = new NettyRemotingClient(config);
        client.start();
        return client;
    }


    public static RemotingServer createRemotingServer() throws InterruptedException {
        NettyServerConfig config = new NettyServerConfig();
        RemotingServer remotingServer = new NettyRemotingServer(config);
        remotingServer.registerProcessor(0, new NettyRequestProcessor() {
            private int i = 0;


            @Override
            public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
                System.out.println("processRequest=" + request + " " + (i++));
                request.setRemark("hello, I am respponse " + ctx.channel().remoteAddress());
                return request;
            }
        }, Executors.newCachedThreadPool());
        remotingServer.start();
        return remotingServer;
    }


    @Test
    public void test_RPC_Sync() throws InterruptedException, RemotingConnectException,
            RemotingSendRequestException, RemotingTimeoutException {
        RemotingServer server = createRemotingServer();
        RemotingClient client = createRemotingClient();

        for (int i = 0; i < 100; i++) {
            TestRequestHeader requestHeader = new TestRequestHeader();
            requestHeader.setCount(i);
            requestHeader.setMessageTitle("HelloMessageTitle");
            RemotingCommand request = RemotingCommand.createRequestCommand(0, requestHeader);
            RemotingCommand response = client.invokeSync("127.0.0.1:8888", request, 1000 * 3000);
            System.out.println("invoke result = " + response);
            assertTrue(response != null);
        }

        client.shutdown();
        server.shutdown();
        System.out.println("-----------------------------------------------------------------");
    }


    @Test
    public void test_RPC_Oneway() throws InterruptedException, RemotingConnectException,
            RemotingTimeoutException, RemotingTooMuchRequestException, RemotingSendRequestException {
        RemotingServer server = createRemotingServer();
        RemotingClient client = createRemotingClient();

        for (int i = 0; i < 100; i++) {
            RemotingCommand request = RemotingCommand.createRequestCommand(0, null);
            request.setRemark(String.valueOf(i));
            client.invokeOneway("127.0.0.1:8888", request, 1000 * 3);
        }

        client.shutdown();
        server.shutdown();
        System.out.println("-----------------------------------------------------------------");
    }


    @Test
    public void test_RPC_Async() throws InterruptedException, RemotingConnectException,
            RemotingTimeoutException, RemotingTooMuchRequestException, RemotingSendRequestException {
        RemotingServer server = createRemotingServer();
        RemotingClient client = createRemotingClient();

        for (int i = 0; i < 100; i++) {
            RemotingCommand request = RemotingCommand.createRequestCommand(0, null);
            request.setRemark(String.valueOf(i));
            client.invokeAsync("127.0.0.1:8888", request, 1000 * 3, new InvokeCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    System.out.println(responseFuture.getResponseCommand());
                }
            });
        }

        Thread.sleep(1000 * 3);

        client.shutdown();
        server.shutdown();
        System.out.println("-----------------------------------------------------------------");
    }


    @Test
    public void test_server_call_client() throws InterruptedException, RemotingConnectException,
            RemotingSendRequestException, RemotingTimeoutException {
        final RemotingServer server = createRemotingServer();
        final RemotingClient client = createRemotingClient();

        server.registerProcessor(0, new NettyRequestProcessor() {
            @Override
            public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
                try {
                    return server.invokeSync(ctx.channel(), request, 1000 * 10);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                } catch (RemotingSendRequestException e) {
                    logger.error(e.getMessage(), e);
                } catch (RemotingTimeoutException e) {
                    logger.error(e.getMessage(), e);
                }

                return null;
            }
        }, Executors.newCachedThreadPool());

        client.registerProcessor(0, new NettyRequestProcessor() {
            @Override
            public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
                System.out.println("client receive server request = " + request);
                request.setRemark("client remark");
                return request;
            }
        }, Executors.newCachedThreadPool());

        for (int i = 0; i < 3; i++) {
            RemotingCommand request = RemotingCommand.createRequestCommand(0, null);
            RemotingCommand response = client.invokeSync("127.0.0.1:8888", request, 1000 * 3);
            System.out.println("invoke result = " + response);
            assertTrue(response != null);
        }

        client.shutdown();
        server.shutdown();
        System.out.println("-----------------------------------------------------------------");
    }

}


class TestRequestHeader implements CommandBody {
    @Nullable
    private Integer count;

    @Nullable
    private String messageTitle;


    public Integer getCount() {
        return count;
    }


    public void setCount(Integer count) {
        this.count = count;
    }


    public String getMessageTitle() {
        return messageTitle;
    }


    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    @Override
    public void checkFields() throws RemotingCommandFieldCheckException {

    }
}


class TestResponseHeader implements CommandBody {
    @Nullable
    private Integer count;

    @Nullable
    private String messageTitle;


    public Integer getCount() {
        return count;
    }


    public void setCount(Integer count) {
        this.count = count;
    }


    public String getMessageTitle() {
        return messageTitle;
    }


    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    @Override
    public void checkFields() throws RemotingCommandFieldCheckException {

    }
}

