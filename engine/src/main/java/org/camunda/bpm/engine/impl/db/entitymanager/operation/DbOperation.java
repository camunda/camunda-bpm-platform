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

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.Recyclable;

/**
 * A database operation.
 *
 * @author Daniel Meyer
 *
 */
public abstract class DbOperation implements Recyclable {

  /**
   * The type of the operation.
   */
  protected DbOperationType operationType;

  /**
   * The type of the DbEntity this operation is executed on.
   */
  protected Class<? extends DbEntity> entityType;

  public void recycle() {
    // clean out the object state
    operationType = null;
    entityType = null;
  }

  // getters / setters //////////////////////////////////////////

  public abstract boolean isFailed();

  public Class<? extends DbEntity> getEntityType() {
    return entityType;
  }

  public void setEntityType(Class<? extends DbEntity> entityType) {
    this.entityType = entityType;
  }

  public DbOperationType getOperationType() {
    return operationType;
  }

  public void setOperationType(DbOperationType operationType) {
    this.operationType = operationType;
  }

}
