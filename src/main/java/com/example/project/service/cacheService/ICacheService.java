package com.example.project.service.cacheService;

import java.util.Optional;

public interface ICacheService<K, V>  {

    /**
     * Retrieves the value from cache if it exists and is not expired,
     * otherwise adds the value to cache and returns an empty Optional.
     *
     * @param key   the cache key
     * @param value the value to cache (if not already cached)
     * @param ttl   the TTL (Time-To-Live) in seconds; if null, default TTL is used
     * @return an Optional containing the cached value if found, otherwise an empty Optional
     */
    Optional<V> getOrSet(K key, V value, Long ttl);

    /**
     * Retrieves the value from cache if it exists and is not expired,
     * else returns an empty Optional.
     *
     * @param key   the cache key
     * @return the cached value if found, otherwise an empty Optional
     */
    Optional<V> get(K key);
    
    /**
     * Adds the value to cache and returns the value.
     *
     * @param key   the cache key
     * @param value the value to cache (if not already cached)
     * @param ttl   the default TTL (Time-To-Live) is used
     * @return the cached value
     */
     V set(K key, V value);
    
    /**
     * Adds the value to cache and returns the value.
     *
     * @param key   the cache key
     * @param value the value to cache (if not already cached)
     * @param ttl   the TTL (Time-To-Live) in seconds; if null, default TTL is used
     * @return the cached value
     */
     V set(K key, V value, Long ttl);
    
    /**
     * Invalidates the cache entry for the given key.
     *
     * @param key the cache key to invalidate
     */
    void invalidate(K key);

    /**
     * Evicts the oldest entry from the cache when the cache exceeds its max size.
     */
    void evictOldest();
}
