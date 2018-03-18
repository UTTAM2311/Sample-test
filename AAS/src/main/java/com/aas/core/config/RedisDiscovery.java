package com.aas.core.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.AmazonElastiCacheClient;
import com.amazonaws.services.elasticache.model.*;
import lombok.Getter;
import lombok.Setter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class RedisDiscovery {

  @Value("${AWS_REGION}")
  @Getter
  @Setter
  private static String awsRegion;

  private RedisDiscovery() {
  }


  /**
   * Get redis cluster urls string [ ].
   *
   * @param elastiCache the elasti cache
   * @return the string [ ]
   */
  public static String[] getRedisClusterUrls(AmazonElastiCache elastiCache) {
    List<CacheCluster> cacheClusters;
    try {
      DescribeCacheClustersRequest dccRequest = new DescribeCacheClustersRequest();
      dccRequest.setShowCacheNodeInfo(true);
      DescribeCacheClustersResult clusterResult = elastiCache.describeCacheClusters(dccRequest);
      cacheClusters = clusterResult.getCacheClusters();
    } catch (AmazonElastiCacheException e) {

      throw new RuntimeException("Exception in redis");
    }
    return cacheClusters.stream()
        .flatMap((Function<CacheCluster, Stream<CacheNode>>) cacheCluster -> cacheCluster
            .getCacheNodes().stream())
        .map(cacheNode -> "redis://" + cacheNode.getEndpoint().getAddress() + ":"
            + cacheNode.getEndpoint().getPort())
        .toArray(String[]::new);
  }


  /**
   * Initialize redisson redisson client.
   *
   * @return the redisson client
   */

  public static RedissonClient initializeRedisson() {
    try {
      DefaultAWSCredentialsProviderChain awsCredentials = DefaultAWSCredentialsProviderChain.getInstance();
      String[] redisHosts = getRedisClusterUrls(getAmazonElastiCache(awsCredentials, awsRegion));

      Config config = new Config();
      if (redisHosts.length == 1) {
        config.useSingleServer().setAddress(redisHosts[0]);
      } else {
        config.useClusterServers().setScanInterval(2000).addNodeAddress(redisHosts);
      }
      config.setCodec(StringCodec.INSTANCE);
      return Redisson.create(config);
    } catch (Exception e) {
      throw new RuntimeException("Exception in redis");
    }
  }

  /**
   * Gets amazon elastic cache.
   *
   * @param credentialsProvider the credentials provider
   * @param region              the region
   * @return the amazon elastic cache
   */
  public static AmazonElastiCache getAmazonElastiCache(AWSCredentialsProvider credentialsProvider,
                                                       String region) {
    return AmazonElastiCacheClient.builder().withCredentials(credentialsProvider).withRegion(region)
        .build();
  }


}