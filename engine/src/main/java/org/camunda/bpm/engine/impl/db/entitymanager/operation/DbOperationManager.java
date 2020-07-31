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
package org.camunda.bpm.engine.impl.db.entitymanager.operation;

import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.DELETE;
import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.INSERT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.comparator.DbBulkOperationComparator;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.comparator.DbEntityOperationComparator;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.comparator.EntityTypeComparatorForInserts;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.comparator.EntityTypeComparatorForModifications;

/**
 * Manages a set of {@link DbOperation database operations}.
 *
 * @author Daniel Meyer
 *
 */
public class DbOperationManager {

  // comparators ////////////////

  public static Comparator<Class<?>> INSERT_TYPE_COMPARATOR = new EntityTypeComparatorForInserts();
  public static Comparator<Class<?>> MODIFICATION_TYPE_COMPARATOR = new EntityTypeComparatorForModifications();
  public static Comparator<DbEntityOperation> INSERT_OPERATION_COMPARATOR = new DbEntityOperationComparator();
  public static Comparator<DbEntityOperation> MODIFICATION_OPERATION_COMPARATOR  = new DbEntityOperationComparator();
  public static Comparator<DbBulkOperation> BULK_OPERATION_COMPARATOR = new DbBulkOperationComparator();

  // pre-sorted operation maps //////////////

  /** INSERTs */
  public SortedMap<Class<?>, SortedSet<DbEntityOperation>> inserts = new TreeMap<Class<?>, SortedSet<DbEntityOperation>>(INSERT_TYPE_COMPARATOR);

  /** UPDATEs of a single entity */
  public SortedMap<Class<?>, SortedSet<DbEntityOperation>> updates = new TreeMap<Class<?>, SortedSet<DbEntityOperation>>(MODIFICATION_TYPE_COMPARATOR);

  /** DELETEs of a single entity */
  public SortedMap<Class<?>, SortedSet<DbEntityOperation>> deletes = new TreeMap<Class<?>, SortedSet<DbEntityOperation>>(MODIFICATION_TYPE_COMPARATOR);

  /** bulk modifications (DELETE, UPDATE) on an entity collection */
  public SortedMap<Class<?>, SortedSet<DbBulkOperation>> bulkOperations = new TreeMap<Class<?>, SortedSet<DbBulkOperation>>(MODIFICATION_TYPE_COMPARATOR);

  /** bulk modifications (DELETE, UPDATE) for which order of execution is important */
  public LinkedHashSet<DbBulkOperation> bulkOperationsInsertionOrder = new LinkedHashSet<DbBulkOperation>();

  public boolean addOperation(DbEntityOperation newOperation) {
    if(newOperation.getOperationType() == INSERT) {
      return getInsertsForType(newOperation.getEntityType(), true)
          .add(newOperation);

    } else if(newOperation.getOperationType() == DELETE) {
      return getDeletesByType(newOperation.getEntityType(), true)
          .add(newOperation);

    } else { // UPDATE
      return getUpdatesByType(newOperation.getEntityType(), true)
          .add(newOperation);

    }
  }

  protected SortedSet<DbEntityOperation> getDeletesByType(Class<? extends DbEntity> type, boolean create) {
    SortedSet<DbEntityOperation> deletesByType = deletes.get(type);
    if(deletesByType == null && create) {
      deletesByType = new TreeSet<DbEntityOperation>(MODIFICATION_OPERATION_COMPARATOR);
      deletes.put(type, deletesByType);
    }
    return deletesByType;
  }

  protected SortedSet<DbEntityOperation> getUpdatesByType(Class<? extends DbEntity> type, boolean create) {
    SortedSet<DbEntityOperation> updatesByType = updates.get(type);
    if(updatesByType == null && create) {
      updatesByType = new TreeSet<DbEntityOperation>(MODIFICATION_OPERATION_COMPARATOR);
      updates.put(type, updatesByType);
    }
    return updatesByType;
  }

  protected SortedSet<DbEntityOperation> getInsertsForType(Class<? extends DbEntity> type, boolean create) {
    SortedSet<DbEntityOperation> insertsByType = inserts.get(type);
    if(insertsByType == null && create) {
      insertsByType = new TreeSet<DbEntityOperation>(INSERT_OPERATION_COMPARATOR);
      inserts.put(type, insertsByType);
    }
    return insertsByType;
  }

  public boolean addOperation(DbBulkOperation newOperation) {
    SortedSet<DbBulkOperation> bulksByType = bulkOperations.get(newOperation.getEntityType());
    if(bulksByType == null) {
      bulksByType = new TreeSet<DbBulkOperation>(BULK_OPERATION_COMPARATOR);
      bulkOperations.put(newOperation.getEntityType(), bulksByType);
    }

    return bulksByType.add(newOperation);
  }

  public boolean addOperationPreserveOrder(DbBulkOperation newOperation) {
    return bulkOperationsInsertionOrder.add(newOperation);
  }

  public List<DbOperation> calculateFlush() {
    List<DbOperation> flush = new ArrayList<DbOperation>();
    // first INSERTs
    addSortedInserts(flush);
    // then UPDATEs + DELETEs
    addSortedModifications(flush);
    
    determineDependencies(flush);
    return flush;
  }

  /** Adds the insert operations to the flush (in correct order).
   * @param operationsForFlush */
  protected void addSortedInserts(List<DbOperation> flush) {
    for (Entry<Class<?>, SortedSet<DbEntityOperation>> operationsForType : inserts.entrySet()) {

      // add inserts to flush
      if(HasDbReferences.class.isAssignableFrom(operationsForType.getKey())) {
        // if this type has self references, we need to resolve the reference order
        flush.addAll(sortByReferences(operationsForType.getValue()));
      } else {
        flush.addAll(operationsForType.getValue());
      }
    }
  }

  /** Adds a correctly ordered list of UPDATE and DELETE operations to the flush.
   * @param flush */
  protected void addSortedModifications(List<DbOperation> flush) {

    // calculate sorted set of all modified entity types
    SortedSet<Class<?>> modifiedEntityTypes = new TreeSet<Class<?>>(MODIFICATION_TYPE_COMPARATOR);
    modifiedEntityTypes.addAll(updates.keySet());
    modifiedEntityTypes.addAll(deletes.keySet());
    modifiedEntityTypes.addAll(bulkOperations.keySet());

    for (Class<?> type : modifiedEntityTypes) {
      // first perform entity UPDATES
      addSortedModificationsForType(type, updates.get(type), flush);
      // next perform entity DELETES
      addSortedModificationsForType(type, deletes.get(type), flush);
      // last perform bulk operations
      SortedSet<DbBulkOperation> bulkOperationsForType = bulkOperations.get(type);
      if(bulkOperationsForType != null) {
        flush.addAll(bulkOperationsForType);
      }
    }

    //the very last perform bulk operations for which the order is important
    if(bulkOperationsInsertionOrder != null) {
      flush.addAll(bulkOperationsInsertionOrder);
    }
  }

  protected void addSortedModificationsForType(Class<?> type, SortedSet<DbEntityOperation> preSortedOperations, List<DbOperation> flush) {
    if(preSortedOperations != null) {
      if(HasDbReferences.class.isAssignableFrom(type)) {
        // if this type has self references, we need to resolve the reference order
        flush.addAll(sortByReferences(preSortedOperations));
      } else {
        flush.addAll(preSortedOperations);
      }
    }
  }


  /**
   * Assumptions:
   * a) all operations in the set work on entities such that the entities implement {@link HasDbReferences}.
   * b) all operations in the set work on the same type (ie. all operations are INSERTs or DELETEs).
   *
   */
  protected List<DbEntityOperation> sortByReferences(SortedSet<DbEntityOperation> preSorted) {
    // copy the pre-sorted set and apply final sorting to list
    List<DbEntityOperation> opList = new ArrayList<DbEntityOperation>(preSorted);

    for (int i = 0; i < opList.size(); i++) {

      DbEntityOperation currentOperation = opList.get(i);
      DbEntity currentEntity = currentOperation.getEntity();
      Set<String> currentReferences = currentOperation.getFlushRelevantEntityReferences();

      // check whether this operation must be placed after another operation
      int moveTo = i;
      for(int k = i+1; k < opList.size(); k++) {
        DbEntityOperation otherOperation = opList.get(k);
        DbEntity otherEntity = otherOperation.getEntity();
        Set<String> otherReferences = otherOperation.getFlushRelevantEntityReferences();

        if(currentOperation.getOperationType() == INSERT) {


          // if we reference the other entity, we need to be inserted after that entity
          if(currentReferences != null && currentReferences.contains(otherEntity.getId())) {
            moveTo = k;
            break; // we can only reference a single entity
          }

        } else { // UPDATE or DELETE

          // if the other entity has a reference to us, we must be placed after the other entity
          if(otherReferences != null && otherReferences.contains(currentEntity.getId())) {
            moveTo = k;
            // cannot break, there may be another entity further to the right which also references us
          }

        }
      }

      if(moveTo > i) {
        opList.remove(i);
        opList.add(moveTo, currentOperation);
        i--;
      }
    }

    return opList;
  }

  protected void determineDependencies(List<DbOperation> flush) {
    TreeSet<DbEntityOperation> defaultValue = new TreeSet<DbEntityOperation>();
    for (DbOperation operation : flush) {
      if (operation instanceof DbEntityOperation) {
        DbEntity entity = ((DbEntityOperation) operation).getEntity();
        if (entity instanceof HasDbReferences) {
          Map<String, Class> dependentEntities = ((HasDbReferences) entity).getDependentEntities();

          if (dependentEntities != null) {
            dependentEntities.forEach((id, type) -> {

              deletes.getOrDefault(type, defaultValue).forEach(o -> {
                if (id.equals(o.getEntity().getId())) {
                  o.setDependency(operation);
                }
              });
            });
          }

        }
      }
    }
  }
}
