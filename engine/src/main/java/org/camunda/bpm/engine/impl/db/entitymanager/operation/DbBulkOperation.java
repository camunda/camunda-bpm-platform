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

/**
 * A bulk operation
 *
 * @author Daniel Meyer
 *
 */
public class DbBulkOperation extends DbOperation {

  public DbBulkOperation() {
  }

  public DbBulkOperation(DbOperationType operationType, Class<? extends DbEntity> entityType, String statement, Object parameter) {
    this.operationType = operationType;
    this.entityType = entityType;
    this.statement = statement;
    this.parameter = parameter;
  }

  protected String statement;
  protected Object parameter;

  @Override
  public void recycle() {
    statement = null;
    parameter = null;
    super.recycle();
  }

  public boolean isFailed() {
    return false;
  }

  public Object getParameter() {
    return parameter;
  }

  public void setParameter(Object parameter) {
    this.parameter = parameter;
  }

  public String getStatement() {
    return statement;
  }

  public void setStatement(String statement) {
    this.statement = statement;
  }

  public String toString() {
    return operationType + " "+ statement +" " +parameter;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
    result = prime * result + ((statement == null) ? 0 : statement.hashCode());
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
    DbBulkOperation other = (DbBulkOperation) obj;
    if (parameter == null) {
      if (other.parameter != null)
        return false;
    } else if (!parameter.equals(other.parameter))
      return false;
    if (statement == null) {
      if (other.statement != null)
        return false;
    } else if (!statement.equals(other.statement))
      return false;
    return true;
  }


}
