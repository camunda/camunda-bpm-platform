package org.camunda.bpm.engine.impl.persistence.deploy;

import org.camunda.commons.utils.cache.Cache;

/**
 * <p>Builds the caches for the {@link DeploymentCache}.</p>
 */
public interface CacheFactory {

  /**
   * Creates a cache that does not exceed a specified number of elements.
   *
   * @param maxNumberOfElementsInCache
   *        The maximum number of elements that is allowed within the cache at the same time.
   * @return
   *        The cache to be created.
   */
  public Cache createCache(int maxNumberOfElementsInCache);
}
