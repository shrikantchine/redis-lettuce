package com.shrikantchine.pooling;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.support.AsyncConnectionPoolSupport;
import io.lettuce.core.support.BoundedAsyncPool;
import io.lettuce.core.support.BoundedPoolConfig;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class StandaloneRedisTest {
    public static void main(String[] args) throws Exception {
        RedisURI uri = RedisURI.builder()
                .withHost("127.0.0.1")
                .withPort(6379)
                .withTimeout(Duration.of(60, ChronoUnit.SECONDS))
                .build();
        RedisClient redisClient = RedisClient.create(uri);

        Supplier<CompletionStage<StatefulRedisConnection<String, String>>> supplier
                = () -> redisClient.connectAsync(StringCodec.UTF8, uri);
        BoundedPoolConfig config = BoundedPoolConfig.create();
        CompletionStage<BoundedAsyncPool<StatefulRedisConnection<String, String>>> asyncPool
                = AsyncConnectionPoolSupport.createBoundedObjectPoolAsync(supplier, config);

        asyncPool.thenComposeAsync(BoundedAsyncPool::acquire)
                .thenAcceptAsync(conn -> {
                    RedisAsyncCommands<String, String> commands = conn.async();
                    commands.set("THE_KEY_345", "THE_VAL_444333").whenComplete((result, throwable) -> {
                        if (throwable != null) throwable.printStackTrace();
                        else System.out.println(result);
                    });
                    commands.flushCommands();
                })
                .toCompletableFuture().join();

        asyncPool.thenComposeAsync(BoundedAsyncPool::acquire)
                .thenAcceptAsync(conn -> {
                    RedisAsyncCommands<String, String> commands = conn.async();
                    commands.get("THE_KEY_345XX").whenComplete((result, throwable) -> {
                        if (throwable != null) throwable.printStackTrace();
                        else System.out.println(result);
                    });
                    commands.flushCommands();
                })
                .toCompletableFuture().join();

        redisClient.shutdownAsync(1, 1, TimeUnit.MINUTES);

    }
}
