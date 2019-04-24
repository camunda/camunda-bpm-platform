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
package org.camunda.bpm.engine.impl.db.entitymanager.cache;

import java.util.Collections;
import java.util.Set;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.entitymanager.Recyclable;

/**
 * A cached entity
 *
 * @author Daniel Meyer
 *
 */
public class CachedDbEntity implements Recyclable {

  protected DbEntity dbEntity;

  protected Object copy;

  protected DbEntityState entityState;

  /**
   * Ids of referenced entities of the same entity type
   */
  protected Set<String> flushRelevantEntityReferences = null;

  public void recycle() {
    // clean out state
    dbEntity = null;
    copy = null;
    entityState = null;
  }

  /**
   * Allows checking whether this entity is dirty.
   * @return true if the entity is dirty (state has changed since it was put into the cache)
   */
  public boolean isDirty() {
    return !dbEntity.getPersistentState().equals(copy);
  }

  public void forceSetDirty() {
    // set the value of the copy to some value which will always be different from the new entity state.
    this.copy = -1;
  }

  public void makeCopy() {
    copy = dbEntity.getPersistentState();
  }

  public String toString() {
    return entityState + " " + dbEntity.getClass().getSimpleName() + "["+dbEntity.getId()+"]";
  }

  public void determineEntityReferences() {
    if (dbEntity instanceof HasDbReferences) {
      flushRelevantEntityReferences = ((HasDbReferences) dbEntity).getReferencedEntityIds();
    }
    else {
      flushRelevantEntityReferences = Collections.emptySet();
    }
  }

  public boolean areFlushRelevantReferencesDetermined() {
    return flushRelevantEntityReferences != null;
  }

  public Set<String> getFlushRelevantEntityReferences() {
    return flushRelevantEntityReferences;
  }

  // getters / setters ////////////////////////////

  public DbEntity getEntity() {
    return dbEntity;
  }

  public void setEntity(DbEntity dbEntity) {
    this.dbEntity = dbEntity;
  }

  public DbEntityState getEntityState() {
    return entityState;
  }

  public void setEntityState(DbEntityState entityState) {
    this.entityState = entityState;
  }

  public Class<? extends DbEntity> getEntityType() {
    return dbEntity.getClass();
  }

}
