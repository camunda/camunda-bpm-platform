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

import org.camunda.bpm.engine.impl.db.DbEntity;
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
    return
      // the entity is PERSISTENT
      entityState == DbEntityState.PERSISTENT
      // AND it has changed
      && !dbEntity.getPersistentState().equals(copy);
  }

  public void makeCopy() {
    copy = dbEntity.getPersistentState();
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

}
