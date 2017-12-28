package com.june.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
/**
 * redis操作
 * @author wuchaoqun
 *
 */
public class RedisUtil {
	private static int TIMEOUT = 10000;
	private static JedisPool jedisPool = null;
	static{
		InputStream in = RedisUtil.class.getClassLoader().getResourceAsStream("redis.properties");
		Properties prop = new Properties();
		try {
			prop.load(in);
			String addr = prop.getProperty("redis.addr");
			String port = prop.getProperty("redis.port");
			String auth = prop.getProperty("redis.auth");
			String maxTotal  = prop.getProperty("redis.maxTotal");
			String maxIdel   = prop.getProperty("redis.maxIdel");
            JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxTotal(Integer.parseInt(maxTotal));
			config.setMaxIdle(Integer.parseInt(maxIdel));
			config.setTestOnBorrow(true);
            jedisPool = new JedisPool(config, addr, Integer.parseInt(port), TIMEOUT,auth);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("redis配置出错");
		}
	}
	
    /**
     * 获取Jedis实例
     * @return jedis
     */
    public static Jedis getJedis() {
        return jedisPool.getResource();
    }   
    
    /**
     * 赋值
     * @param key
     * @param value
     */
    public static void set(String key,String value) {
    	Jedis jedis = jedisPool.getResource();
    	jedis.set(key, value);
    	jedis.close();
    }
    
    /**
     * 取值
     * @param key
     * @param value
     */
    public static String get(String key) {
    	Jedis jedis = jedisPool.getResource();
    	String value = jedis.get(key);   	
    	jedis.close();
    	return value;
    } 
}