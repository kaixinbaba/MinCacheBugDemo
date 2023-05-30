package io.github.kaixinbaba.mincachebugdemo;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 缓存管理类
 *
 * @author xunjunjie
 */
@SuppressWarnings("all")
public class CacheManagerWrapper implements InvocationHandler {

    private final CacheManager delegate;

    private CacheManagerWrapper(CacheManager cacheManager) {
        this.delegate = cacheManager;
    }

    public static CacheManager wrap(CacheManager target) {
        CacheManagerWrapper wrapper = new CacheManagerWrapper(target);
        final Object proxy = Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(), wrapper);
        System.out.println(proxy instanceof CacheManager);
        return (CacheManager) proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(delegate, args);
        if (Objects.equals(method.getName(), "getCache") && result instanceof Cache) {
            result = CacheWrapper.wrap((Cache) result);
        }
        return result;
    }
}
