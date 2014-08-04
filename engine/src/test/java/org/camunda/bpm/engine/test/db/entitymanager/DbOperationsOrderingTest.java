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
package org.camunda.bpm.engine.test.db.entitymanager;

import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.DELETE;
import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.INSERT;
import static org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType.UPDATE;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationType;
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

  protected DbEntityManager entityManager;

  // setup some entities
  ExecutionEntity execution1 = null;
  ExecutionEntity execution2 = null;
  ExecutionEntity execution3 = null;
  ExecutionEntity execution4 = null;

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
    DbSqlSessionFactory dbSqlSessionFactory = new DbSqlSessionFactory();
    TestIdGenerator idGenerator = new TestIdGenerator();
    dbSqlSessionFactory.setIdGenerator(idGenerator);
    entityManager = new DbEntityManager(dbSqlSessionFactory, null);

    execution1 = new ExecutionEntity();
    execution1.setId("100");
    execution2 = new ExecutionEntity();
    execution2.setId("101");
    execution3 = new ExecutionEntity();
    execution3.setId("102");
    execution4 = new ExecutionEntity();
    execution4.setId("103");

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
  public void insertsShouldComeFirst() {

    entityManager.delete(variable2);
    entityManager.delete(execution2);
    entityManager.insert(variable1);
    entityManager.insert(task1);
    entityManager.update(task2);
    entityManager.insert(execution1);

    List<DbOperation> operations = entityManager.getOperationManager().getDbOperations();

    assertOperation(INSERT, execution1, operations.get(0));
    assertOperation(INSERT, task1, operations.get(1));
    assertOperation(INSERT, variable1, operations.get(2));
    assertOperation(UPDATE, task2, operations.get(4));
    assertOperation(DELETE, variable2, operations.get(3));
    assertOperation(DELETE, execution2, operations.get(5));

  }

  @Test
  public void shouldOrderInsertsByReference() {

    // if execution 3 is parent of execution 1
    execution1.setParentId(execution3.getId());

    entityManager.insert(execution1);
    entityManager.insert(execution2);
    entityManager.insert(execution3);

    List<DbOperation> operations = entityManager.getOperationManager().getDbOperations();

    // then execution 3 is inserted before execution 1
    assertOperation(INSERT, execution2, operations.get(0));
    assertOperation(INSERT, execution3, operations.get(1));
    assertOperation(INSERT, execution1, operations.get(2));
  }

  @Test
  public void shouldOrderInsertsByReference2() {

    // if execution 3 is parent of execution 1
    execution1.setParentId(execution3.getId());

    entityManager.insert(execution2);
    entityManager.insert(execution3);
    entityManager.insert(execution1);

    List<DbOperation> operations = entityManager.getOperationManager().getDbOperations();

    // then execution 3 is inserted before execution 1
    assertOperation(INSERT, execution2, operations.get(0));
    assertOperation(INSERT, execution3, operations.get(1));
    assertOperation(INSERT, execution1, operations.get(2));
  }

  @Test
  public void shouldOrderDeletesByReference() {

    // if execution 3 is parent of execution 1
    execution1.setParentId(execution3.getId());

    entityManager.delete(execution2);
    entityManager.delete(execution3);
    entityManager.delete(execution1);

    List<DbOperation> operations = entityManager.getOperationManager().getDbOperations();

    // then execution 1 is deleted before execution 3
    assertOperation(DELETE, execution1, operations.get(0));
    assertOperation(DELETE, execution2, operations.get(1));
    assertOperation(DELETE, execution3, operations.get(2));
  }

  protected void assertOperation(DbOperationType expectedOperationTyoe, DbEntity expectedEntity, DbOperation actualOperation) {
    DbEntityOperation entityOperation = (DbEntityOperation) actualOperation;
    assertEquals("Expected "+expectedOperationTyoe+" of "+expectedEntity, expectedOperationTyoe, entityOperation.getOperationType());
    assertEquals("Expected "+expectedOperationTyoe+" of "+expectedEntity, expectedEntity, entityOperation.getEntity());
  }

}
