package com.shrikantchine;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StandaloneRedisTest {
    public static void main(String[] args) {
        RedisURI uri = RedisURI.builder()
                .withHost("127.0.0.1")
                .withPort(6379)
                .withTimeout(Duration.of(60, ChronoUnit.SECONDS))
                .build();
        RedisClient redisClient = RedisClient.create(uri);
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisAsyncCommands<String, String> commands = connection.async();
            RedisFuture<String> future = commands.set("Key", "VAL");
            String result = future.get(10, TimeUnit.SECONDS);
            System.out.println(result);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            redisClient.shutdown(Duration.ofSeconds(2), Duration.ofSeconds(2));
        }
    }
}