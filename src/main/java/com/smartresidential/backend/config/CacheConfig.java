package com.smartresidential.backend.config;

import com.smartresidential.backend.cache.CacheNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(
            @Value("${app.cache.default-ttl}") Duration defaultTtl
    ) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(defaultTtl)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()
                ));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            RedisCacheConfiguration defaultConfiguration,
            @Value("${app.cache.issue-categories-ttl}") Duration issueCategoriesTtl,
            @Value("${app.cache.buildings-ttl}") Duration buildingsTtl,
            @Value("${app.cache.apartments-ttl}") Duration apartmentsTtl
    ) {
        return builder -> builder.withInitialCacheConfigurations(Map.of(
                CacheNames.ISSUE_CATEGORIES, defaultConfiguration.entryTtl(issueCategoriesTtl),
                CacheNames.BUILDINGS, defaultConfiguration.entryTtl(buildingsTtl),
                CacheNames.APARTMENTS, defaultConfiguration.entryTtl(apartmentsTtl)
        ));
    }
}
