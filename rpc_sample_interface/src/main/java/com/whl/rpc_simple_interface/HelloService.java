package com.whl.rpc_simple_interface;

public interface HelloService {
    String hello(String name);
    String hello(Person person);
}
