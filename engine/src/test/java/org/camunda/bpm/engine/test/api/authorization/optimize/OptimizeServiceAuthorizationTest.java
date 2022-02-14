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


import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.TENANT;
import static org.camunda.bpm.engine.authorization.Resources.USER;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestBaseRule;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ResetDmnConfigUtil;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class OptimizeServiceAuthorizationTest {

  public static final String TEST_DECISION = "testDecision";
  public static final String SIMPLE_PROCESS = "process";
  public static final String USER_TASK_PROCESS = "userTaskProcess";
  private static final String FAILING_PROCESS = "oneFailingServiceTaskProcess";
  private OptimizeService optimizeService;

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";
  protected String userId = "test";

  public static final String DECISION_INPUT_EQUALS_OUTPUT =
    "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected AuthorizationTestBaseRule authRule = new AuthorizationTestBaseRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule).around(authRule);

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getCompletedHistoricActivityInstances(new Date(0L), null, 10)},
      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getRunningHistoricActivityInstances(new Date(0L), null, 10)},

      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getCompletedHistoricProcessInstances(new Date(0L), null, 10)},
      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getRunningHistoricProcessInstances(new Date(0L), null, 10)},

      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getCompletedHistoricTaskInstances(new Date(0L), null, 10)},
      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getRunningHistoricTaskInstances(new Date(0L), null, 10)},

      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getHistoricIdentityLinkLogs(new Date(0L), null, 10)},

      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getHistoricUserOperationLogs(new Date(0L), null, 10)},

      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getHistoricVariableUpdates(new Date(0L), null, false, 10)},

      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getHistoricDecisionInstances(new Date(0L), null, 10)},

      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getCompletedHistoricIncidents(new Date(0L), null, 10)},
      {(Function<OptimizeService, List<?>>) optimizeService ->
        optimizeService.getOpenHistoricIncidents(new Date(0L), null, 10)},
    });
  }

  @Parameterized.Parameter
  public Function<OptimizeService, List<?>> methodToTest;

  protected IdentityService identityService;
  protected RepositoryService repositoryService;
  protected AuthorizationService authorizationService;
  protected RuntimeService runtimeService;
  protected DecisionService decisionService;
  protected TaskService taskService;
  protected ManagementService managementService;

  @Before
  public void setUp() throws Exception {

    identityService = engineRule.getIdentityService();
    repositoryService = engineRule.getRepositoryService();
    authorizationService = engineRule.getAuthorizationService();
    runtimeService = engineRule.getRuntimeService();
    decisionService = engineRule.getDecisionService();
    taskService = engineRule.getTaskService();
    managementService = engineRule.getManagementService();
    ProcessEngineConfigurationImpl config = engineRule.getProcessEngineConfiguration();
    optimizeService = config.getOptimizeService();

    DefaultDmnEngineConfiguration dmnEngineConfiguration =
      engineRule.getProcessEngineConfiguration().getDmnEngineConfiguration();
    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
      .enableFeelLegacyBehavior(true)
      .init();

    authRule.createUserAndGroup(userId, "testGroup");
    authRule.createGrantAuthorization(AUTHORIZATION, ANY, userId, ALL);
    authRule.createGrantAuthorization(USER, ANY, userId, ALL);

    deployTestData();
    authRule.enableAuthorization(userId);
  }

  @After
  public void tearDown() {
    DefaultDmnEngineConfiguration dmnEngineConfiguration =
      engineRule.getProcessEngineConfiguration().getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
      .enableFeelLegacyBehavior(false)
      .init();

    authRule.disableAuthorization();
    authRule.deleteUsersAndGroups();
    identityService.clearAuthentication();
  }

  @Test
  public void cantGetDataWithoutTenantAuthorization() {
    // given
    identityService.setAuthentication(userId, null, Collections.singletonList(TENANT_ONE));
    authRule.createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    authRule.createGrantAuthorization(DECISION_DEFINITION, ANY, userId, READ_HISTORY);

    try {
      // when
      methodToTest.apply(optimizeService);
      fail("Exception expected: It should not be possible to retrieve the data");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      testRule.assertTextPresent(userId, exceptionMessage);
      testRule.assertTextPresent(READ.getName(), exceptionMessage);
      testRule.assertTextPresent(TENANT.resourceName(), exceptionMessage);
    }
  }

  @Test
  public void cantGetDataWithoutProcessDefinitionAuthorization() {
    // given
    identityService.setAuthentication(userId, null, Collections.singletonList(TENANT_ONE));
    authRule.createGrantAuthorization(DECISION_DEFINITION, ANY, userId, READ_HISTORY);
    authRule.createGrantAuthorization(TENANT, ANY, userId, READ);

    try {
      // when
      methodToTest.apply(optimizeService);
      fail("Exception expected: It should not be possible to retrieve the data");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      testRule.assertTextPresent(userId, exceptionMessage);
      testRule.assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  @Test
  public void authorizationOnSingleProcessResourceNotEnough() {
    // given
    identityService.setAuthentication(userId, null, Collections.singletonList(TENANT_ONE));
    authRule.createGrantAuthorization(PROCESS_DEFINITION, SIMPLE_PROCESS, userId, READ_HISTORY);
    authRule.createGrantAuthorization(DECISION_DEFINITION, ANY, userId, READ_HISTORY);
    authRule.createGrantAuthorization(TENANT, ANY, userId, READ);

    try {
      // when
      methodToTest.apply(optimizeService);
      fail("Exception expected: It should not be possible to retrieve the data");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      testRule.assertTextPresent(userId, exceptionMessage);
      testRule.assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  @Test
  public void cantGetDataWithoutDecisionDefinitionAuthorization() {
    // given
    identityService.setAuthentication(userId, null, Collections.singletonList(TENANT_ONE));
    authRule.createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    authRule.createGrantAuthorization(TENANT, ANY, userId, READ);

    try {
      // when
      methodToTest.apply(optimizeService);
      fail("Exception expected: It should not be possible to retrieve the data");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      testRule.assertTextPresent(userId, exceptionMessage);
      testRule.assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      testRule.assertTextPresent(DECISION_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  @Test
  public void authorizationOnSingleDecisionResourceNotEnough() {
    // given
    identityService.setAuthentication(userId, null, Collections.singletonList(TENANT_ONE));
    authRule.createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    authRule.createGrantAuthorization(DECISION_DEFINITION, TEST_DECISION, userId, READ_HISTORY);
    authRule.createGrantAuthorization(TENANT, ANY, userId, READ);

    try {
      // when
      methodToTest.apply(optimizeService);
      fail("Exception expected: It should not be possible to retrieve the data");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      testRule.assertTextPresent(userId, exceptionMessage);
      testRule.assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      testRule.assertTextPresent(DECISION_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  @Test
  public void canGetDataWithAllAuthorizations() {
    // given
    identityService.setAuthentication(userId, null, Collections.singletonList(TENANT_ONE));
    generateTestData();

    authRule.createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    authRule.createGrantAuthorization(DECISION_DEFINITION, ANY, userId, READ_HISTORY);
    authRule.createGrantAuthorization(TENANT, ANY, userId, READ);

    // when
    List<?> instance = methodToTest.apply(optimizeService);

    // then
    assertThat(instance.size()).isGreaterThan(0);
  }

  private void generateTestData() {
    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(false);

    // completed activity/task/process instance data
    final ProcessDefinition process = selectProcessDefinitionByKey("process");
    runtimeService.startProcessInstanceById(
      process.getId(),
      // variable update data
      Variables.createVariables()
        .putValue("foo", "bar")
    );
    // running activity/task/process instance data
    final ProcessDefinition process2 = selectProcessDefinitionByKey("userTaskProcess");
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(process2.getId());
    // op log data
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    runtimeService.activateProcessInstanceById(processInstance.getId());
    // identity link log data
    completeAllUserTasks();
    // decision instance data
    final DecisionDefinition decision = selectDecisionDefinitionByKey();
    decisionService.evaluateDecisionById(decision.getId())
      .variables(Variables.createVariables().putValue("input1", "a")).evaluate();

    // create completed incident data
    runtimeService.startProcessInstanceByKey(FAILING_PROCESS);
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 0); // creates incident
    managementService.setJobRetries(jobId, 1); // resolves incident
    managementService.setJobRetries(jobId, 0); // creates second incident

    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(true);
  }

  private void completeAllUserTasks() {
      List<Task> list = taskService.createTaskQuery().list();
      for (Task task : list) {
        taskService.claim(task.getId(), userId);
        taskService.complete(task.getId());
      }
  }

  protected ProcessDefinition selectProcessDefinitionByKey(final String processDefinitionKey) {
    return  repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
  }

  protected DecisionDefinition selectDecisionDefinitionByKey() {
    return repositoryService
      .createDecisionDefinitionQuery()
      .decisionDefinitionKey(TEST_DECISION)
      .singleResult();
  }

  private void deployTestData() {
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().tenantId(TENANT_ONE);
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess(SIMPLE_PROCESS)
      .startEvent()
      .endEvent()
      .done();
    deploymentBuilder.addModelInstance("testProcess1-" + ".bpmn", bpmnModelInstance);
    bpmnModelInstance = Bpmn.createExecutableProcess(USER_TASK_PROCESS)
      .startEvent()
      .userTask()
      .userTask()
      .endEvent()
      .done();
    deploymentBuilder.addModelInstance("userTaskProcess1-" + ".bpmn", bpmnModelInstance);
    bpmnModelInstance = Bpmn.createExecutableProcess(FAILING_PROCESS)
      .startEvent("start")
      .serviceTask("task")
        .camundaAsyncBefore()
        .camundaClass(FailingDelegate.class.getName())
      .endEvent("end")
      .done();
    deploymentBuilder.addModelInstance("failingProcess1-" + ".bpmn", bpmnModelInstance);
    deploymentBuilder.addClasspathResource(DECISION_INPUT_EQUALS_OUTPUT);
    testRule.deploy(deploymentBuilder);

    deploymentBuilder = repositoryService.createDeployment().tenantId(TENANT_TWO);
    deploymentBuilder.addModelInstance("testProcess2-" + ".bpmn", bpmnModelInstance);
    deploymentBuilder.addClasspathResource(DECISION_INPUT_EQUALS_OUTPUT);
    testRule.deploy(deploymentBuilder);
  }


  /**
   * This class is just there to support code that's below java 8. Once
   * only Java 8 is supported by every engine version that's maintained we
   * can remove this interface.
   */
  private interface Function<T, T1> {
    T1 apply(final T t);
  }
}
