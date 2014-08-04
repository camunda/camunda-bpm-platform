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

import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.INSERT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.order.DbOperationPreOrdering;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.order.EntityReferenceOrder;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.order.EntityTypeOrder;

/**
 * Manages a set of {@link DbOperation database operations}.
 *
 * @author Daniel Meyer
 *
 */
public class DbOperationManager {

  // the comparators used by the manager to calculate pre and post ordering

  public static DbOperationPreOrdering preOrderer = new DbOperationPreOrdering();

  public static EntityReferenceOrder entityReferenceOrder = new EntityReferenceOrder();
  public static EntityTypeOrder entityTypeOrder = new EntityTypeOrder();

  /**
   * A sorted set of pending operations.
   *
   * At any point in time, this set is guaranteed to be
   *
   * a) minimal: for each pair of operations in the set, one operation does not cancel out the other.
   *             concretely speaking this means that:
   *             - there is no DELETE and UPDATE on the same entity,
   *             - there is no DELETE and INSERT in the same entity
   *
   * b) pre-ordered: operations are ordered as induced by the {@link #preOrderer} comparator.
   */
  protected TreeSet<DbOperation> dbOperations = new TreeSet<DbOperation>(preOrderer);

  /**
   * A set of operations which are also present in {@link #dbOperations} but which need to be post processed.
   * Post processing means that their final ordering needs to be determined by post processing by applying
   * the {@link EntityReferenceOrder} to the set.
   */
  protected Set<DbOperation> operationsForPostProcessing = new HashSet<DbOperation>();

  /**
   * Adds a new {@link DbBulkOperation} to the set of operations.
   *
   * @param newOperation the new bulk operation to add
   * @return true if the operation was added.
   */
  public boolean addOperation(DbBulkOperation newOperation) {
    return dbOperations.add(newOperation);
  }

  /**
   * Adds a new DbEntityOperation to the set of operations.
   *
   * @param newOperation the operation to add to the existing set of operations.
   * @return true if the operations was added to the set of operations
   */
  public boolean addOperation(DbEntityOperation newOperation) {
    boolean isIgnored = false;

    // if the operation is already in the set: ignore
    if(dbOperations.contains(newOperation)) {
      return false;

    // otherwise check whether this operation cancels out another
    // operation or is cancelled out by another operation or both
    } else {
      Iterator<DbOperation> iterator = dbOperations.iterator();
      while (iterator.hasNext()) {
        DbOperation dbOperation = (DbOperation) iterator.next();

        // only applicable if it is a DbEntityOperation
        if (dbOperation instanceof DbEntityOperation) {
          DbEntityOperation currentOperation = (DbEntityOperation) dbOperation;

          // newOperation is an UPDATE to an object which is being DELETED by the currentOperation
          if(newOperation.isDeletedBy(currentOperation)) {
            // => ignore the newOperation
            isIgnored = true;
            break;

          // the currentOperation INSERTS or UPDATES an entity which is DELETED by the newOperation
          } else if(currentOperation.isDeletedBy(newOperation)) {
            // => remove the current operation
            iterator.remove();

            // if the current operation was an INSERT then we must ignore both operations
            if(currentOperation.getOperationType() == INSERT) {
              isIgnored = true;
              break;
            }
          }
        }
      }

      // add new operation
      if(!isIgnored) {
        dbOperations.add(newOperation);
      }
    }

    return !isIgnored;
  }

  protected List<DbOperation> calculateDbReferenceOrdering() {
    ArrayList<DbOperation> sortedOperations = new ArrayList<DbOperation>(dbOperations);
    int numOfOperations = sortedOperations.size();

    for (int i = 0; i < numOfOperations; i++) {
      DbOperation currentOperation = sortedOperations.get(i);

      // check whether this operation needs post processing
      if(HasDbReferences.class.isAssignableFrom(currentOperation.getEntityType())) {
        int k = i;
        int newIndex = i;

        // go back until we either
        // reach the lower bound for the given type
        // or reach an operation such that the current operation needs to precede it
        DbOperation dbOperation = null;
        while (k > 1
            && entityTypeOrder.compare(dbOperation = sortedOperations.get(k-1), currentOperation) == 0
            && dbOperation instanceof DbEntityOperation) {
          k--;
          if(entityReferenceOrder.compare((DbEntityOperation)dbOperation, (DbEntityOperation)currentOperation) > 0) {
            newIndex = k;
          }
        }

        if(newIndex == i) {
          // do the same in the other direction
          k = i; // reset k
          while (k < numOfOperations-1
              && entityTypeOrder.compare(dbOperation = sortedOperations.get(k+1), currentOperation) == 0
              && dbOperation instanceof DbEntityOperation) {
            k++;
            if(entityReferenceOrder.compare((DbEntityOperation)dbOperation, (DbEntityOperation)currentOperation) < 0) {
              newIndex = k;
            }
          }
        }

        // move operation to new index:
        if(i != newIndex) {
          sortedOperations.remove(i);
          sortedOperations.add(newIndex, currentOperation);

          // make sure we do not skip an operation
          if(i<newIndex) {
            i--;
          }
        }

      }
    }

    return sortedOperations;
  }

  public List<DbOperation> getDbOperations() {
    return calculateDbReferenceOrdering();
  }

  public boolean isDeleted(Object object) {
    for (DbOperation dbOperation : dbOperations) {
      if (dbOperation instanceof DbEntityOperation) {
        DbEntityOperation entityOperation = (DbEntityOperation) dbOperation;
        if(object.equals(entityOperation.getEntity())) {
          return true;
        }
      }
    }
    return false;
  }
}
