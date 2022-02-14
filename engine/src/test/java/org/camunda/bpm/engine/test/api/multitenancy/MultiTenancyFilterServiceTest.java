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
package org.camunda.bpm.engine.test.api.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultiTenancyFilterServiceTest extends PluggableProcessEngineTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";
  protected static final String[] TENANT_IDS = new String[] {TENANT_ONE, TENANT_TWO};

  protected String filterId = null;
  protected final List<String> taskIds = new ArrayList<String>();

  @Before
  public void setUp() throws Exception {
    createTaskWithoutTenantId();
    createTaskForTenant(TENANT_ONE);
    createTaskForTenant(TENANT_TWO);
  }

  @Test
  public void testCreateFilterWithTenantIdCriteria() {
    TaskQuery query = taskService.createTaskQuery().tenantIdIn(TENANT_IDS);
    filterId = createFilter(query);

    Filter savedFilter = filterService.getFilter(filterId);
    TaskQueryImpl savedQuery = savedFilter.getQuery();

    assertThat(savedQuery.getTenantIds()).isEqualTo(TENANT_IDS);
  }

  @Test
  public void testCreateFilterWithNoTenantIdCriteria() {
    TaskQuery query = taskService.createTaskQuery().withoutTenantId();
    filterId = createFilter(query);

    Filter savedFilter = filterService.getFilter(filterId);
    TaskQueryImpl savedQuery = savedFilter.getQuery();

    assertThat(savedQuery.isWithoutTenantId()).isTrue();
    assertThat(savedQuery.getTenantIds()).isNull();
  }

  @Test
  public void testFilterTasksNoTenantIdSet() {
    TaskQuery query = taskService.createTaskQuery();
    filterId = createFilter(query);

    assertThat(filterService.count(filterId)).isEqualTo(3L);
  }

  @Test
  public void testFilterTasksByTenantIds() {
    TaskQuery query = taskService.createTaskQuery().tenantIdIn(TENANT_IDS);
    filterId = createFilter(query);

    assertThat(filterService.count(filterId)).isEqualTo(2L);

    TaskQuery extendingQuery = taskService.createTaskQuery().taskName("testTask");
    assertThat(filterService.count(filterId, extendingQuery)).isEqualTo(2L);
  }

  @Test
  public void testFilterTasksWithoutTenantId() {
    TaskQuery query = taskService.createTaskQuery().withoutTenantId();
    filterId = createFilter(query);

    assertThat(filterService.count(filterId)).isEqualTo(1L);

    TaskQuery extendingQuery = taskService.createTaskQuery().taskName("testTask");
    assertThat(filterService.count(filterId, extendingQuery)).isEqualTo(1L);
  }

  @Test
  public void testFilterTasksByExtendingQueryWithTenantId() {
    TaskQuery query = taskService.createTaskQuery().taskName("testTask");
    filterId = createFilter(query);

    TaskQuery extendingQuery = taskService.createTaskQuery().tenantIdIn(TENANT_ONE);
    assertThat(filterService.count(filterId, extendingQuery)).isEqualTo(1L);
  }

  @Test
  public void testFilterTasksByExtendingQueryWithoutTenantId() {
    TaskQuery query = taskService.createTaskQuery().taskName("testTask");
    filterId = createFilter(query);

    TaskQuery extendingQuery = taskService.createTaskQuery().withoutTenantId();
    assertThat(filterService.count(filterId, extendingQuery)).isEqualTo(1L);
  }

  @Test
  public void testFilterTasksWithNoAuthenticatedTenants() {
    TaskQuery query = taskService.createTaskQuery();
    filterId = createFilter(query);

    identityService.setAuthentication("user", null, null);

    assertThat(filterService.count(filterId)).isEqualTo(1L);
  }

  @Test
  public void testFilterTasksWithAuthenticatedTenant() {
    TaskQuery query = taskService.createTaskQuery();
    filterId = createFilter(query);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    assertThat(filterService.count(filterId)).isEqualTo(2L);
  }

  @Test
  public void testFilterTasksWithAuthenticatedTenants() {
    TaskQuery query = taskService.createTaskQuery();
    filterId = createFilter(query);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    assertThat(filterService.count(filterId)).isEqualTo(3L);
  }

  @Test
  public void testFilterTasksByTenantIdNoAuthenticatedTenants() {
    TaskQuery query = taskService.createTaskQuery().tenantIdIn(TENANT_ONE);
    filterId = createFilter(query);

    identityService.setAuthentication("user", null, null);

    assertThat(filterService.count(filterId)).isEqualTo(0L);
  }

  @Test
  public void testFilterTasksByTenantIdWithAuthenticatedTenant() {
    TaskQuery query = taskService.createTaskQuery().tenantIdIn(TENANT_ONE);
    filterId = createFilter(query);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    assertThat(filterService.count(filterId)).isEqualTo(1L);
  }

  @Test
  public void testFilterTasksByExtendingQueryWithTenantIdNoAuthenticatedTenants() {
    TaskQuery query = taskService.createTaskQuery().taskName("testTask");
    filterId = createFilter(query);

    identityService.setAuthentication("user", null, null);

    TaskQuery extendingQuery = taskService.createTaskQuery().tenantIdIn(TENANT_ONE);
    assertThat(filterService.count(filterId, extendingQuery)).isEqualTo(0L);
  }

  @Test
  public void testFilterTasksByExtendingQueryWithTenantIdAuthenticatedTenant() {
    TaskQuery query = taskService.createTaskQuery().taskName("testTask");
    filterId = createFilter(query);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    TaskQuery extendingQuery = taskService.createTaskQuery().tenantIdIn(TENANT_ONE);
    assertThat(filterService.count(filterId, extendingQuery)).isEqualTo(1L);
  }

  @Test
  public void testFilterTasksWithDisabledTenantCheck() {
    TaskQuery query = taskService.createTaskQuery();
    filterId = createFilter(query);

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    assertThat(filterService.count(filterId)).isEqualTo(3L);
  }

  protected void createTaskWithoutTenantId() {
    createTaskForTenant(null);
  }

  protected void createTaskForTenant(String tenantId) {
    Task newTask = taskService.newTask();
    newTask.setName("testTask");

    if(tenantId != null) {
      newTask.setTenantId(tenantId);
    }

    taskService.saveTask(newTask);

    taskIds.add(newTask.getId());
  }

  protected String createFilter(TaskQuery query) {
    Filter newFilter = filterService.newTaskFilter("myFilter");
    newFilter.setQuery(query);

    return filterService.saveFilter(newFilter).getId();
  }

  @After
  public void tearDown() throws Exception {
    filterService.deleteFilter(filterId);
    identityService.clearAuthentication();
    for(String taskId : taskIds) {
      taskService.deleteTask(taskId, true);
    }
  }
}
