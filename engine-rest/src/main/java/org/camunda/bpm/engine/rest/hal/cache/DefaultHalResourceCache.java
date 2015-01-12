/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.rest.cache.Cache;

public class DefaultHalResourceCache implements Cache {

  public final static Comparator<HalResourceCacheEntry> COMPARATOR = HalResourceCacheEntryComparator.getInstance();

  protected int capacity;
  protected long secondsToLive;
  protected Map<String, HalResourceCacheEntry> cache;

  public DefaultHalResourceCache() {
    this(100, 100);
  }

  public DefaultHalResourceCache(int capacity, long secondsToLive) {
    this.capacity = capacity;
    this.secondsToLive = secondsToLive;
    cache = new HashMap<String, HalResourceCacheEntry>();
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  public long getSecondsToLive() {
    return secondsToLive;
  }

  public void setSecondsToLive(long secondsToLive) {
    this.secondsToLive = secondsToLive;
  }

  public int size() {
    return cache.size();
  }

  public void put(String id, Object resource) {
    cache.put(id, new HalResourceCacheEntry(id, resource));
    ensureCapacityLimit();
  }

  public void remove(String id) {
    cache.remove(id);
  }

  public Object get(String id) {
    HalResourceCacheEntry cacheEntry = cache.get(id);
    if (cacheEntry != null) {
      if (expired(cacheEntry)) {
        remove(cacheEntry.getId());
        return null;
      }
      else {
        return cacheEntry.getResource();
      }
    }
    else {
      return null;
    }
  }

  public void destroy() {
    cache.clear();
  }

  protected void ensureCapacityLimit() {
    if (size() > getCapacity()) {
      List<HalResourceCacheEntry> resources = new ArrayList<HalResourceCacheEntry>(cache.values());
      NavigableSet<HalResourceCacheEntry> remainingResources = new TreeSet<HalResourceCacheEntry>(COMPARATOR);

      // remove expired resources
      for (HalResourceCacheEntry resource : resources) {
        if (expired(resource)) {
          remove(resource.getId());
        }
        else {
          remainingResources.add(resource);
        }

        if (size() <= getCapacity()) {
          // abort if capacity is reached
          return;
        }
      }

      // if still exceed capacity remove oldest
      while (remainingResources.size() > capacity) {
        HalResourceCacheEntry resourceToRemove = remainingResources.pollFirst();
        if (resourceToRemove != null) {
          remove(resourceToRemove.getId());
        }
        else {
          break;
        }
      }
    }
  }

  protected boolean expired(HalResourceCacheEntry entry) {
    return entry.getCreateTime() + secondsToLive * 1000 < ClockUtil.getCurrentTime().getTime();
  }

}
