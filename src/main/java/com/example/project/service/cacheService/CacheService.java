package com.example.project.service.cacheService;

import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class CacheService<K, V>  implements ICacheService<K, V>
{
    @Value("${cache.default.ttl}")
    private long defaultTtl;

    @Value("${cache.max.size}")
    private int maxCacheSize;

    private final Map<K, CacheEntry<V>> cache = new LinkedHashMap<>(16, 0.75f, true);

    public Optional<V> getOrSet(K key, V value, Long ttl) {
        CacheEntry<V> cachedEntry = cache.get(key);

        if (cachedEntry != null && !cachedEntry.isExpired()) {
            return Optional.of(cachedEntry.getValue());
        }

        if (cache.size() >= maxCacheSize) {
            evictOldest();
        }
        
        long expiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(
            ttl != null ? ttl : defaultTtl
        );
        cache.put(key, new CacheEntry<>(value, expiryTime));

        return Optional.empty();
    }

    public Optional<V> get(K key)
    {
    	CacheEntry<V> cachedEntry = cache.get(key);

        if (cachedEntry != null && !cachedEntry.isExpired()) {
            return Optional.of(cachedEntry.getValue());
        }
        
        return Optional.empty();
    }
    
    public V set(K key, V value)
    {
    	return set(key, value, defaultTtl);
    }
    
    public V set(K key, V value, Long ttl)
    {
    	 if (cache.size() >= maxCacheSize) {
             evictOldest();
         }

         long expiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(
             ttl != null ? ttl : defaultTtl
         );
         var cachedEntry = new CacheEntry<>(value, expiryTime);
         cache.put(key, new CacheEntry<>(value, expiryTime));

         return cachedEntry.getValue();
    }
    
    public void invalidate(K key) {
        cache.remove(key);
    }

    public void evictOldest() {
        K oldestKey = cache.keySet().iterator().next();
        cache.remove(oldestKey);  
    }

    private static class CacheEntry<V> {
        private final V value;
        private final long expiryTime;

        public CacheEntry(V value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }

        public V getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}
