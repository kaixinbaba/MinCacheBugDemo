package io.github.kaixinbaba.mincachebugdemo;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * SpringBoot 缓存配置
 *
 * @author xunjunjie
 */
@Configuration
@Slf4j
@EnableCaching
public class CacheConfig {

    /**
     * @return Caffeine 缓存管理器
     */
    @Bean("caffeineCacheManager")
    public CacheManager cacheManager() {
        final CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();

        final Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMillis(1000 * 60 * 60)).initialCapacity(4096).maximumSize(1 << 17);

        caffeineCacheManager.setCaffeine(caffeine);

        // 包装缓存管理器
        return CacheManagerWrapper.wrap(caffeineCacheManager);
    }

}
