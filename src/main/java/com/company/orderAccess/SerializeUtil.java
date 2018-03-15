package com.company.orderAccess;

import java.nio.charset.Charset;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.xinwei.nnl.common.util.JsonUtil;

public class SerializeUtil implements RedisSerializer<Object>{
	private final Charset charset;
	static final byte[] EMPTY_ARRAY = new byte[0];
	public SerializeUtil()
	{
		this(Charset.forName("UTF8"));
	}
	public SerializeUtil(Charset charset) {
        // TODO Auto-generated constructor stub
        
        this.charset = charset;
    }
	@Override
	public byte[] serialize(Object t) throws SerializationException {
		   // TODO Auto-generated method stub
        try {
        	byte[] b = null;
        	if(t!=null)
        	{
        		
        		String name = t.getClass().getName();
        		byte[] className = name.getBytes(charset);
        		byte[] objectByte = JsonUtil.toJson(t).getBytes(charset);
        		b = new byte[className.length + objectByte.length + 2];
        	    b[0] = (byte) (className.length & 0xff);    
        	    b[1] = (byte) (className.length >> 8 & 0xff);   
        		
        	}
        	return (t == null ? EMPTY_ARRAY : JsonUtil.toJson(t).getBytes(charset));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		// TODO Auto-generated method stub
        String objectStr = null;
        Object object = null;
        if (bytes == null) {
            return object;
        }
        try {
        	/*
            objectStr = new String(bytes,charset); //byte数组转换为String
            JsonUtil.fromJson(jsonStr, type)
            JSONObject jsonObject = JSONObject.fromObject(objectStr); //String转化为JSONObject
            object = jsonObject;  //返回的是JSONObject类型  取数据时候需要再次转换一下
        	*/
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return object;
	}

}
