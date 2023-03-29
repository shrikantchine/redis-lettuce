package com.shrikantchine;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClusterRedisTest {
    public static void main(String[] args) {
        RedisURI creatorUri = RedisURI.builder()
                .withHost("127.0.0.1")
                .withPort(6382)
                .withPassword("bitnami".toCharArray())
                .build();
        RedisClusterClient clusterClient = RedisClusterClient.create(creatorUri);

        try (StatefulRedisClusterConnection<String, String> connection = clusterClient.connect()) {
            RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
            RedisFuture<String> future = commands.set("KEY111", "VAL111");
            System.out.println(future.get(2, TimeUnit.MINUTES));
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            clusterClient.shutdown(Duration.ofSeconds(1), Duration.ofSeconds(1));
        }
    }
}
