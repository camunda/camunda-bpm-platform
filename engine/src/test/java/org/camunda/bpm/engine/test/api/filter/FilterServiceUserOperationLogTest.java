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
package org.camunda.bpm.engine.test.api.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tobias Metzke
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class FilterServiceUserOperationLogTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected FilterService filterService;
  protected HistoryService historyService;
  protected TaskService taskService;
  protected IdentityService identityService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule);

  @Before
  public void setUp() {
    filterService = engineRule.getFilterService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    identityService = engineRule.getIdentityService();
  }

  @After
  public void tearDown() {
    // delete all existing filters
    for (Filter filter : filterService.createTaskFilterQuery().list()) {
      filterService.deleteFilter(filter.getId());
    }
  }

  @Test
  public void testCreateFilter() {
    // given
    Filter filter = filterService.newTaskFilter()
        .setName("name")
        .setOwner("owner")
        .setQuery(taskService.createTaskQuery())
        .setProperties(new HashMap<String, Object>());

    // when
    identityService.setAuthenticatedUserId("userId");
    filterService.saveFilter(filter);
    identityService.clearAuthentication();

    // then
    assertEquals(1L, historyService.createUserOperationLogQuery().count());
    UserOperationLogEntry logEntry = historyService.createUserOperationLogQuery().singleResult();
    assertThat(logEntry.getEntityType()).isEqualTo(EntityTypes.FILTER);
    assertThat(logEntry.getOperationType()).isEqualTo(UserOperationLogEntry.OPERATION_TYPE_CREATE);
    assertThat(logEntry.getProperty()).isEqualTo("filterId");
    assertThat(logEntry.getOrgValue()).isNull();
    assertThat(logEntry.getNewValue()).isEqualTo(filter.getId());
  }

  @Test
  public void testUpdateFilter() {
    // given
    Filter filter = filterService.newTaskFilter()
        .setName("name")
        .setOwner("owner")
        .setQuery(taskService.createTaskQuery())
        .setProperties(new HashMap<String, Object>());
    filterService.saveFilter(filter);

    // when
    identityService.setAuthenticatedUserId("userId");
    filter.setName(filter.getName() + "_new");
    filterService.saveFilter(filter);
    identityService.clearAuthentication();

    // then
    assertEquals(1L, historyService.createUserOperationLogQuery().count());
    UserOperationLogEntry logEntry = historyService.createUserOperationLogQuery().singleResult();
    assertThat(logEntry.getEntityType()).isEqualTo(EntityTypes.FILTER);
    assertThat(logEntry.getOperationType()).isEqualTo(UserOperationLogEntry.OPERATION_TYPE_UPDATE);
    assertThat(logEntry.getProperty()).isEqualTo("filterId");
    assertThat(logEntry.getOrgValue()).isNull();
    assertThat(logEntry.getNewValue()).isEqualTo(filter.getId());
  }

  @Test
  public void testDeleteFilter() {
    // given
    Filter filter = filterService.newTaskFilter()
        .setName("name")
        .setOwner("owner")
        .setQuery(taskService.createTaskQuery())
        .setProperties(new HashMap<String, Object>());
    filterService.saveFilter(filter);

    // when
    identityService.setAuthenticatedUserId("userId");
    filterService.deleteFilter(filter.getId());
    identityService.clearAuthentication();

    // then
    assertEquals(1L, historyService.createUserOperationLogQuery().count());
    UserOperationLogEntry logEntry = historyService.createUserOperationLogQuery().singleResult();
    assertThat(logEntry.getEntityType()).isEqualTo(EntityTypes.FILTER);
    assertThat(logEntry.getOperationType()).isEqualTo(UserOperationLogEntry.OPERATION_TYPE_DELETE);
    assertThat(logEntry.getProperty()).isEqualTo("filterId");
    assertThat(logEntry.getOrgValue()).isNull();
    assertThat(logEntry.getNewValue()).isEqualTo(filter.getId());
  }

}
