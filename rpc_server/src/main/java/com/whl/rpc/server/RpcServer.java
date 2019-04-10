package com.whl.rpc.server;


import com.whl.rpc_common.RpcDecoder;
import com.whl.rpc_common.RpcEncoder;
import com.whl.rpc_common.RpcRequest;
import com.whl.rpc_common.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


import java.util.HashMap;
import java.util.Map;

import com.whl.rpc_registry.ServiceRegistry;
/**
 * 框架的RPC服务器（将用户系统的业务类发布为RPC服务）
 * 使用时可以由用户通过Spring-bean的方式注入到用户的业务系统中
 * 由于本类实现了ApplicationContextAware InitializingBean
 * Spring 构造本对象的时候会调用setApplicationContext()方法，从而
 * 可以在方法中通过自定义注解获得用户的业务接口和实现
 * 还会通过afterPropertiesSet() 方法，在方法中启动netty服务器
 */
public class RpcServer implements ApplicationContextAware, InitializingBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serverAddress;

    private ServiceRegistry serviceRegistry;

    public RpcServer(String serverAddress){
        this.serverAddress = serverAddress;
    }

    //服务器绑定的地址和端口号由Spring构造本类的时候，从配置文件中传递进来
    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry){
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    private Map<String, Object> handlerMap = new HashMap<>();

    /**
     * 启动netty服务，绑定handle流水线
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RpcDecoder(RpcRequest.class))//注册解码 IN-1
                                .addLast(new RpcEncoder(RpcResponse.class))//注册编码 OUT
                                .addLast(new RpcHandler(handlerMap)); //注册RpcHandler IN-2

                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);// 这个还不知道啥意思呢

        String[] array = serverAddress.split(":");
        String host = array[0];

        int port = Integer.parseInt(array[1]);

        ChannelFuture future = bootstrap.bind(host, port).sync();

        LOGGER.debug("server started on port {}", port);

        if (serviceRegistry != null){
            serviceRegistry.register(serverAddress);
        }
        future.channel().closeFuture().sync();
    }

    /**
     * 通过注解，获取标注了rpc服务注解的业务类的
     * 接口名称及impl对象，将放置于handleMap中
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext
                .getBeansWithAnnotation(RpcService.class);

        for (Object serviceBean : serviceBeanMap.values()) {

            String interfaceName = serviceBean.getClass()
                    .getAnnotation(RpcService.class).value().getName();//获取到注解上的接口名称
            handlerMap.put(interfaceName, serviceBean);
        }
    }
}
