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

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;

/**
 * Orders operations by the id of the modified entitites.
 *
 * @author Daniel Meyer
 *
 */
public class EntityIdOrder implements DbOperationComparator<DbEntityOperation> {

  public boolean isApplicableTo(DbOperation dbOperation) {
    // not applicable to bulk operations
    return (dbOperation instanceof DbEntityOperation);
  }

  public int compare(DbEntityOperation firstOperation, DbEntityOperation secondOperation) {

    DbEntity firstEntity = firstOperation.getEntity();
    DbEntity secondEntity = secondOperation.getEntity();

    int order = firstEntity.getId().compareTo(secondEntity.getId());
    if(order == 0) {
      // the same id may be used for multiple enitites of different types
      return firstEntity.getClass().getName().compareTo(secondEntity.getClass().getName());
    } else {
      return order;
    }
  }

}
