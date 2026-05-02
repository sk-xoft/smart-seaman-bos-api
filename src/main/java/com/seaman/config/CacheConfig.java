package com.seaman.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.seaman.constant.BusinessConstant;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import java.util.Arrays;

@Configuration
public class CacheConfig {

    @Bean
    public SimpleCacheManager buildCacheManager(){
        CaffeineCache masterMessageCode = buildCaffeineCache(BusinessConstant.MASTER_MESSAGE_CODE, BusinessConstant.CACHE_GENERAL_EXPIRE_HOURS);
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(Arrays.asList(masterMessageCode));
        simpleCacheManager.initializeCaches();

        return simpleCacheManager;
    }

    private CaffeineCache buildCaffeineCache(String name, int expireHours){
        return new CaffeineCache(name, Caffeine.newBuilder()
                .softValues()
                .expireAfterWrite(Duration.ofHours(expireHours))
                .build());
    }

}
