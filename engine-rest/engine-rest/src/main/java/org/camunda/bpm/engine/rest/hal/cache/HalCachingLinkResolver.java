/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.hal.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.cache.Cache;
import org.camunda.bpm.engine.rest.hal.Hal;
import org.camunda.bpm.engine.rest.hal.HalLinkResolver;
import org.camunda.bpm.engine.rest.hal.HalResource;

public abstract class HalCachingLinkResolver implements HalLinkResolver {

  /**
   * Resolve resources for linked ids, if configured uses a cache.
   */
  public List<HalResource<?>> resolveLinks(String[] linkedIds, ProcessEngine processEngine) {
    Cache cache = getCache();

    if (cache == null) {
      return resolveNotCachedLinks(linkedIds, processEngine);
    }
    else {
      ArrayList<String> notCachedLinkedIds = new ArrayList<String>();
      List<HalResource<?>> resolvedResources = resolveCachedLinks(linkedIds, cache, notCachedLinkedIds);

      if (!notCachedLinkedIds.isEmpty()) {
        List<HalResource<?>> notCachedResources = resolveNotCachedLinks(notCachedLinkedIds.toArray(new String[notCachedLinkedIds.size()]), processEngine);
        resolvedResources.addAll(notCachedResources);
        putIntoCache(notCachedResources);
      }

      sortResolvedResources(resolvedResources);

      return resolvedResources;
    }
  }

  /**
   * Sort the resolved resources to ensure consistent order of resolved resources.
   */
  protected void sortResolvedResources(List<HalResource<?>> resolvedResources) {
    Comparator<HalResource<?>> comparator = getResourceComparator();
    if (comparator != null) {
      Collections.sort(resolvedResources, comparator);
    }
  }

  /**
   * @return the cache for this resolver
   */
  protected Cache getCache() {
    return Hal.getInstance().getHalRelationCache(getHalResourceClass());
  }

  /**
   * Returns a list with all resources which are cached.
   *
   * @param linkedIds the ids to resolve
   * @param cache the cache to use
   * @param notCachedLinkedIds a list with ids which are not found in the cache
   * @return the cached resources
   */
  protected List<HalResource<?>> resolveCachedLinks(String[] linkedIds, Cache cache, List<String> notCachedLinkedIds) {
    ArrayList<HalResource<?>> resolvedResources = new ArrayList<HalResource<?>>();

    for (String linkedId : linkedIds) {
      HalResource<?> resource = (HalResource<?>) cache.get(linkedId);
      if (resource != null) {
        resolvedResources.add(resource);
      }
      else {
        notCachedLinkedIds.add(linkedId);
      }
    }

    return resolvedResources;
  }

  /**
   * Put a resource into the cache.
   */
  protected void putIntoCache(List<HalResource<?>> notCachedResources) {
    Cache cache = getCache();
    for (HalResource<?> notCachedResource : notCachedResources) {
      cache.put(getResourceId(notCachedResource), notCachedResource);
    }
  }

  /**
   * @return the class of the entity which is resolved
   */
  protected abstract Class<?> getHalResourceClass();

  /**
   * @return a comparator for this HAL resource if not overridden sorting is skipped
   */
  protected Comparator<HalResource<?>> getResourceComparator() {
    return null;
  }

  /**
   * @return the resolved resources which are currently not cached
   */
  protected abstract List<HalResource<?>> resolveNotCachedLinks(String[] linkedIds, ProcessEngine processEngine);

  /**
   * @return the id which identifies a resource in the cache
   */
  protected abstract String getResourceId(HalResource<?> resource);

}
