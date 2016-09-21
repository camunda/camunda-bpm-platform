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
import java.util.Map;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricFormPropertyEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricFormPropertyEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;

/**
 * Provides the keys used by {@link DbEntityCache} for organizing the different {@link DbEntity} types.
 * Especially for polymorphic types, it is important that they are accessible in the cache under one
 * common key such that querying the cache with a superclass or with a subclass both return the cached
 * entities.
 *
 * @author Thorben Lindhauer
 */
public class DbEntityCacheKeyMapping {

  protected Map<Class<?>, Class<?>> entityCacheKeys;

  public DbEntityCacheKeyMapping() {
    this.entityCacheKeys = new HashMap<Class<?>, Class<?>>();
  }

  public Class<?> getEntityCacheKey(Class<?> entityType) {
    Class<?> entityCacheKey = entityCacheKeys.get(entityType);
    if (entityCacheKey == null) {
      return entityType;
    }

    return entityCacheKey;
  }

  public void registerEntityCacheKey(Class<?> entityType, Class<?> cacheKey) {
    this.entityCacheKeys.put(entityType, cacheKey);
  }

  public static DbEntityCacheKeyMapping defaultEntityCacheKeyMapping() {
    DbEntityCacheKeyMapping mapping = new DbEntityCacheKeyMapping();

    // subclasses of JobEntity
    mapping.registerEntityCacheKey(MessageEntity.class, JobEntity.class);
    mapping.registerEntityCacheKey(TimerEntity.class, JobEntity.class);

    // subclasses of HistoricDetailEventEntity
    mapping.registerEntityCacheKey(HistoricFormPropertyEntity.class, HistoricDetailEventEntity.class);
    mapping.registerEntityCacheKey(HistoricFormPropertyEventEntity.class, HistoricDetailEventEntity.class);
    mapping.registerEntityCacheKey(HistoricVariableUpdateEventEntity.class, HistoricDetailEventEntity.class);
    mapping.registerEntityCacheKey(HistoricDetailVariableInstanceUpdateEntity.class, HistoricDetailEventEntity.class);

    return mapping;
  }

  public static DbEntityCacheKeyMapping emptyMapping() {
    return new DbEntityCacheKeyMapping();
  }
}
