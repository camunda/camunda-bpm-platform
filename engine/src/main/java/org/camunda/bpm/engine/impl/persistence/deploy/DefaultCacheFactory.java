package org.camunda.bpm.engine.impl.persistence.deploy;


import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.commons.utils.cache.Cache;
import org.camunda.commons.utils.cache.ConcurrentLruCache;

/**
 * <p>Provides the default cache implementation for the deployment caches see {@link DeploymentCache}.</p>
 *
 * @author Johannes Heinemann
 */
public class DefaultCacheFactory implements CacheFactory{

  @Override
  public Cache createCache(int maxNumberOfElementsInCache) {
    return new ConcurrentLruCache<String, DbEntity>(maxNumberOfElementsInCache);
  }
}
