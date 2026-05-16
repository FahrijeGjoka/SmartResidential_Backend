package com.smartresidential.backend.cache;

import com.smartresidential.backend.multitenancy.TenantContext;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class TenantCacheEvictor {

    private static final Logger LOGGER = Logger.getLogger(TenantCacheEvictor.class.getName());

    private final StringRedisTemplate redisTemplate;

    public TenantCacheEvictor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void evictCurrentTenant(String cacheName) {
        String schemaName = TenantContext.getSchemaName();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictTenant(cacheName, schemaName);
                }
            });
            return;
        }

        evictTenant(cacheName, schemaName);
    }

    public void evictTenant(String cacheName, String schemaName) {
        String keyPattern = cacheName + "::*tenant:" + schemaName + ":*";

        try {
            Set<String> keys = scanKeys(keyPattern);
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (RuntimeException exception) {
            LOGGER.log(
                    Level.WARNING,
                    "Failed to evict tenant-scoped cache entries for pattern '" + keyPattern + "'.",
                    exception
            );
        }
    }

    private Set<String> scanKeys(String pattern) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(100)
                    .build();

            try (var cursor = connection.keyCommands().scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }

            return keys;
        });
    }
}
