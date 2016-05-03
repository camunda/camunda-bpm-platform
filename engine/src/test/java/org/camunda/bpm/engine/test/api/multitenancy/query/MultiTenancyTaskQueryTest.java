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
package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class MultiTenancyTaskQueryTest extends PluggableProcessEngineTestCase {

  private static final String TENANT_ONE = "tenant1";
  private static final String TENANT_TWO = "tenant2";
  private static final String TENANT_NON_EXISTING = "nonExistingTenant";

  private final List<String> taskIds = new ArrayList<String>();

  @Override
  protected void setUp() throws Exception {

    createTaskWithoutTenant();
    createTaskForTenant(TENANT_ONE);
    createTaskForTenant(TENANT_TWO);
  }

  @Test
  public void testQueryNoTenantIdSet() {
    TaskQuery query = taskService.createTaskQuery();

    assertThat(query.count(), is(3L));
  }

  @Test
  public void testQueryByTenantId() {
    TaskQuery query = taskService.createTaskQuery()
      .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = taskService.createTaskQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  @Test
  public void testQueryByTenantIds() {
    TaskQuery query = taskService.createTaskQuery()
      .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));

    query = taskService.createTaskQuery()
        .tenantIdIn(TENANT_ONE, TENANT_NON_EXISTING);

    assertThat(query.count(), is(1L));
  }

  @Test
  public void testQueryByTasksWithoutTenantId() {
    TaskQuery query = taskService.createTaskQuery()
      .withoutTenantId();

    assertThat(query.count(), is(1L));
  }

  @Test
  public void testQueryByNonExistingTenantId() {
    TaskQuery query = taskService.createTaskQuery()
      .tenantIdIn(TENANT_NON_EXISTING);

    assertThat(query.count(), is(0L));
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

  public void testQuerySortingAsc() {
    // exclude tasks without tenant id because of database-specific ordering
    List<Task> tasks = taskService.createTaskQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(tasks.size(), is(2));
    assertThat(tasks.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(tasks.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    // exclude tasks without tenant id because of database-specific ordering
    List<Task> tasks = taskService.createTaskQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(tasks.size(), is(2));
    assertThat(tasks.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(tasks.get(1).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count(), is(1L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    TaskQuery query = taskService.createTaskQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count(), is(1L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    TaskQuery query = taskService.createTaskQuery();

    assertThat(query.count(), is(3L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count(), is(3L));
  }

  protected String createTaskWithoutTenant() {
    return createTaskForTenant(null);
  }

  protected String createTaskForTenant(String tenantId) {
    Task task = taskService.newTask();
    if (tenantId != null) {
      task.setTenantId(tenantId);
    }
    taskService.saveTask(task);

    String taskId = task.getId();
    taskIds.add(taskId);

    return taskId;
  }

  @Override
  protected void tearDown() throws Exception {
    identityService.clearAuthentication();
    for (String taskId : taskIds) {
      taskService.deleteTask(taskId, true);
    }
    taskIds.clear();
  }

}
