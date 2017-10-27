package com.company.orderAccess;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Cluster;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

//@Configuration
//@EnableConfigurationProperties(RedisProperties.class)
/*
class Config {

	private final RedisProperties properties;
	private final RedisClusterConfiguration clusterConfiguration;

    //List<String> clusterNodes = Arrays.asList("101.200.166.163:9000","101.200.166.163:9001", "101.200.166.163:9002","101.200.166.163:9003","101.200.166.163:9004","101.200.166.163:9005");

    public Config(RedisProperties properties,
			
			ObjectProvider<RedisClusterConfiguration> clusterConfiguration) {
		this.properties = properties;
		
		this.clusterConfiguration = clusterConfiguration.getIfAvailable();
	}
    private JedisPoolConfig jedisPoolConfig() {
		JedisPoolConfig config = new JedisPoolConfig();
		RedisProperties.Pool props = this.properties.getPool();
		config.setMaxTotal(props.getMaxActive());
		config.setMaxIdle(props.getMaxIdle());
		config.setMinIdle(props.getMinIdle());
		config.setMaxWaitMillis(props.getMaxWait());
		return config;
	}
    protected final RedisClusterConfiguration getClusterConfiguration() {
		if (this.clusterConfiguration != null) {
			return this.clusterConfiguration;
		}
		if (this.properties.getCluster() == null) {
			return null;
		}
		Cluster clusterProperties = this.properties.getCluster();
		RedisClusterConfiguration config = new RedisClusterConfiguration(
				clusterProperties.getNodes());

		if (clusterProperties.getMaxRedirects() != null) {
			config.setMaxRedirects(clusterProperties.getMaxRedirects());
		}
		return config;
	}

    private JedisConnectionFactory createJedisConnectionFactory() {
		JedisPoolConfig poolConfig = this.properties.getPool() != null
				? jedisPoolConfig() : new JedisPoolConfig();

		
		if (getClusterConfiguration() != null) {
			return new JedisConnectionFactory(getClusterConfiguration(), poolConfig);
		}
		return new JedisConnectionFactory(poolConfig);
	}

    @Bean
    RedisConnectionFactory connectionFactory() {
    	//if(true)
    	return createJedisConnectionFactory();
    	//RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(clusterNodes);
    	//redisClusterConfiguration.setClusterNodes(clusterNodes);
    	//JedisConnectionFactory jedisConnectionFactory =  new JedisConnectionFactory(redisClusterConfiguration);
    	
    	//return jedisConnectionFactory;
    }

	

   
}*/