/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.db.entitymanager.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.db.DbEntity;


/**
 * A simple first level cache for {@link DbEntity Entities}.
 *
 * @author Daniel Meyer
 *
 */
public class DbEntityCache {

  /**
   * The cache itself: a map indexed by the id (aka. Primary Key) of the objects
   */
  protected Map<Class<?>, Map<String, CachedDbEntity>> cachedEntites = new HashMap<Class<?>, Map<String, CachedDbEntity>>();

  /**
   * get an object from the cache
   *
   * @param type the type of the object
   * @param id the id of the object
   * @return the object or 'null' if the object is not in the cache
   * @throws ProcessEngineException if an object for the given id can be found but is of the wrong type.
   */
  @SuppressWarnings("unchecked")
  public <T extends DbEntity> T get(Class<T> type, String id) {
    CachedDbEntity cachedDbEntity = getCachedEntity(type, id);
    if(cachedDbEntity != null) {
      DbEntity dbEntity = cachedDbEntity.getEntity();
      try {
        return (T) dbEntity;
      } catch(ClassCastException e) {
        throw new ProcessEngineException("Could not lookup entity of type '"+type+"' and id '"+id+"': found entity of type '"+dbEntity.getClass()+"'.", e);
      }
    } else {
      return null;
    }
  }

  /**
   * Looks up an entity in the cache.
   *
   * @param type the type of the object
   * @param id the id of the CachedEntity to lookup
   * @return the cached entity or null if the entity does not exist.
   */
  public CachedDbEntity getCachedEntity(Class<?> type, String id) {
    Map<String, CachedDbEntity> entitesByType = cachedEntites.get(type);
    if(entitesByType != null) {
      return entitesByType.get(id);
    } else {
      return null;
    }
  }

  /**
   * Looks up an entity in the cache.
   * @param dbEntity the entity for which the CachedEntity should be looked up
   * @return the cached entity or null if the entity does not exist.
   */
  public CachedDbEntity getCachedEntity(DbEntity dbEntity) {
    return getCachedEntity(dbEntity.getClass(), dbEntity.getId());
  }

  /**
   * Put a new, {@link DbEntityState#TRANSIENT} object into the cache.
   *
   * @param e the object to put into the cache
   */
  public void putTransient(DbEntity e) {
    CachedDbEntity cachedDbEntity = new CachedDbEntity();
    cachedDbEntity.setEntity(e);
    cachedDbEntity.setEntityState(DbEntityState.TRANSIENT);
    putInternal(cachedDbEntity);
  }

  /**
   * Put a {@link DbEntityState#PERSISTENT} object into the cache.
   *
   * @param e the object to put into the cache
   */
  public void putPersistent(DbEntity e) {
    CachedDbEntity cachedDbEntity = new CachedDbEntity();
    cachedDbEntity.setEntity(e);
    cachedDbEntity.setEntityState(DbEntityState.PERSISTENT);
    cachedDbEntity.makeCopy();
    putInternal(cachedDbEntity);
  }

  protected void putInternal(CachedDbEntity cachedDbEntity) {
    Class<? extends DbEntity> type = cachedDbEntity.getEntity().getClass();
    Map<String, CachedDbEntity> map = cachedEntites.get(type);
    if(map == null) {
      map = new HashMap<String, CachedDbEntity>();
      cachedEntites.put(type, map);
    }
    map.put(cachedDbEntity.getEntity().getId(), cachedDbEntity);
  }

  /**o
   * Remove an entity from the cache
   * @param e the entity to remove
   * @return
   */
  public boolean remove(DbEntity e) {
    Map<String, CachedDbEntity> typeMap = cachedEntites.get(e.getClass());
    if(typeMap != null) {
      CachedDbEntity removedEntry = typeMap.remove(e.getId());
      if(typeMap.isEmpty()) {
        cachedEntites.remove(e.getClass());
      }
      return removedEntry != null;
    } else {
      return false;
    }
  }

  /**
   * Allows checking whether the entity with the provided id is present in the cache
   *
   * @param id the id of the entity to check
   * @return true if the the entity with the provided id is present in the cache
   */
  public boolean contains(String id) {
    return cachedEntites.containsKey(id);
  }

  /**
   * Allows checking whether the provided entity is present in the cache
   *
   * @param dbEntity the entity to check
   * @return true if the the provided entity is present in the cache
   */
  public boolean contains(DbEntity dbEntity) {
    return contains(dbEntity.getId());
  }

  /**
   * Allows checking whether the provided entity is present in the cache
   * and is {@link DbEntityState#PERSISTENT}.
   *
   * @param dbEntity the entity to check
   * @return true if the provided entity is present in the cache and is
   * {@link DbEntityState#PERSISTENT}.
   */
  public boolean isPersistent(DbEntity dbEntity) {
    CachedDbEntity cachedDbEntity = getCachedEntity(dbEntity);
    if(cachedDbEntity == null) {
      return false;
    } else {
      return cachedDbEntity.getEntityState() == DbEntityState.PERSISTENT;
    }
  }

  /**
   * Allows checking whether the provided entity is present in the cache
   * and is {@link DbEntityState#TRANSIENT}.
   *
   * @param dbEntity the entity to check
   * @return true if the provided entity is present in the cache and is
   * {@link DbEntityState#TRANSIENT}.
   */
  public boolean isTransient(DbEntity dbEntity) {
    CachedDbEntity cachedDbEntity = getCachedEntity(dbEntity);
    if(cachedDbEntity == null) {
      return false;
    } else {
      return cachedDbEntity.getEntityState() == DbEntityState.TRANSIENT;
    }
  }

  /**
   * Iterate the {@link CachedDbEntity CachedEntities} in the cache.
   * @return an Iterator over the {@link CachedDbEntity CachedEntities} in the cache.
   */
  public Iterator<CachedDbEntity> cachedEntitiesIterator() {
    final Iterator<Class<?>> cacheKeyIterator = cachedEntites.keySet().iterator();
    return new Iterator<CachedDbEntity>() {

      Iterator<CachedDbEntity> cacheValueIterator = null;

      public boolean hasNext() {
        if(cacheValueIterator == null || !cacheValueIterator.hasNext()) {
          return cacheKeyIterator.hasNext();
        } else {
          return true;
        }
      }

      public CachedDbEntity next() {
        if((cacheValueIterator == null || !cacheValueIterator.hasNext()) && cacheKeyIterator.hasNext()) {
          Class<?> nextKey = cacheKeyIterator.next();
          cacheValueIterator = cachedEntites.get(nextKey).values().iterator();
        }
        return cacheValueIterator.next();
      }

      public void remove() {
        cacheValueIterator.remove();
      }
    };
  }

}
