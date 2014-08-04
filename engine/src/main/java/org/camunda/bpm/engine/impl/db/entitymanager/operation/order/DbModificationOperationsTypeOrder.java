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
package org.camunda.bpm.engine.impl.db.entitymanager.operation.order;

import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType;

/**
 *
 * @author Daniel Meyer
 *
 */
public class DbModificationOperationsTypeOrder implements DbOperationComparator<DbOperation> {

  public int compare(DbOperation firstOperation, DbOperation secondOperation) {

    DbOperationType firstOperationType = firstOperation.getOperationType();
    DbOperationType secondOperationType = secondOperation.getOperationType();

    Integer firstOp = (firstOperationType == DbOperationType.UPDATE_BULK || firstOperationType == DbOperationType.DELETE_BULK) ? 2 : 1;
    Integer secondOp = (secondOperationType == DbOperationType.UPDATE_BULK || secondOperationType == DbOperationType.DELETE_BULK) ? 2 : 1;

    return firstOp.compareTo(secondOp);
  }

  public boolean isApplicableTo(DbOperation dbOperation) {
    return true;
  }

}
