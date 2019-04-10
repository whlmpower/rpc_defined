package com.whl.rpc_common;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializationUtil {
    private static Map<Class<?>, Schema> cachedSchema = new ConcurrentHashMap<>();

    private static Objenesis objenesis = new ObjenesisStd(true);

    private SerializationUtil(){

    }


    /**
     * 获取缓存schema
     * @param cls
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls){
        Schema<T> schema = cachedSchema.get(cls);
        if (schema == null){
            schema = RuntimeSchema.getSchema(cls);
            if (schema != null){
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj){
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try{
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(), e);
        }finally {
            buffer.clear();
        }

    }

    /**
     * 反序列化 （字节数组 -> 对象）
     * @param data
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data, Class<T> cls){
        try{
            /**
             * 如果一个类没有参数为空的构造方法，
             * 使用newInstance 方法试图得到一个实例对象的时候回抛出异常的
             * 通过ObjenesisStd可以完美解决这个问题
             */
            T message = objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
//            T message = schema.newMessage(); 效果等同于上述message newInstance
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        }catch (Exception e){
            throw  new IllegalStateException(e.getMessage(), e);
        }
    }

}
