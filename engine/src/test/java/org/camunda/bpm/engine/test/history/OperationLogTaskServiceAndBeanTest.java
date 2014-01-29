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
package org.camunda.bpm.engine.test.history;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.*;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.*;

/**
 * @author Danny Gräf
 */
public class OperationLogTaskServiceAndBeanTest extends PluggableProcessEngineTestCase {

  public void testBeanPropertyChanges() {
    TaskEntity entity = new TaskEntity();

    // assign and validate changes
    entity.setAssignee("icke");
    Map<String, PropertyChange> changes = entity.getPropertyChanges();
    assertEquals(1, changes.size());
    assertNull(changes.get(ASSIGNEE).getOrgValue());
    assertEquals("icke", changes.get(ASSIGNEE).getNewValue());

    // assign it again
    entity.setAssignee("er");
    changes = entity.getPropertyChanges();
    assertEquals(1, changes.size());

    // original value is still null because the task was not saved
    assertNull(changes.get(ASSIGNEE).getOrgValue());
    assertEquals("er", changes.get(ASSIGNEE).getNewValue());

    // set a due date
    entity.setDueDate(new Date());
    changes = entity.getPropertyChanges();
    assertEquals(2, changes.size());
  }

  public void testNotTrackChangeToTheSameValue() {
    TaskEntity entity = new TaskEntity();

    // get and set a properties
    entity.setPriority(entity.getPriority());
    entity.setOwner(entity.getOwner());
    entity.setFollowUpDate(entity.getFollowUpDate());

    // should not track this change
    assertTrue(entity.getPropertyChanges().isEmpty());
  }

  public void testRemoveChangeWhenSetBackToTheOrgValue() {
    TaskEntity entity = new TaskEntity();

    // set an owner (default is null)
    entity.setOwner("icke");

    // should track this change
    assertFalse(entity.getPropertyChanges().isEmpty());

    // reset the owner
    entity.setOwner(null);

    // the change is removed
    assertTrue(entity.getPropertyChanges().isEmpty());
  }

  public void testAllTrackedProperties() {
    Date yesterday = new Date(new Date().getTime() - 86400000);
    Date tomorrow = new Date(new Date().getTime() + 86400000);

    TaskEntity entity = new TaskEntity();

    // call all tracked setter methods
    entity.setAssignee("er");
    entity.setDelegationState(DelegationState.PENDING);
    entity.setDeleted(true);
    entity.setDescription("a description");
    entity.setDueDate(tomorrow);
    entity.setFollowUpDate(yesterday);
    entity.setName("to do");
    entity.setOwner("icke");
    entity.setParentTaskId("parent");
    entity.setPriority(73);

    // and validate the change list
    Map<String, PropertyChange> changes = entity.getPropertyChanges();
    assertEquals("er", changes.get(ASSIGNEE).getNewValue());
    assertSame(DelegationState.PENDING, changes.get(DELEGATION).getNewValue());
    assertTrue((Boolean) changes.get(DELETE).getNewValue());
    assertEquals("a description", changes.get(DESCRIPTION).getNewValue());
    assertEquals(tomorrow, changes.get(DUE_DATE).getNewValue());
    assertEquals(yesterday, changes.get(FOLLOW_UP_DATE).getNewValue());
    assertEquals("to do", changes.get(NAME).getNewValue());
    assertEquals("icke", changes.get(OWNER).getNewValue());
    assertEquals("parent", changes.get(PARENT_TASK).getNewValue());
    assertEquals(73, changes.get(PRIORITY).getNewValue());
  }

  private Task task;

  public void testDeleteTask() {
    // given: a single task
    task = taskService.newTask();
    taskService.saveTask(task);

    // then: delete the task
    taskService.deleteTask(task.getId(), "duplicated");

    // expect: one entry for the deletion
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_DELETE);
    assertEquals(1, query.count());

    // assert: details
    UserOperationLogEntry delete = query.singleResult();
    assertEquals(DELETE, delete.getProperty());
    assertFalse(Boolean.parseBoolean(delete.getOrgValue()));
    assertTrue(Boolean.parseBoolean(delete.getNewValue()));

    cleanupHistory();
  }

  public void testCompositeBeanInteraction() {
    // given: a manually created task
    task = taskService.newTask();

    // then: save the task without any property change
    taskService.saveTask(task);

    // expect: no entry
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_CREATE);
    UserOperationLogEntry create = query.singleResult();
    assertNotNull(create);
    assertEquals(ENTITY_TYPE_TASK, create.getEntityType());
    assertNull(create.getOrgValue());
    assertNull(create.getNewValue());
    assertNull(create.getProperty());

    task.setAssignee("icke");
    task.setName("to do");

    // then: save the task again
    taskService.saveTask(task);

    // expect: two update entries with the same operation id
    List<UserOperationLogEntry> entries = queryOperationDetails(OPERATION_TYPE_UPDATE).list();
    assertEquals(2, entries.size());
    assertEquals(entries.get(0).getOperationId(), entries.get(1).getOperationId());

    // clean up DB
    taskService.deleteTask(task.getId());
    cleanupHistory();
  }

  public void testMultipleValueChange() {
    // given: a single task
    task = taskService.newTask();
    taskService.saveTask(task);

    // then: change a property twice
    task.setName("a task");
    task.setName("to do");
    taskService.saveTask(task);
    UserOperationLogEntry update = queryOperationDetails(OPERATION_TYPE_UPDATE).singleResult();
    assertNull(update.getOrgValue());
    assertEquals("to do", update.getNewValue());

    // clean up DB
    taskService.deleteTask(task.getId());
    cleanupHistory();
  }

  public void testSetDateProperty() {
    // given: a single task
    task = taskService.newTask();
    Date now = ClockUtil.getCurrentTime();
    task.setDueDate(now);
    taskService.saveTask(task);

    UserOperationLogEntry logEntry = historyService.createUserOperationLogQuery().singleResult();
    assertEquals(String.valueOf(now.getTime()), logEntry.getNewValue());

    // clean up DB
    taskService.deleteTask(task.getId());
    cleanupHistory();
  }

  public void testResetChange() {
    // given: a single task
    task = taskService.newTask();
    taskService.saveTask(task);

    // then: change the name
    String name = "a task";
    task.setName(name);
    taskService.saveTask(task);
    UserOperationLogEntry update = queryOperationDetails(OPERATION_TYPE_UPDATE).singleResult();
    assertNull(update.getOrgValue());
    assertEquals(name, update.getNewValue());

    // then: change the name some times and set it back to the original value
    task.setName("to do 1");
    task.setName("to do 2");
    task.setName(name);
    taskService.saveTask(task);

    // expect: there is no additional change tracked
    update = queryOperationDetails(OPERATION_TYPE_UPDATE).singleResult();
    assertNull(update.getOrgValue());
    assertEquals(name, update.getNewValue());

    // clean up DB
    taskService.deleteTask(task.getId());
    cleanupHistory();
  }

  public void testConcurrentTaskChange() {
    // create a task
    task = taskService.newTask();
    taskService.saveTask(task);

    // change the bean property
    task.setAssignee("icke");

    // use the service method to do an other assignment
    taskService.setAssignee(task.getId(), "er");

    try { // now try to save the task and overwrite the change
      taskService.saveTask(task);
    } catch (Exception e) {
      assertNotNull(e); // concurrent modification
    }

    taskService.deleteTask(task.getId());
    cleanupHistory();
  }

  private UserOperationLogQuery queryOperationDetails(String type) {
    return historyService.createUserOperationLogQuery().operationType(type);
  }

  private void cleanupHistory() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricTaskInstanceManager().deleteHistoricTaskInstanceById(task.getId());
        return null;
      }
    });
  }
}
