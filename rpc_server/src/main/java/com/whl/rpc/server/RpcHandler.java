package com.whl.rpc.server;

import com.whl.rpc_common.RpcRequest;
import com.whl.rpc_common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    /**
     * 接收消息， 处理消息，  返回结果
     *
     * @param channelHandlerContext
     * @param request
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest request) throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setResponseId(request.getRequestId());
        try{
            // 根据request 使用反射处理具体的业务逻辑调用
            Object result = handle(request);
            rpcResponse.setResult(result);

        }catch (Exception e){
            rpcResponse.setError(e);
        }
        // 写入到outbundle 进行下一步处理，Encoder 编码，最终发送到channel，返回给客户端
        channelHandlerContext.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 通过反射的方式来完成
     * 根据request来处理具体的业务调用
     * @param request
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object handle(RpcRequest request) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String className = request.getClassName();

        Object serviceBean = handlerMap.get(className);

        String methodName = request.getMethodName();

        Class<?>[] parameterTypes = request.getParameterTypes();

        Object[] parameters = request.getParameters();

        Class<?> forName = Class.forName(className);

        Method method = forName.getMethod(methodName, parameterTypes);

        return method.invoke(serviceBean, parameters);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        LOGGER.error("server caught exception", cause);

        ctx.close();
    }
}
