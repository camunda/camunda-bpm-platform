package org.camunda.bpm.engine.test.api.cfg;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.persistence.deploy.DefaultCacheFactory;
import org.camunda.commons.utils.cache.Cache;
import org.camunda.commons.utils.cache.ConcurrentLruCache;

/**
 * Cache that has a maximum capacity two elements.
 *
 * @author Johannes Heinemann
 */
public class MyCacheFactory extends DefaultCacheFactory {

  @Override
  public Cache createCache(int maxNumberOfElementsInCache) {
    return new MyMostRecentlyUsedCache<String, DbEntity>(maxNumberOfElementsInCache);
  }

  private class MyMostRecentlyUsedCache<K, V> extends ConcurrentLruCache<K, V> {

    public MyMostRecentlyUsedCache(int capacity) {
      super(2);
    }

  }
}
