package com.smartresidential.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartresidential.backend.cache.CacheNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger LOGGER = Logger.getLogger(CacheConfig.class.getName());

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(
            @Value("${app.cache.default-ttl}") Duration defaultTtl
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(defaultTtl)
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            RedisCacheConfiguration defaultConfiguration,
            @Value("${app.cache.issue-categories-ttl}") Duration issueCategoriesTtl,
            @Value("${app.cache.buildings-ttl}") Duration buildingsTtl,
            @Value("${app.cache.apartments-ttl}") Duration apartmentsTtl,
            @Value("${app.cache.issues-ttl}") Duration issuesTtl
    ) {
        return builder -> builder.withInitialCacheConfigurations(Map.of(
                CacheNames.ISSUE_CATEGORIES, defaultConfiguration.entryTtl(issueCategoriesTtl),
                CacheNames.BUILDINGS, defaultConfiguration.entryTtl(buildingsTtl),
                CacheNames.APARTMENTS, defaultConfiguration.entryTtl(apartmentsTtl),
                CacheNames.ISSUES, defaultConfiguration.entryTtl(issuesTtl)
        ));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                logCacheError("get", exception, cache, key);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                logCacheError("put", exception, cache, key);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                logCacheError("evict", exception, cache, key);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                logCacheError("clear", exception, cache, null);
            }
        };
    }

    private static void logCacheError(String operation, RuntimeException exception, Cache cache, Object key) {
        String cacheName = cache != null ? cache.getName() : "unknown";
        LOGGER.log(
                Level.WARNING,
                "Cache " + operation + " failed for cache '" + cacheName + "' and key '" + key + "'. Continuing without cache.",
                exception
        );
    }
}
