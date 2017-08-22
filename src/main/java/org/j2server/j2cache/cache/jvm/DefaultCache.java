package org.j2server.j2cache.cache.jvm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.j2server.j2cache.cache.CacheSizes;
import org.j2server.j2cache.cache.CannotCalculateSizeException;
import org.j2server.j2cache.cache.ICache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCache<K, V> implements ICache<K, V> {
	private static final Logger Log = LoggerFactory.getLogger(DefaultCache.class);
	protected Map<K, CacheWapper<V>> map;
	private String name;
	private long maxCacheSize;
	private long maxLifetime;
	private int cacheSize = 0;
	private LinkedList<KeyLinkNode<K>> keyLinklist;

	public DefaultCache(String name, long maxSize, long maxLifetime) {
		this.name = name;
		this.maxCacheSize = maxSize;
		this.maxLifetime = maxLifetime;
		keyLinklist = new LinkedList<KeyLinkNode<K>>();
		map = new ConcurrentHashMap<K, CacheWapper<V>>(103);		
	}
	
	@Override
	public int size() {
		clearExpireCache();
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		clearExpireCache();
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		clearExpireCache();
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
        Iterator<?> it = values().iterator();
        while(it.hasNext()) {
            if(value.equals(it.next())) {
                 return true;
            }
        }
        return false;
	}

	@Override
	public V get(Object key) {
		clearExpireCache();
		
		CacheWapper<V> wapper = map.get(key);
		if (wapper == null) {
			return null;
		}
		return wapper.object;
	}

	@Override
	public V put(K key, V value) {
		V returnValue = remove(key);
		
		int objectSize = 1;
        try {
             objectSize = CacheSizes.sizeOfAnything(value);
        }
        catch (CannotCalculateSizeException e) {
             Log.warn(e.getMessage(), e);
        }
        
        if (maxCacheSize > 0 && objectSize > maxCacheSize * .98) {
        	Log.warn("Cache: " + name + " -- object with key " + key +
                    " is too large to fit in cache. Size is " + objectSize);
        	return value;
        }
        cacheSize += objectSize;        
		DefaultCache.CacheWapper<V> cacheObject = new DefaultCache.CacheWapper<>(value, objectSize);
		map.put(key, cacheObject);
		//将Key放于链表中
		KeyLinkNode<K> node = new KeyLinkNode<K>();
		node.timestamp = System.currentTimeMillis();
		node.key = key;
		keyLinklist.addFirst(node);
		return returnValue;
	}

	@Override
	public V remove(Object key) {
		DefaultCache.CacheWapper<V> cacheObject = map.get(key);
        // If the object is not in cache, stop trying to remove it.
        if (cacheObject == null) {
            return null;
        }
        // remove from the hash map
        map.remove(key);
        // removed the object, so subtract its size from the total.
        cacheSize -= cacheObject.size;
        return cacheObject.object;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
        for (Iterator<? extends K> i = m.keySet().iterator(); i.hasNext();) {
            K key = i.next();
            V value = m.get(key);
            put(key, value);
        }
	}

	@Override
	public void clear() {
		if (map != null) {
			map.clear();
		}
		
		keyLinklist.clear();
		keyLinklist = new LinkedList<DefaultCache.KeyLinkNode<K>>();
		cacheSize = 0;
	}

	@Override
	public Set<K> keySet() {
        clearExpireCache();
        synchronized (this) {
            return new HashSet<>(map.keySet());
        }
	}

	@Override
	public Collection<V> values() {
		clearExpireCache();
		return new DefaultCache.CacheObjectCollection(map.values());
	}
    /**
     * Wraps a cached object collection to return a view of its inner objects
     */
    private final class CacheObjectCollection<V> implements Collection<V> {
        private Collection<DefaultCache.CacheWapper<V>> cachedObjects;

        private CacheObjectCollection(Collection<DefaultCache.CacheWapper<V>> cachedObjects) {
            this.cachedObjects = new ArrayList<>(cachedObjects);
        }

        @Override
        public int size() {
            return cachedObjects.size();
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public boolean contains(Object o) {
            Iterator<V> it = iterator();
            while (it.hasNext()) {
                if (it.next().equals(o)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Iterator<V> iterator() {
            return new Iterator<V>() {
                private final Iterator<DefaultCache.CacheWapper<V>> it = cachedObjects.iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public V next() {
                    if(it.hasNext()) {
                        DefaultCache.CacheWapper<V> object = it.next();
                        if(object == null) {
                            return null;
                        } else {
                            return object.object;
                        }
                    }
                    else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public Object[] toArray() {
            Object[] array = new Object[size()];
            Iterator<?> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                array[i] = it.next();
            }
            return array;
        }

        @Override
        public <V>V[] toArray(V[] a) {
            Iterator<V> it = (Iterator<V>) iterator();
            int i = 0;
            while (it.hasNext()) {
                a[i++] = it.next();
            }
            return a;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            Iterator<?> it = c.iterator();
            while(it.hasNext()) {
                if(!contains(it.next())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean add(V o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends V> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }


	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
        synchronized (this) {
            final Map<K, V> result = new HashMap<>();
            for (final Entry<K, DefaultCache.CacheWapper<V>> entry : map.entrySet()) {
                result.put(entry.getKey(), entry.getValue().object);
            }
            return result.entrySet();
        }
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public long getMaxCacheSize() {
		return maxCacheSize;
	}

	@Override
	public void setMaxCacheSize(int maxSize) {
		this.maxCacheSize = maxSize;
	}

	@Override
	public long getMaxLifetime() {
		return maxLifetime;
	}

	@Override
	public void setMaxLifetime(long maxLifetime) {
		this.maxLifetime = maxLifetime;
	}

	@Override
	public int getCacheSize() {
		return cacheSize;
	}

	/**
	 * 清理过期缓存
	 */
	protected void clearExpireCache() {
		if (maxLifetime <=0 ) {
			return;
		}
		
		long expireTime = System.currentTimeMillis() - maxLifetime;
		
		Iterator list = keyLinklist.iterator();	
		while (list.hasNext()) {
			KeyLinkNode<K> node = (KeyLinkNode<K>) list.next();
            if (node == null) {
                return;
            }
            
            if (expireTime > node.timestamp) {
            	// Remove the object
            	remove(node.key);
            	keyLinklist.remove(node);
            }
        }
	}
	
	@SuppressWarnings("unused")
	private static class CacheWapper<V>  {
		 /**
         * Underlying object wrapped by the CacheObject.
         */
        public V object;

        /**
         * The size of the Cacheable object. The size of the Cacheable
         * object is only computed once when it is added to the cache. This makes
         * the assumption that once objects are added to cache, they are mostly
         * read-only and that their size does not change significantly over time.
         */
        public int size;		
		private long createTime;
		
        public CacheWapper(V object, int size) {
            this.object = object;
            this.size = size;
        }
	}
	
	/**
	 * 缓存中key的链表，用于快速检索
	 * @author xiexb
	 *
	 * @param <E>
	 */
	private static class KeyLinkNode<E> {
		public long timestamp;
		public E key;
	}
}
