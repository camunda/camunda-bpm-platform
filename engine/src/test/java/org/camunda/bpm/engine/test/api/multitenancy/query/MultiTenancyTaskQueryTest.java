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
package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class MultiTenancyTaskQueryTest extends PluggableProcessEngineTest {

  private static final String TENANT_ONE = "tenant1";
  private static final String TENANT_TWO = "tenant2";
  private static final String TENANT_NON_EXISTING = "nonExistingTenant";

  private final List<String> taskIds = new ArrayList<>();

  @Before
  public void setUp() throws Exception {

    createTaskWithoutTenant();
    createTaskForTenant(TENANT_ONE);
    createTaskForTenant(TENANT_TWO);
  }

  @Test
  public void testQueryNoTenantIdSet() {
    TaskQuery query = taskService.createTaskQuery();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByTenantId() {
    TaskQuery query = taskService.createTaskQuery()
      .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    query = taskService.createTaskQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIds() {
    TaskQuery query = taskService.createTaskQuery()
      .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count()).isEqualTo(2L);

    query = taskService.createTaskQuery()
        .tenantIdIn(TENANT_ONE, TENANT_NON_EXISTING);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTasksWithoutTenantId() {
    TaskQuery query = taskService.createTaskQuery()
      .withoutTenantId();

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryWithAndWithoutTenantId() {
    // when
    TaskQuery query = taskService.createTaskQuery()
        .or()
          .tenantIdIn(TENANT_ONE, TENANT_TWO)
          .withoutTenantId()
        .endOr();

    // then
    assertThat(query.list().size()).isEqualTo(3);
  }

  @Test
  public void testQueryByNonExistingTenantId() {
    TaskQuery query = taskService.createTaskQuery()
      .tenantIdIn(TENANT_NON_EXISTING);

    assertThat(query.count()).isEqualTo(0L);
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
  public void testQuerySortingAsc() {
    // exclude tasks without tenant id because of database-specific ordering
    List<Task> tasks = taskService.createTaskQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(tasks.size()).isEqualTo(2);
    assertThat(tasks.get(0).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(tasks.get(1).getTenantId()).isEqualTo(TENANT_TWO);
  }

  @Test
  public void testQuerySortingDesc() {
    // exclude tasks without tenant id because of database-specific ordering
    List<Task> tasks = taskService.createTaskQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(tasks).hasSize(2);
    assertThat(tasks.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(tasks.get(1).getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    TaskQuery query = taskService.createTaskQuery();

    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    TaskQuery query = taskService.createTaskQuery();

    assertThat(query.count()).isEqualTo(3L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(taskService.createTaskQuery().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(3L);
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

  @After
  public void tearDown() throws Exception {
    identityService.clearAuthentication();
    for (String taskId : taskIds) {
      taskService.deleteTask(taskId, true);
    }
    taskIds.clear();
  }

}
