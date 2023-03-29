package com.shrikantchine;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SentinelRedisTest {
    public static void main(String[] args) {
        RedisURI urlSentinel = RedisURI.Builder.sentinel("127.0.0.1", 6382)
                .withSentinelMasterId("mymaster")
                .withTimeout(Duration.of(60, ChronoUnit.SECONDS))
                .build();
        RedisClient redisClientSentinel = RedisClient.create(urlSentinel);

        try (StatefulRedisMasterReplicaConnection<String, String> connection
                     = MasterReplica.connect(redisClientSentinel, StringCodec.UTF8, urlSentinel)) {
            connection.setReadFrom(ReadFrom.ANY_REPLICA);
            RedisAsyncCommands<String, String> commands = connection.async();
            RedisFuture<String> future = commands.set("KEY", "VALUE");
            System.out.println(future.get(2, TimeUnit.MINUTES));
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            redisClientSentinel.shutdown(Duration.ofSeconds(2), Duration.ofSeconds(2));
        }
    }
}
