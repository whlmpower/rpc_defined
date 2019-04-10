package com.whl.rpc_sample_server;

import com.whl.rpc.server.RpcService;
import com.whl.rpc_simple_interface.HelloService;
import com.whl.rpc_simple_interface.Person;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService{

    @Override
    public String hello(String name) {
        System.out.println("已经调用服务端接口实现，业务处理结果为：");
        System.out.println("Hello! " + name);
        return "Hello! " + name;
    }

    @Override
    public String hello(Person person) {
        System.out.println("已经调用服务端接口实现，业务处理为：");
        System.out.println("Hello! " + person.getName() + " " + person.getAge());
        return "Hello! " + person.getName() + " " + person.getAge();
    }

}
