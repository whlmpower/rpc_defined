package com.whl.rpc_client;

import com.whl.rpc_common.RpcDecoder;
import com.whl.rpc_common.RpcEncoder;
import com.whl.rpc_common.RpcRequest;
import com.whl.rpc_common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 框架RPC客户端，用于发送RPC请求
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse>{

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RpcClient.class);

    private String host;
    private int port;

    private RpcResponse rpcResponse;

    private final Object obj = new Object();

    public RpcClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    public RpcResponse send(RpcRequest request) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class)) // OUT-1
                                    .addLast(new RpcDecoder(RpcResponse.class)) // IN-1
                                    .addLast(RpcClient.this); //IN-2
                        }
                    }).option(ChannelOption.SO_KEEPALIVE, true);

            //将request对象写入outbundle处理后发出（即RpcEncoder编码器）
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 用线程等待的方式决定是否关闭连接
            // 其意义是：先在此阻塞，等待获取到服务端的返回后，被唤醒，从而关闭网络连接
            synchronized (obj){
                obj.wait();
            }

            if (rpcResponse != null){
                future.channel().closeFuture().sync();
            }
            return rpcResponse;

        }finally {
            group.shutdownGracefully();
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        this.rpcResponse = response;

        synchronized (obj){
            obj.notifyAll();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }
}
