package com.whl.rpc_sample_server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 用户系统服务端启动入口
 * 其意义在于启动springcontext，从而构造框架中的rpcServer
 * 亦即：通过注解扫描，将用户系统中标注了RpcService注解的业务发布到RpcServer中
 */
public class RpcBootstrap {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
