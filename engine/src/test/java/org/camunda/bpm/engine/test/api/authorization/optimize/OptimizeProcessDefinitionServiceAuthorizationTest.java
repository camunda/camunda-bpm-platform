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
package org.camunda.bpm.engine.test.api.authorization.optimize;


import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.optimize.OptimizeHistoricIdentityLinkLogEntity;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Date;
import java.util.List;

import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class OptimizeProcessDefinitionServiceAuthorizationTest extends AuthorizationTest {

  protected String deploymentId;
  private OptimizeService optimizeService;

  @Override
  public void setUp() throws Exception {

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    optimizeService = config.getOptimizeService();

    DeploymentBuilder deploymentbuilder = repositoryService.createDeployment();
    BpmnModelInstance defaultModel = Bpmn.createExecutableProcess("process")
      .startEvent()
      .endEvent()
      .done();
    deploymentId = deployment(deploymentbuilder, defaultModel);

    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  public void testGetCompletedActivitiesWithoutAuthorization() {
    // given
    startProcessInstanceByKey("process");

    try {
      // when
      optimizeService.getCompletedHistoricActivityInstances(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the activities");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }

  }

  public void testGetCompletedActivitiesWithAuthorization() {
    // given
    startProcessInstanceByKey("process");
    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, READ_HISTORY);

    // when
    List<HistoricActivityInstance> completedHistoricActivityInstances =
      optimizeService.getCompletedHistoricActivityInstances(new Date(0L), null, 10);

    // then
    assertThat(completedHistoricActivityInstances.size(), is(2));
  }

  public void testGetRunningActivitiesWithoutAuthorization() {
    // given
    startProcessInstanceByKey("process");

    try {
      // when
      optimizeService.getRunningHistoricActivityInstances(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the activities");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }

  }

  public void testGetRunningActivitiesWithAuthorization() {
    // given
    startProcessInstanceByKey("process");
    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, READ_HISTORY);

    // when
    List<HistoricActivityInstance> runningHistoricActivityInstances =
      optimizeService.getRunningHistoricActivityInstances(new Date(0L), null, 10);

    // then
    assertThat(runningHistoricActivityInstances.size(), is(0));
  }

  public void testGetCompletedTasksWithoutAuthorization() {
    // given
    startProcessInstanceByKey("process");

    try {
      // when
      optimizeService.getCompletedHistoricTaskInstances(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the tasks");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  public void testGetCompletedTasksWithAuthorization() {
    // given
    startProcessInstanceByKey("process");
    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, READ_HISTORY);

    // when
    List<HistoricTaskInstance> completedHistoricTaskInstances =
      optimizeService.getCompletedHistoricTaskInstances(new Date(0L), null, 10);

    // then
    assertThat(completedHistoricTaskInstances.size(), is(0));
  }

  public void testGetRunningTasksWithoutAuthorization() {
    // given
    startProcessInstanceByKey("process");

    try {
      // when
      optimizeService.getRunningHistoricTaskInstances(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the tasks");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  public void testGetRunningTasksWithAuthorization() {
    // given
    startProcessInstanceByKey("process");
    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, READ_HISTORY);

    // when
    List<HistoricTaskInstance> runningHistoricTaskInstances =
      optimizeService.getRunningHistoricTaskInstances(new Date(0L), null, 10);

    // then
    assertThat(runningHistoricTaskInstances.size(), is(0));
  }

  public void testGetOperationsLogWithoutAuthorization() {
    // given
    startProcessInstanceByKey("process");

    try {
      // when
      optimizeService.getHistoricUserOperationLogs(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the logs");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  public void testGetOperationsLogWithAuthorization() {
    // given
    startProcessInstanceByKey("process");
    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, READ_HISTORY);

    // when
    List<UserOperationLogEntry> operationLogEntries =
      optimizeService.getHistoricUserOperationLogs(new Date(0L), null, 10);

    // then
    assertThat(operationLogEntries.size(), is(0));
  }

  public void testGetHistoricIdentityLinkLogWithoutAuthorization() {
    // given
    startProcessInstanceByKey("process");

    try {
      // when
      optimizeService.getHistoricIdentityLinkLogs(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the logs");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  public void testGetHistoricIdentityLinkLogWithAuthorization() {
    // given
    startProcessInstanceByKey("process");
    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, READ_HISTORY);

    // when
    List<OptimizeHistoricIdentityLinkLogEntity> historicIdentityLinkLogs =
      optimizeService.getHistoricIdentityLinkLogs(new Date(0L), null, 10);

    // then
    assertThat(historicIdentityLinkLogs.size(), is(0));
  }

  public void testGetCompletedProcessInstancesWithoutAuthorization() {
    // given
    startProcessInstanceByKey("process");

    try {
      // when
      optimizeService.getCompletedHistoricProcessInstances(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the activities");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  public void testGetCompletedProcessInstancesWithAuthorization() {
    // given
    startProcessInstanceByKey("process");
    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, READ_HISTORY);

    // when
    List<HistoricProcessInstance> completedHistoricProcessInstances =
      optimizeService.getCompletedHistoricProcessInstances(new Date(0L), null, 10);

    // then
    assertThat(completedHistoricProcessInstances.size(), is(1));
  }

  public void testGetRunningProcessInstancesWithoutAuthorization() {
    // given
    startProcessInstanceByKey("process");

    try {
      // when
      optimizeService.getRunningHistoricProcessInstances(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the activities");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  public void testGetRunningProcessInstancesWithAuthorization() {
    // given
    startProcessInstanceByKey("process");
    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, READ_HISTORY);

    // when
    List<HistoricProcessInstance> runningHistoricProcessInstances =
      optimizeService.getRunningHistoricProcessInstances(new Date(0L), null, 10);

    // then
    assertThat(runningHistoricProcessInstances.size(), is(0));
  }

  public void testGetVariableUpdatesWithoutAuthorization() {
    // given
    startProcessInstanceByKey("process");

    try {
      // when
      optimizeService.getHistoricVariableUpdates(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the activities");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  public void testGetVariableUpdatesWithAuthorization() {
    // given
    startProcessInstanceByKey("process");
    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, READ_HISTORY);

    // when
    List<HistoricVariableUpdate> historicVariableUpdates =
      optimizeService.getHistoricVariableUpdates(new Date(0L), null, 10);

    // then
    assertThat(historicVariableUpdates.size(), is(0));
  }

  public void testAuthorizationsOnSingleProcessDefinitionIsNotEnough() {
    // given
    startProcessInstanceByKey("process");
    createGrantAuthorization(PROCESS_DEFINITION, "process", userId, READ_HISTORY);

    try {
      // when
      optimizeService.getCompletedHistoricActivityInstances(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the activities");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  public void testGrantAuthorizationWithAllPermissions() {
    // given
    startProcessInstanceByKey("process");
    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, ALL);
    createGrantAuthorization(PROCESS_INSTANCE, "*", userId, ALL);

    // when
    List<HistoricActivityInstance> completedHistoricActivityInstances =
      optimizeService.getCompletedHistoricActivityInstances(new Date(0L), null, 10);

    // then
    assertThat(completedHistoricActivityInstances.size(), is(2));
  }

}
