package io.github.kaixinbaba.mincachebugdemo;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author xunjunjie
 */
@Service
public class TestService {

    /**
     * 缓存方法，命中缓存直接返回，否则会等待 3秒 再返回
     * @param name
     * @return
     */
    @Cacheable(cacheNames = "cacheName", key = "#name")
    public String cache(String name) {
        try {
            TimeUnit.SECONDS.sleep(3L);
        } catch (InterruptedException e) {
        }
        return "Hello " + name;
    }
}
