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
package org.camunda.bpm.engine.test.standalone.db.entitymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class DbOperationsOrderingTest {

  protected ExposingDbEntityManager entityManager;

  // setup some entities
  ExecutionEntity execution1 = null;
  ExecutionEntity execution2 = null;
  ExecutionEntity execution3 = null;
  ExecutionEntity execution4 = null;
  ExecutionEntity execution5 = null;
  ExecutionEntity execution6 = null;
  ExecutionEntity execution7 = null;
  ExecutionEntity execution8 = null;

  TaskEntity task1 = null;
  TaskEntity task2 = null;
  TaskEntity task3 = null;
  TaskEntity task4 = null;

  VariableInstanceEntity variable1 = null;
  VariableInstanceEntity variable2 = null;
  VariableInstanceEntity variable3 = null;
  VariableInstanceEntity variable4 = null;


  @Before
  public void setup() {
    TestIdGenerator idGenerator = new TestIdGenerator();
    entityManager = new ExposingDbEntityManager(idGenerator, null);

    execution1 = new ExecutionEntity();
    execution1.setId("101");
    execution2 = new ExecutionEntity();
    execution2.setId("102");
    execution3 = new ExecutionEntity();
    execution3.setId("103");
    execution4 = new ExecutionEntity();
    execution4.setId("104");
    execution5 = new ExecutionEntity();
    execution5.setId("105");
    execution6 = new ExecutionEntity();
    execution6.setId("106");
    execution7 = new ExecutionEntity();
    execution7.setId("107");
    execution8 = new ExecutionEntity();
    execution8.setId("108");

    task1 = new TaskEntity();
    task1.setId("104");
    task2 = new TaskEntity();
    task2.setId("105");
    task3 = new TaskEntity();
    task3.setId("106");
    task4 = new TaskEntity();
    task4.setId("107");

    variable1 = new VariableInstanceEntity();
    variable1.setId("108");
    variable2 = new VariableInstanceEntity();
    variable2.setId("109");
    variable3 = new VariableInstanceEntity();
    variable3.setId("110");
    variable4 = new VariableInstanceEntity();
    variable4.setId("111");
  }

  @Test
  public void testInsertSingleEntity() {

    entityManager.insert(execution1);
    entityManager.flushEntityCache();

    List<DbOperation> flush = entityManager.getDbOperationManager().calculateFlush();
    assertEquals(1, flush.size());
  }

  @Test
  public void testInsertReferenceOrdering() {

    execution2.setParentExecution(execution3);

    entityManager.insert(execution2);
    entityManager.insert(execution3);

    // the parent (3) is inserted before the child (2)
    entityManager.flushEntityCache();
    List<DbOperation> flush = entityManager.getDbOperationManager().calculateFlush();
    assertHappensAfter(execution2, execution3, flush);

  }


  @Test
  public void testInsertReferenceOrderingAndIdOrdering() {

    execution2.setParentExecution(execution3);

    entityManager.insert(execution2);
    entityManager.insert(execution3);
    entityManager.insert(execution1);

    // the parent (3) is inserted before the child (2)
    entityManager.flushEntityCache();
    List<DbOperation> flush = entityManager.getDbOperationManager().calculateFlush();
    assertHappensAfter(execution2, execution3, flush);
    assertHappensAfter(execution3, execution1, flush);
    assertHappensAfter(execution2, execution1, flush);

  }

  @Test
  public void testInsertReferenceOrderingMultipleTrees() {

    // tree1
    execution3.setParentExecution(execution4);
    execution2.setParentExecution(execution4);
    execution5.setParentExecution(execution3);

    // tree2
    execution1.setParentExecution(execution8);

    entityManager.insert(execution8);
    entityManager.insert(execution6);
    entityManager.insert(execution2);
    entityManager.insert(execution5);
    entityManager.insert(execution1);
    entityManager.insert(execution4);
    entityManager.insert(execution7);
    entityManager.insert(execution3);

    // the parent (3) is inserted before the child (2)
    entityManager.flushEntityCache();
    List<DbOperation> insertOperations = entityManager.getDbOperationManager().calculateFlush();
    assertHappensAfter(execution3, execution4, insertOperations);
    assertHappensAfter(execution2, execution4, insertOperations);
    assertHappensAfter(execution5, execution3, insertOperations);
    assertHappensAfter(execution1, execution8, insertOperations);

  }

  @Test
  public void testDeleteReferenceOrdering() {
    // given
    execution1.setParentExecution(execution2);
    entityManager.getDbEntityCache().putPersistent(execution1);
    entityManager.getDbEntityCache().putPersistent(execution2);

    // when deleting the entities
    entityManager.delete(execution1);
    entityManager.delete(execution2);

    entityManager.flushEntityCache();

    // then the flush is based on the persistent relationships
    List<DbOperation> deleteOperations = entityManager.getDbOperationManager().calculateFlush();
    assertHappensBefore(execution1, execution2, deleteOperations);
  }

  @Test
  public void testDeleteReferenceOrderingAfterTransientUpdate() {
    // given
    execution1.setParentExecution(execution2);
    entityManager.getDbEntityCache().putPersistent(execution1);
    entityManager.getDbEntityCache().putPersistent(execution2);

    // when reverting the relation in memory
    execution1.setParentExecution(null);
    execution2.setParentExecution(execution1);

    // and deleting the entities
    entityManager.delete(execution1);
    entityManager.delete(execution2);

    entityManager.flushEntityCache();

    // then the flush is based on the persistent relationships
    List<DbOperation> deleteOperations = entityManager.getDbOperationManager().calculateFlush();
    assertHappensBefore(execution1, execution2, deleteOperations);
  }

  protected void assertHappensAfter(DbEntity entity1, DbEntity entity2, List<DbOperation> operations) {
    int idx1 = indexOfEntity(entity1, operations);
    int idx2 = indexOfEntity(entity2, operations);
    assertTrue("operation for " + entity1 + " should be executed after operation for " + entity2, idx1 > idx2);
  }

  protected void assertHappensBefore(DbEntity entity1, DbEntity entity2, List<DbOperation> operations) {
    int idx1 = indexOfEntity(entity1, operations);
    int idx2 = indexOfEntity(entity2, operations);
    assertTrue("operation for " + entity1 + " should be executed before operation for " + entity2, idx1 < idx2);
  }

  protected int indexOfEntity(DbEntity entity, List<DbOperation> operations) {
    for (int i = 0; i < operations.size(); i++) {
      if(entity == ((DbEntityOperation) operations.get(i)).getEntity()) {
        return i;
      }
    }
    return -1;
  }

  @Test
  public void testInsertIdOrdering() {

    entityManager.insert(execution1);
    entityManager.insert(execution2);

    entityManager.flushEntityCache();
    List<DbOperation> insertOperations = entityManager.getDbOperationManager().calculateFlush();
    assertHappensAfter(execution2, execution1, insertOperations);
  }

  public static class ExposingDbEntityManager extends DbEntityManager {

    public ExposingDbEntityManager(IdGenerator idGenerator, PersistenceSession persistenceSession) {
      super(idGenerator, persistenceSession);
    }

    /**
     * Expose this method for test purposes
     */
    public void flushEntityCache() {
      super.flushEntityCache();
    }
  }


}
