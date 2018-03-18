package com.aas.core.config;

import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;


public class RedisHandler {

  /**
   * Initialize Redis connection.
   */

  public static RedissonClient getCache() {
    return redisson;
  }

  private static RedissonClient redisson;

  private static RMapCache<String, String> redisMap;


  /**
   * Static block to initialize Redis client and establish connection.
   */
  public static void initialize(String endpoint) {
    try {
      Config config = new Config();
      config.useSingleServer().setAddress(endpoint);
      config.setCodec(new StringCodec());
      redisson = Redisson.create(config);
      redisMap = getCache().getMapCache("session_store");
    } catch (Exception ex) {
      throw new RuntimeException("redis.error");
    }
  }

  /**
   * Static block to initialize Redis client with AWS ElasicCache and establish connection.
   */
  public static void initialize() {
    try {
      redisson = RedisDiscovery.initializeRedisson();
    } catch (Exception ex) {
      throw new RuntimeException("redis.error");
    }
  }

  /**
   * Add values to key.
   *
   * @param name    the key
   * @param tokenId the set of values
   */
  public static void setValue(String name, String tokenId, String value, long expiration) {
    redisMap.put(name + ":" + tokenId, value, expiration, TimeUnit.SECONDS);
  }

  /**
   * Checks if name:tokenId exists in cache.
   *
   * @param name    the key
   * @param tokenId the value
   * @return true is exists, false otherwise
   */
  public static boolean containsEntry(String name, String tokenId) {
    return redisMap.containsKey(name + ":" + tokenId);
  }

  /**
   * Entry name:tokenId from Redis set
   *
   * @param name    the name
   * @param tokenId the tokenId
   * @return true if delete is successful
   */
  public static Object deleteEntry(String name, String tokenId) {
    return redisMap.remove(name + ":" + tokenId);
  }

  public static void setCredentials(String name, String tokenId, String credentials) {
    redisMap.put(name + ":" + tokenId, credentials);
  }


  public static void flushMap() {
    redisMap.delete();
  }
}

