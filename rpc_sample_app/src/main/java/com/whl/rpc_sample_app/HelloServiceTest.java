package com.whl.rpc_sample_app;

import com.whl.rpc_client.RpcProxy;
import com.whl.rpc_simple_interface.HelloService;
import com.whl.rpc_simple_interface.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class HelloServiceTest {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest1(){
        //调用代理的create方法，代理HelloService接口
        HelloService helloService = rpcProxy.create(HelloService.class);

        //调用代理的方法，执行invoke
        String result = helloService.hello("world");
        System.out.println("服务端返回结果：");
        System.out.println(result);
    }

    @Test
    public void helloTest2() {
        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello(new Person("Yong", 100));
        System.out.println("服务端返回结果：");
        System.out.println(result);
    }


}
