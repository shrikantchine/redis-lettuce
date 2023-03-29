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

public class MasterSlaveRedisTest {
    public static void main(String[] args) {
        RedisURI uriMaster = RedisURI.builder()
                .withHost("127.0.0.1")
                .withPort(6379)
                .withTimeout(Duration.of(60, ChronoUnit.SECONDS))
                .build();
        RedisClient redisClientMaster = RedisClient.create(uriMaster);

        try (StatefulRedisMasterReplicaConnection<String, String> connection = MasterReplica.connect(
                redisClientMaster, StringCodec.UTF8, uriMaster
        )) {
            connection.setReadFrom(ReadFrom.ANY_REPLICA);
            RedisAsyncCommands<String, String> commands = connection.async();
            RedisFuture<String> future = commands.set("Key", "VAL");
            String result = future.get(10, TimeUnit.SECONDS);
            System.out.println(result);

            RedisFuture<String> val = commands.get("Key");
            System.out.println(val.get());
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            redisClientMaster.shutdown(Duration.ofSeconds(2), Duration.ofSeconds(2));
        }
    }
}
