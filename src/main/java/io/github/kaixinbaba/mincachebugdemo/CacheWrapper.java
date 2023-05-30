package io.github.kaixinbaba.mincachebugdemo;

import org.springframework.cache.Cache;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;


/**
 * 目前只代理 evic 方法
 *
 * @author xunjunjie
 */
@SuppressWarnings("all")
public class CacheWrapper implements MethodInterceptor {

    private static final String WILDCARD = "*";

    private static final ConcurrentMap<Cache, Cache> cacheMap = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Pattern> regexCacheMap = new ConcurrentHashMap<>();

    private final Cache delegate;

    private CacheWrapper(Cache delegate) {
        this.delegate = delegate;
    }

    /**
     * 获取代理类
     *
     * @param target
     * @return
     */
    private static Cache buildProxy(Cache target) {
        CacheWrapper wrapper = new CacheWrapper(target);

        final Enhancer enhancer = new Enhancer();

        enhancer.setCallback(wrapper);
        enhancer.setSuperclass(target.getClass());

        return (Cache) enhancer.create(
                new Class[] { String.class, com.github.benmanes.caffeine.cache.Cache.class, boolean.class },
                new Object[] { target.getName(), target.getNativeCache(), false });
    }

    /**
     * 获取缓存包装类
     *
     * @param target
     * @return
     */
    public static Cache wrap(Cache target) {
        Cache wrapper = cacheMap.get(target);
        if (Objects.isNull(wrapper)) {
            wrapper = buildProxy(target);
            cacheMap.putIfAbsent(target, wrapper);
        }
        return wrapper;
    }

    /**
     * TODO 目前只支持 * 星号
     *
     * @param method
     * @param args
     * @return
     */
    private boolean evictNeedWildCardMatch(Method method, Object[] args) {
        // Cache 的 evict 只有一个参数
        return Objects.equals(method.getName(), "evict") && args[0] instanceof String
                && ((String) args[0]).contains(WILDCARD);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (!evictNeedWildCardMatch(method, args)) {
            return method.invoke(delegate, args);
        }
        final com.github.benmanes.caffeine.cache.Cache<String, Object> nativeCache = (com.github.benmanes.caffeine.cache.Cache<String, Object>) delegate
                .getNativeCache();
        final Map<String, Object> innerMap = nativeCache.asMap();
        if (CollectionUtils.isEmpty(innerMap)) {
            return method.invoke(delegate, args);
        }

        // 把通配符*替换成正则表达式.*
        String regexPattern = ((String) args[0]).replace("*", ".*");

        Pattern pattern = regexCacheMap.get(regexPattern);
        if (Objects.isNull(pattern)) {
            pattern = Pattern.compile(regexPattern);
            regexCacheMap.putIfAbsent(regexPattern, pattern);
        }
        // for lambda
        final Pattern finalPattern = pattern;

        innerMap.keySet().stream().filter(k -> {
            return finalPattern.matcher(k).matches();
        }).forEach(k -> {
            nativeCache.invalidate(k);
        });
        // 因为 evict 返回 void，这了直接 return null
        return null;
    }
}
