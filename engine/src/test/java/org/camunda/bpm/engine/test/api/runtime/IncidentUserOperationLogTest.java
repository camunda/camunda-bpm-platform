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
package org.camunda.bpm.engine.test.api.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class IncidentUserOperationLogTest {
  
  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected IdentityService identityService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    identityService = engineRule.getIdentityService();
  }
  
  @Test
  public void shouldLogIncidentCreation() {
    // given
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");
    assertEquals(0, historyService.createUserOperationLogQuery().count());

    // when
    identityService.setAuthenticatedUserId("userId");
    Incident incident = runtimeService.createIncident("foo", processInstance.getId(), "aa", "bar");
    identityService.clearAuthentication();

    // then
    assertEquals(2, historyService.createUserOperationLogQuery().count());
    
    UserOperationLogEntry entry = historyService.createUserOperationLogQuery().property("incidentType").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE_INCIDENT, entry.getOperationType());
    assertEquals(EntityTypes.PROCESS_INSTANCE, entry.getEntityType());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, entry.getCategory());
    assertNull(entry.getOrgValue());
    assertEquals("foo", entry.getNewValue());
    assertNull(entry.getExecutionId());
    assertEquals(processInstance.getId(), entry.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), entry.getProcessDefinitionId());
    assertEquals("Process", entry.getProcessDefinitionKey());
    
    entry = historyService.createUserOperationLogQuery().property("configuration").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE_INCIDENT, entry.getOperationType());
    assertEquals(EntityTypes.PROCESS_INSTANCE, entry.getEntityType());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, entry.getCategory());
    assertNull(entry.getOrgValue());
    assertEquals(incident.getConfiguration(), entry.getNewValue());
    assertNull(entry.getExecutionId());
    assertEquals(processInstance.getId(), entry.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), entry.getProcessDefinitionId());
    assertEquals("Process", entry.getProcessDefinitionKey());
  }

  @Test
  public void shouldNotLogIncidentCreationFailure() {
    // given
    assertEquals(0, historyService.createUserOperationLogQuery().count());
    
    // when
    thrown.expect(BadUserRequestException.class);
    runtimeService.createIncident("foo", null, "userTask1", "bar");
    
    // then
    assertEquals(0, historyService.createUserOperationLogQuery().count());
  }

  @Test
  public void shouldLogIncidentResolution() {
    // given
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");
    Incident incident = runtimeService.createIncident("foo", processInstance.getId(), "userTask1", "bar");
    assertEquals(0, historyService.createUserOperationLogQuery().count());

    // when
    identityService.setAuthenticatedUserId("userId");
    runtimeService.resolveIncident(incident.getId());
    identityService.clearAuthentication();

    // then
    assertEquals(1, historyService.createUserOperationLogQuery().count());
    UserOperationLogEntry entry = historyService.createUserOperationLogQuery().singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_RESOLVE, entry.getOperationType());
    assertEquals(EntityTypes.PROCESS_INSTANCE, entry.getEntityType());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, entry.getCategory());
    assertEquals("incidentId", entry.getProperty());
    assertNull(entry.getOrgValue());
    assertEquals(incident.getId(), entry.getNewValue());
    assertNull(entry.getExecutionId());
    assertEquals(processInstance.getId(), entry.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), entry.getProcessDefinitionId());
    assertEquals("Process", entry.getProcessDefinitionKey());
  }

  @Test
  public void shouldNotLogIncidentResolutionFailure() {
    // given
    assertEquals(0, historyService.createUserOperationLogQuery().count());
    
    // when
    thrown.expect(NotFoundException.class);
    runtimeService.resolveIncident("foo");
    
    // then
    assertEquals(0, historyService.createUserOperationLogQuery().count());
  }
}
