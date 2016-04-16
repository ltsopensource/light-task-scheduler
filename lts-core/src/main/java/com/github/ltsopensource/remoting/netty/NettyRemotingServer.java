package com.github.ltsopensource.remoting.netty;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.remoting.*;
import com.github.ltsopensource.remoting.common.RemotingHelper;
import com.github.ltsopensource.remoting.exception.RemotingException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.InetSocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 11/3/15.
 */
public class NettyRemotingServer extends AbstractRemotingServer {

    public static final Logger LOGGER = AbstractRemotingServer.LOGGER;

    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup bossSelectorGroup;
    private final EventLoopGroup workerSelectorGroup;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private AppContext appContext;

    public NettyRemotingServer(AppContext appContext, RemotingServerConfig remotingServerConfig) {
        this(remotingServerConfig, null);
        this.appContext = appContext;
    }

    public NettyRemotingServer(RemotingServerConfig remotingServerConfig, final ChannelEventListener channelEventListener) {
        super(remotingServerConfig, channelEventListener);
        this.serverBootstrap = new ServerBootstrap();
        this.bossSelectorGroup = new NioEventLoopGroup(1, new NamedThreadFactory("NettyBossSelectorThread_"));
        this.workerSelectorGroup = new NioEventLoopGroup(remotingServerConfig.getServerSelectorThreads(), new NamedThreadFactory("NettyServerSelectorThread_", true));
    }

    @Override
    protected void serverStart() throws RemotingException {

        NettyLogger.setNettyLoggerFactory();

        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                remotingServerConfig.getServerWorkerThreads(),
                new NamedThreadFactory("NettyServerWorkerThread_")
        );

        final NettyCodecFactory nettyCodecFactory = new NettyCodecFactory(appContext, getCodec());

        this.serverBootstrap.group(this.bossSelectorGroup, this.workerSelectorGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 65536)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(new InetSocketAddress(this.remotingServerConfig.getListenPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup,
                                nettyCodecFactory.getEncoder(),
                                nettyCodecFactory.getDecoder(),
                                new IdleStateHandler(remotingServerConfig.getReaderIdleTimeSeconds(),
                                        remotingServerConfig.getWriterIdleTimeSeconds(), remotingServerConfig.getServerChannelMaxIdleTimeSeconds()),//
                                new NettyConnectManageHandler(), //
                                new NettyServerHandler());
                    }
                });

        try {
            this.serverBootstrap.bind().sync();
        } catch (InterruptedException e) {
            throw new RemotingException("Start Netty server bootstrap error", e);
        }
    }

    @Override
    protected void serverShutdown() throws RemotingException{

        this.bossSelectorGroup.shutdownGracefully();
        this.workerSelectorGroup.shutdownGracefully();

        if (this.defaultEventExecutorGroup != null) {
            this.defaultEventExecutorGroup.shutdownGracefully();
        }
    }

    class NettyServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
            processMessageReceived(new NettyChannel(ctx), msg);
        }
    }

    class NettyConnectManageHandler extends ChannelDuplexHandler {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(new NettyChannel(ctx));
            LOGGER.info("SERVER : channelRegistered {}", remoteAddress);
            super.channelRegistered(ctx);
        }


        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(new NettyChannel(ctx));
            LOGGER.info("SERVER : channelUnregistered, the channel[{}]", remoteAddress);
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            com.github.ltsopensource.remoting.Channel channel = new NettyChannel(ctx);
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("SERVER: channelActive, the channel[{}]", remoteAddress);
            super.channelActive(ctx);

            if (channelEventListener != null) {
                putRemotingEvent(new RemotingEvent(RemotingEventType.CONNECT, remoteAddress, channel));
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            com.github.ltsopensource.remoting.Channel channel = new NettyChannel(ctx);

            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("SERVER: channelInactive, the channel[{}]", remoteAddress);
            super.channelInactive(ctx);

            if (channelEventListener != null) {
                putRemotingEvent(new RemotingEvent(RemotingEventType.CLOSE, remoteAddress, channel));
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;

                com.github.ltsopensource.remoting.Channel channel = new NettyChannel(ctx);

                final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);

                if (event.state().equals(IdleState.ALL_IDLE)) {
                    LOGGER.warn("SERVER: IDLE [{}]", remoteAddress);
                    RemotingHelper.closeChannel(channel);
                }

                if (channelEventListener != null) {
                    RemotingEventType remotingEventType = RemotingEventType.valueOf(event.state().name());
                    putRemotingEvent(new RemotingEvent(remotingEventType,
                            remoteAddress, channel));
                }
            }

            ctx.fireUserEventTriggered(evt);
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

            com.github.ltsopensource.remoting.Channel channel = new NettyChannel(ctx);

            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
            LOGGER.warn("SERVER: exceptionCaught {}", remoteAddress, cause);

            if (channelEventListener != null) {
                putRemotingEvent(new RemotingEvent(RemotingEventType.EXCEPTION, remoteAddress, channel));
            }

            RemotingHelper.closeChannel(channel);
        }
    }

}
