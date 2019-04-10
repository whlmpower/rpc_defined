package com.whl.rpc_common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    // 构造函数传入需要序列化的class
    public RpcEncoder(Class<?> genericClass){
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object inob, ByteBuf out) throws Exception {

        if (genericClass.isInstance(inob)){
            byte[] data = SerializationUtil.serialize(inob);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
