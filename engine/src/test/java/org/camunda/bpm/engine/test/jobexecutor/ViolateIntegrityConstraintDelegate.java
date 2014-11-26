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
package org.camunda.bpm.engine.test.jobexecutor;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Daniel Meyer
 *
 */
public class ViolateIntegrityConstraintDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    String existingId = execution.getId();

    // insert an execution referencing the current execution

    ExecutionEntity newExecution = new ExecutionEntity();
    newExecution.setId("someId");
    newExecution.setParentId(existingId);

    DbEntityOperation insertOperation = new DbEntityOperation();
    insertOperation.setOperationType(DbOperationType.INSERT);
    insertOperation.setEntity(newExecution);

    Context.getCommandContext()
      .getDbSqlSession()
      .executeDbOperation(insertOperation);

  }

}
