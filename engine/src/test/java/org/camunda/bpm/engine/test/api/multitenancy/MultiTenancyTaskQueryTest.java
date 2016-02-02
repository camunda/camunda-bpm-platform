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
package org.camunda.bpm.engine.test.api.multitenancy;

import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class MultiTenancyTaskQueryTest extends PluggableProcessEngineTestCase {

  private static final String TENANT1 = "tenant1";
  private static final String TENANT2 = "tenant2";
  private static final String TENANT3 = "tenant3";

  @Test
  public void testQueryByTenantId() {
    final String task1 = createTaskForTenant(TENANT1);
    final String task2 = createTaskForTenant(TENANT2);

    assertEquals(1, taskService.createTaskQuery()
      .tenantIdIn(TENANT1)
      .count());

    assertEquals(1, taskService.createTaskQuery()
        .tenantIdIn(TENANT2)
        .count());

    assertEquals(0, taskService.createTaskQuery()
        .tenantIdIn(TENANT3)
        .count());

    assertEquals(2, taskService.createTaskQuery()
        .tenantIdIn(TENANT1, TENANT2)
        .count());

    assertEquals(1, taskService.createTaskQuery()
        .tenantIdIn(TENANT1, TENANT3)
        .count());

    deleteTask(task1);
    deleteTask(task2);
  }

  @Test
  public void testQueryByTenantIdNullFails() {
    try {
      assertEquals(0, taskService.createTaskQuery()
          .tenantIdIn((String)null));

      fail("Exception expected");
    }
    catch(NullValueException e) {
      // expected
    }
  }


  @Test
  public void testOrderByTenantId() {
    final String task1 = createTaskForTenant(TENANT1);
    final String task2 = createTaskForTenant(TENANT2);
    final String task3 = createTaskForTenant(TENANT3);

    List<Task> list = null;

    list = taskService.createTaskQuery().orderByTenantId().asc().list();
    assertEquals(task1, list.get(0).getId());
    assertEquals(task2, list.get(1).getId());
    assertEquals(task3, list.get(2).getId());

    list = taskService.createTaskQuery().orderByTenantId().desc().list();
    assertEquals(task3, list.get(0).getId());
    assertEquals(task2, list.get(1).getId());
    assertEquals(task1, list.get(2).getId());

    deleteTask(task1);
    deleteTask(task2);
    deleteTask(task3);
  }

  protected void deleteTask(String taskId) {
    taskService.deleteTask(taskId, true);
  }

  protected String createTaskForTenant(String tenantId) {
    Task task = taskService.newTask();
    task.setTenantId(tenantId);
    taskService.saveTask(task);
    return task.getId();
  }


}
