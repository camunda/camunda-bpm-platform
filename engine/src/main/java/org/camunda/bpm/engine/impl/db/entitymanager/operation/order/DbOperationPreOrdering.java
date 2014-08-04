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

import java.util.Comparator;

import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;

/**
 *
 * @author Daniel Meyer
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DbOperationPreOrdering implements Comparator<DbOperation> {

  protected final DbOperationComparator[] delegateComparators;

  public DbOperationPreOrdering(DbOperationComparator[] delegateComparators) {
    this.delegateComparators = delegateComparators;
  }

  public DbOperationPreOrdering() {
    this.delegateComparators = new DbOperationComparator[] {
      new DbOperationTypeOrder(),  // INSERT before BULK before DELETE & UPDATE                                   RELATIVE Order over Bulk + Entity
      new EntityTypeOrder(),       // Entities of a given type always BEFORE/AFTER entities of another type       RELATIVE Order over Bulk + Entity
      new DbModificationOperationsTypeOrder(),
      new EntityIdOrder(),         // order by entity id                                                          TOTAL Order over Entity
      new BulkStatementOrder()     // order by statement name                                                     TOTAL Order over Bulk
    };
  }

  public int compare(DbOperation o1, DbOperation o2) {

    if(o1.equals(o2)) {
      return 0;
    }

    for (DbOperationComparator comparator : delegateComparators) {
      if(comparator.isApplicableTo(o1) && comparator.isApplicableTo(o2)) {
        int idx = comparator.compare(o1, o2);
        if(idx != 0) {
          return idx;
        }
      }
    }

    return 0;
  }

}
