package com.whl.rpc_client;

import com.whl.rpc_common.RpcRequest;
import com.whl.rpc_common.RpcResponse;
import com.whl.rpc_registry.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

public class RpcProxy {

    private String serverAddress;

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress){
        this.serverAddress = serverAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //创建RpcRequest，封装被代理类的属性
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        //查找声明这个业务方法的接口名称
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        request.setMethodName(method.getName());
                        //查找服务
                        if (serviceDiscovery != null){
                            serverAddress = serviceDiscovery.discover();
                        }

                        String[] array = serverAddress.split(":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        //通过netty发送服务器请求
                        RpcClient client = new RpcClient(host, port);
                        RpcResponse response = client.send(request);
                        if (response.getError() != null){
                            throw response.getError();
                        }else {
                            return response.getResult();
                        }

                    }
                });
    }
}
