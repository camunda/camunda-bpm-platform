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
package org.camunda.bpm.engine.impl.db.entitymanager.operation;

import java.util.Set;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;

/**
 * An operation on a single DbEntity
 *
 * @author Daniel Meyer
 *
 */
public class DbEntityOperation extends DbOperation {

  /**
   * The entity the operation is performed on.
   */
  protected DbEntity entity;

  protected Set<String> flushRelevantEntityReferences;

  /**
   * Indicates whether the operation failed to execute due to OptimisticLocking
   */
  protected boolean failed = false;

  public void recycle() {
    entity = null;
    super.recycle();
  }

  public DbEntity getEntity() {
    return entity;
  }

  public void setEntity(DbEntity dbEntity) {
    this.entityType = dbEntity.getClass();
    this.entity = dbEntity;
  }

  public void setFailed(boolean failed) {
    this.failed = failed;
  }

  public boolean isFailed() {
    return failed;
  }

  public void setFlushRelevantEntityReferences(Set<String> flushRelevantEntityReferences) {
    this.flushRelevantEntityReferences = flushRelevantEntityReferences;
  }

  public Set<String> getFlushRelevantEntityReferences() {
    return flushRelevantEntityReferences;
  }

  public String toString() {
    return operationType + " " + ClassNameUtil.getClassNameWithoutPackage(entity)+"["+entity.getId()+"]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entity == null) ? 0 : entity.hashCode());
    result = prime * result + ((operationType == null) ? 0 : operationType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DbEntityOperation other = (DbEntityOperation) obj;
    if (entity == null) {
      if (other.entity != null)
        return false;
    } else if (!entity.equals(other.entity))
      return false;
    if (operationType != other.operationType)
      return false;
    return true;
  }

}
