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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProvider;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderCaseInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderHistoricDecisionInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderProcessInstanceContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Daniel Meyer
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class TenantIdProviderTest extends ResourceProcessEngineTestCase {

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";
  protected static final String DECISION_DEFINITION_KEY = "decision";
  protected static final String CASE_DEFINITION_KEY = "caseTaskCase";

  protected static final String DMN_FILE = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";

  protected static final String CMMN_FILE = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTask.cmmn";
  protected static final String CMMN_VARIABLE_FILE = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTaskVariables.cmmn";
  protected static final String CMMN_SUBPROCESS_FILE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  protected static final String TENANT_ID = "tenant1";

  public TenantIdProviderTest() {
    super("org/camunda/bpm/engine/test/api/multitenancy/TenantIdProviderTest.camunda.cfg.xml");
  }

  @Override
  protected void tearDown() throws Exception {
    TestTenantIdProvider.reset();
    super.tearDown();
  }

  // root process instance //////////////////////////////////

  public void testProviderCalledForProcessDefinitionWithoutTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment without tenant id
    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done());

    // if a process instance is started
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // then the tenant id provider is invoked
    assertThat(tenantIdProvider.parameters.size(), is(1));
  }

  public void testProviderNotCalledForProcessDefinitionWithTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    deploymentForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done());

    // if a process instance is started
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.parameters.size(), is(0));
  }

  public void testProviderCalledWithVariables() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done());

    // if a process instance is started
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, Variables.createVariables().putValue("varName", true));

    // then the tenant id provider is passed in the variable
    assertThat(tenantIdProvider.parameters.size(), is(1));
    assertThat((Boolean) tenantIdProvider.parameters.get(0).getVariables().get("varName"), is(true));
  }

  public void testProviderCalledWithProcessDefinition() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done());
    ProcessDefinition deployedProcessDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // if a process instance is started
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // then the tenant id provider is passed in the process definition
    ProcessDefinition passedProcessDefinition = tenantIdProvider.parameters.get(0).getProcessDefinition();
    assertThat(passedProcessDefinition, is(notNullValue()));
    assertThat(passedProcessDefinition.getId(), is(deployedProcessDefinition.getId()));
  }

  public void testSetsTenantId() {

    String tenantId = TENANT_ID;
    SetValueTenantIdProvider tenantIdProvider = new SetValueTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done());

    // if a process instance is started
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // then the tenant id provider can set the tenant id to a value
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance.getTenantId(), is(tenantId));
  }

  public void testSetNullTenantId() {

    String tenantId = null;
    SetValueTenantIdProvider tenantIdProvider = new SetValueTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done());

    // if a process instance is started
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // then the tenant id provider can set the tenant id to null
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance.getTenantId(), is(nullValue()));
  }

  // sub process instance //////////////////////////////////

  public void testProviderCalledForProcessDefinitionWithoutTenantId_SubProcessInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment without tenant id
    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());

    // if a process instance is started
    runtimeService.startProcessInstanceByKey("superProcess");

    // then the tenant id provider is invoked twice
    assertThat(tenantIdProvider.parameters.size(), is(2));
  }

  public void testProviderNotCalledForProcessDefinitionWithTenantId_SubProcessInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    deploymentForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());

    // if a process instance is started
    runtimeService.startProcessInstanceByKey("superProcess");

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.parameters.size(), is(0));
  }

  public void testProviderCalledWithVariables_SubProcessInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).camundaIn("varName", "varName").done());

    // if a process instance is started
    runtimeService.startProcessInstanceByKey("superProcess", Variables.createVariables().putValue("varName", true));

    // then the tenant id provider is passed in the variable
    assertThat(tenantIdProvider.parameters.get(1).getVariables().size(), is(1));
    assertThat((Boolean) tenantIdProvider.parameters.get(1).getVariables().get("varName"), is(true));
  }

  public void testProviderCalledWithProcessDefinition_SubProcessInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());
    ProcessDefinition deployedProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();

    // if a process instance is started
    runtimeService.startProcessInstanceByKey("superProcess");

    // then the tenant id provider is passed in the process definition
    ProcessDefinition passedProcessDefinition = tenantIdProvider.parameters.get(1).getProcessDefinition();
    assertThat(passedProcessDefinition, is(notNullValue()));
    assertThat(passedProcessDefinition.getId(), is(deployedProcessDefinition.getId()));
  }

  public void testProviderCalledWithSuperProcessInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());
    ProcessDefinition superProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("superProcess").singleResult();


    // if a process instance is started
    runtimeService.startProcessInstanceByKey("superProcess");

    // then the tenant id provider is passed in the process definition
    DelegateExecution superExecution = tenantIdProvider.parameters.get(1).getSuperExecution();
    assertThat(superExecution, is(notNullValue()));
    assertThat(superExecution.getProcessDefinitionId(), is(superProcessDefinition.getId()));
  }

  public void testSetsTenantId_SubProcessInstance() {

    String tenantId = TENANT_ID;
    SetValueOnSubProcessInstanceTenantIdProvider tenantIdProvider = new SetValueOnSubProcessInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());

    // if a process instance is started
    runtimeService.startProcessInstanceByKey("superProcess");

    // then the tenant id provider can set the tenant id to a value
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    assertThat(subProcessInstance.getTenantId(), is(tenantId));

    // and the super process instance is not assigned a tenant id
    ProcessInstance superProcessInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("superProcess").singleResult();
    assertThat(superProcessInstance.getTenantId(), is(nullValue()));
  }

  public void testSetNullTenantId_SubProcessInstance() {

    String tenantId = null;
    SetValueOnSubProcessInstanceTenantIdProvider tenantIdProvider = new SetValueOnSubProcessInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());

    // if a process instance is started
    runtimeService.startProcessInstanceByKey("superProcess");

    // then the tenant id provider can set the tenant id to null
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    assertThat(processInstance.getTenantId(), is(nullValue()));
  }

  public void testTenantIdInheritedFromSuperProcessInstance() {

    String tenantId = TENANT_ID;
    SetValueOnRootProcessInstanceTenantIdProvider tenantIdProvider = new SetValueOnRootProcessInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());

    // if a process instance is started
    runtimeService.startProcessInstanceByKey("superProcess");

    // then the tenant id is inherited to the sub process instance even tough it is not set by the provider
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    assertThat(processInstance.getTenantId(), is(tenantId));
  }

  // process task in case //////////////////////////////

  public void testProviderCalledForProcessDefinitionWithoutTenantId_ProcessTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTask.cmmn"),
        Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done());

    // if the case is started
    caseService.createCaseInstanceByKey("testCase");
    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId("PI_ProcessTask_1").singleResult();
    caseService.manuallyStartCaseExecution(caseExecution.getId());

    // then the tenant id provider is invoked once for the process instance
    assertThat(tenantIdProvider.parameters.size(), is(1));
  }

  public void testProviderNotCalledForProcessDefinitionWithTenantId_ProcessTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(repositoryService.createDeployment().tenantId(TENANT_ID)
        .addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTask.cmmn"),
        Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done());

    // if the case is started
    caseService.createCaseInstanceByKey("testCase");
    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId("PI_ProcessTask_1").singleResult();
    caseService.manuallyStartCaseExecution(caseExecution.getId());

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.parameters.size(), is(0));
  }

  public void testProviderCalledWithVariables_ProcessTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTask.cmmn"),
        Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done());

    // if the case is started
    caseService.createCaseInstanceByKey("testCase", Variables.createVariables().putValue("varName", true));
    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId("PI_ProcessTask_1").singleResult();
    caseService.manuallyStartCaseExecution(caseExecution.getId());

    // then the tenant id provider is passed in the variable
    assertThat(tenantIdProvider.parameters.size(), is(1));

    VariableMap variables = tenantIdProvider.parameters.get(0).getVariables();
    assertThat(variables.size(), is(1));
    assertThat((Boolean) variables.get("varName"), is(true));
  }

  public void testProviderCalledWithProcessDefinition_ProcessTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTask.cmmn"),
        Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done());

    // if the case is started
    caseService.createCaseInstanceByKey("testCase");
    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId("PI_ProcessTask_1").singleResult();
    caseService.manuallyStartCaseExecution(caseExecution.getId());

    // then the tenant id provider is passed in the process definition
    assertThat(tenantIdProvider.parameters.size(), is(1));
    assertThat(tenantIdProvider.parameters.get(0).getProcessDefinition(), is(notNullValue()));
  }

  public void testProviderCalledWithSuperCaseExecution() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTask.cmmn"),
        Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done());

    // if the case is started
    caseService.createCaseInstanceByKey("testCase");
    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId("PI_ProcessTask_1").singleResult();
    caseService.manuallyStartCaseExecution(caseExecution.getId());

    // then the tenant id provider is handed in the super case execution
    assertThat(tenantIdProvider.parameters.size(), is(1));
    assertThat(tenantIdProvider.parameters.get(0).getSuperCaseExecution(), is(notNullValue()));
  }

  // historic decision instance //////////////////////////////////

  public void testProviderCalledForDecisionDefinitionWithoutTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment without tenant id
    deployment(DMN_FILE);

    // if a decision definition is evaluated
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY).variables(createVariables()).evaluate();

    // then the tenant id provider is invoked
    assertThat(tenantIdProvider.dmnParameters.size(), is(1));
  }

  public void testProviderNotCalledForDecisionDefinitionWithTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    deploymentForTenant(TENANT_ID, DMN_FILE);

    // if a decision definition is evaluated
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY).variables(createVariables()).evaluate();

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.dmnParameters.size(), is(0));
  }

  public void testProviderCalledWithDecisionDefinition() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(DMN_FILE);
    DecisionDefinition deployedDecisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    // if a decision definition is evaluated
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY).variables(createVariables()).evaluate();

    // then the tenant id provider is passed in the decision definition
    DecisionDefinition passedDecisionDefinition = tenantIdProvider.dmnParameters.get(0).getDecisionDefinition();
    assertThat(passedDecisionDefinition, is(notNullValue()));
    assertThat(passedDecisionDefinition.getId(), is(deployedDecisionDefinition.getId()));
  }

  public void testSetsTenantIdForHistoricDecisionInstance() {

    String tenantId = TENANT_ID;
    SetValueTenantIdProvider tenantIdProvider = new SetValueTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(DMN_FILE);

    // if a decision definition is evaluated
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY).variables(createVariables()).evaluate();

    // then the tenant id provider can set the tenant id to a value
    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();
    assertThat(historicDecisionInstance.getTenantId(), is(tenantId));
  }

  public void testSetNullTenantIdForHistoricDecisionInstance() {

    String tenantId = null;
    SetValueTenantIdProvider tenantIdProvider = new SetValueTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(DMN_FILE);

    // if a decision definition is evaluated
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY).variables(createVariables()).evaluate();

    // then the tenant id provider can set the tenant id to null
    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();
    assertThat(historicDecisionInstance.getTenantId(), is(nullValue()));
  }

  public void testProviderCalledForHistoricDecisionDefinitionWithoutTenantId_BusinessRuleTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .businessRuleTask()
        .camundaDecisionRef(DECISION_DEFINITION_KEY)
      .endEvent()
      .done();

    // given a deployment without tenant id
    deploymentWithoutTenant(DMN_FILE, process);

    // if a decision definition is evaluated
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, createVariables());

    // then the tenant id provider is invoked
    assertThat(tenantIdProvider.dmnParameters.size(), is(1));
  }

  public void testProviderNotCalledForHistoricDecisionDefinitionWithTenantId_BusinessRuleTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef(DECISION_DEFINITION_KEY)
        .endEvent()
        .done();

    // given a deployment with a tenant id
    deploymentForTenant(TENANT_ID, DMN_FILE, process);

    // if a process instance is started
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, createVariables());

    // then the tenant id providers are not invoked
    assertThat(tenantIdProvider.dmnParameters.size(), is(0));
  }

  public void testProviderCalledWithExecution_BusinessRuleTasks() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef(DECISION_DEFINITION_KEY)
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentWithoutTenant(DMN_FILE, process);

    // if a process instance is started
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, createVariables());
    Execution execution = runtimeService.createExecutionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();

    // then the tenant id provider is invoked
    assertThat(tenantIdProvider.dmnParameters.size(), is(1));
    ExecutionEntity passedExecution = (ExecutionEntity) tenantIdProvider.dmnParameters.get(0).getExecution();
    assertThat(passedExecution, is(notNullValue()));
    assertThat(passedExecution.getParent().getId(), is(execution.getId()));
  }

  public void testSetsTenantIdForHistoricDecisionInstance_BusinessRuleTask() {

    String tenantId = TENANT_ID;
    SetValueOnHistoricDecisionInstanceTenantIdProvider tenantIdProvider = new SetValueOnHistoricDecisionInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef(DECISION_DEFINITION_KEY)
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentWithoutTenant(DMN_FILE, process);

    // if a process instance is started
    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).setVariables(createVariables()).execute();

    // then the tenant id provider can set the tenant id to a value
    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult();
    assertThat(historicDecisionInstance.getTenantId(), is(tenantId));
  }

  public void testSetNullTenantIdForHistoricDecisionInstance_BusinessRuleTask() {

    String tenantId = null;
    SetValueOnHistoricDecisionInstanceTenantIdProvider tenantIdProvider = new SetValueOnHistoricDecisionInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef(DECISION_DEFINITION_KEY)
        .camundaAsyncAfter()
        .endEvent()
        .done();

    deploymentWithoutTenant(DMN_FILE, process);

    // if a process instance is started
    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).setVariables(createVariables()).execute();

    // then the tenant id provider can set the tenant id to a value
    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult();
    assertThat(historicDecisionInstance.getTenantId(), is(nullValue()));
  }

  protected String deploymentWithoutTenant(String classpathResource, BpmnModelInstance modelInstance) {
    return deployment(repositoryService.createDeployment().addClasspathResource(classpathResource), modelInstance);
  }

  protected VariableMap createVariables() {
    return Variables.createVariables().putValue("status", "gold");
  }

  // root case instance //////////////////////////////////

  public void testProviderCalledForCaseDefinitionWithoutTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment without tenant id
    deployment(CMMN_FILE);

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider is invoked
    assertThat(tenantIdProvider.caseParameters.size(), is(1));
  }

  public void testProviderNotCalledForCaseInstanceWithTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    deploymentForTenant(TENANT_ID, CMMN_FILE);

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.caseParameters.size(), is(0));
  }

  public void testProviderCalledForCaseInstanceWithVariables() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(CMMN_FILE);

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).setVariables(Variables.createVariables().putValue("varName", true)).create();

    // then the tenant id provider is passed in the variable
    assertThat(tenantIdProvider.caseParameters.size(), is(1));
    assertThat((Boolean) tenantIdProvider.caseParameters.get(0).getVariables().get("varName"), is(true));
  }

  public void testProviderCalledWithCaseDefinition() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(CMMN_FILE);
    CaseDefinition deployedCaseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider is passed in the case definition
    CaseDefinition passedCaseDefinition = tenantIdProvider.caseParameters.get(0).getCaseDefinition();
    assertThat(passedCaseDefinition, is(notNullValue()));
    assertThat(passedCaseDefinition.getId(), is(deployedCaseDefinition.getId()));
  }

  public void testSetsTenantIdForCaseInstance() {

    String tenantId = TENANT_ID;
    SetValueTenantIdProvider tenantIdProvider = new SetValueTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(CMMN_FILE);

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider can set the tenant id to a value
    CaseInstance caseInstance = caseService.createCaseInstanceQuery().singleResult();
    assertThat(caseInstance.getTenantId(), is(tenantId));
  }

  public void testSetNullTenantIdForCaseInstance() {

    String tenantId = null;
    SetValueTenantIdProvider tenantIdProvider = new SetValueTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(CMMN_FILE);

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider can set the tenant id to null
    CaseInstance caseInstance = caseService.createCaseInstanceQuery().singleResult();
    assertThat(caseInstance.getTenantId(), is(nullValue()));
  }

  // sub case instance //////////////////////////////////

  public void testProviderCalledForCaseDefinitionWithoutTenantId_SubCaseInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment without tenant id
    deployment(CMMN_SUBPROCESS_FILE,CMMN_FILE);

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();
    startCaseTask();

    // then the tenant id provider is invoked twice
    assertThat(tenantIdProvider.caseParameters.size(), is(2));
  }

  public void testProviderNotCalledForCaseDefinitionWithTenantId_SubCaseInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    deploymentForTenant(TENANT_ID, CMMN_SUBPROCESS_FILE, CMMN_FILE);

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();
    startCaseTask();

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.caseParameters.size(), is(0));
  }

  public void testProviderCalledWithVariables_SubCaseInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(CMMN_SUBPROCESS_FILE, CMMN_VARIABLE_FILE);

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).setVariables(Variables.createVariables().putValue("varName", true)).create();
    startCaseTask();

    // then the tenant id provider is passed in the variable
    assertThat(tenantIdProvider.caseParameters.get(1).getVariables().size(), is(1));
    assertThat((Boolean) tenantIdProvider.caseParameters.get(1).getVariables().get("varName"), is(true));
  }

  public void testProviderCalledWithCaseDefinition_SubCaseInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(CMMN_SUBPROCESS_FILE, CMMN_FILE);
    CaseDefinition deployedCaseDefinition = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("oneTaskCase").singleResult();

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();
    startCaseTask();

    // then the tenant id provider is passed in the case definition
    CaseDefinition passedCaseDefinition = tenantIdProvider.caseParameters.get(1).getCaseDefinition();
    assertThat(passedCaseDefinition, is(notNullValue()));
    assertThat(passedCaseDefinition.getId(), is(deployedCaseDefinition.getId()));
  }

  public void testProviderCalledWithSuperCaseInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(CMMN_SUBPROCESS_FILE, CMMN_FILE);
    CaseDefinition superCaseDefinition = repositoryService.createCaseDefinitionQuery().caseDefinitionKey(CASE_DEFINITION_KEY).singleResult();


    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();
    startCaseTask();

    // then the tenant id provider is passed in the case definition
    DelegateCaseExecution superCaseExecution = tenantIdProvider.caseParameters.get(1).getSuperCaseExecution();
    assertThat(superCaseExecution, is(notNullValue()));
    assertThat(superCaseExecution.getCaseDefinitionId(), is(superCaseDefinition.getId()));
  }

  public void testSetsTenantId_SubCaseInstance() {

    String tenantId = TENANT_ID;
    SetValueOnSubCaseInstanceTenantIdProvider tenantIdProvider = new SetValueOnSubCaseInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(CMMN_SUBPROCESS_FILE, CMMN_FILE);

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();
    startCaseTask();

    // then the tenant id provider can set the tenant id to a value
    CaseInstance subCaseInstance = caseService.createCaseInstanceQuery().caseDefinitionKey("oneTaskCase").singleResult();
    assertThat(subCaseInstance.getTenantId(), is(tenantId));

    // and the super case instance is not assigned a tenant id
    CaseInstance superCaseInstance = caseService.createCaseInstanceQuery().caseDefinitionKey(CASE_DEFINITION_KEY).singleResult();
    assertThat(superCaseInstance.getTenantId(), is(nullValue()));
  }

  public void testSetNullTenantId_SubCaseInstance() {

    String tenantId = null;
    SetValueOnSubCaseInstanceTenantIdProvider tenantIdProvider = new SetValueOnSubCaseInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(CMMN_SUBPROCESS_FILE, CMMN_FILE);

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();
    startCaseTask();

    // then the tenant id provider can set the tenant id to null
    CaseInstance caseInstance = caseService.createCaseInstanceQuery().caseDefinitionKey("oneTaskCase").singleResult();
    assertThat(caseInstance.getTenantId(), is(nullValue()));
  }

  public void testTenantIdInheritedFromSuperCaseInstance() {

    String tenantId = TENANT_ID;
    SetValueOnRootCaseInstanceTenantIdProvider tenantIdProvider = new SetValueOnRootCaseInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(CMMN_SUBPROCESS_FILE, CMMN_FILE);

    // if a case instance is created
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();
    startCaseTask();

    // then the tenant id is inherited to the sub case instance even tough it is not set by the provider
    CaseInstance caseInstance = caseService.createCaseInstanceQuery().caseDefinitionKey("oneTaskCase").singleResult();
    assertThat(caseInstance.getTenantId(), is(tenantId));
  }

  public void testProviderCalledForCaseInstanceWithSuperCaseExecution() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    deployment(CMMN_SUBPROCESS_FILE, CMMN_FILE);

    // if the case is started
    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();
    startCaseTask();

    // then the tenant id provider is handed in the super case execution
    assertThat(tenantIdProvider.caseParameters.size(), is(2));
    assertThat(tenantIdProvider.caseParameters.get(1).getSuperCaseExecution(), is(notNullValue()));
  }

  protected void startCaseTask() {
    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId("PI_CaseTask_1").singleResult();
    caseService.withCaseExecution(caseExecution.getId()).manualStart();
  }

  // helpers //////////////////////////////////////////

  public static class TestTenantIdProvider implements TenantIdProvider {

    protected static TenantIdProvider delegate;

    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      if(delegate != null) {
        return delegate.provideTenantIdForProcessInstance(ctx);
      }
      else {
        return null;
      }
    }

    public static void reset() {
      delegate = null;
    }

    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
      if (delegate != null) {
        return delegate.provideTenantIdForHistoricDecisionInstance(ctx);
      } else {
        return null;
      }
    }

    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
      if (delegate != null) {
        return delegate.provideTenantIdForCaseInstance(ctx);
      } else {
        return null;
      }
    }
  }

  public static class ContextLoggingTenantIdProvider implements TenantIdProvider {

    protected List<TenantIdProviderProcessInstanceContext> parameters = new ArrayList<TenantIdProviderProcessInstanceContext>();
    protected List<TenantIdProviderHistoricDecisionInstanceContext> dmnParameters = new ArrayList<TenantIdProviderHistoricDecisionInstanceContext>();
    protected List<TenantIdProviderCaseInstanceContext> caseParameters = new ArrayList<TenantIdProviderCaseInstanceContext>();

    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      parameters.add(ctx);
      return null;
    }

    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
      dmnParameters.add(ctx);
      return null;
    }

    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
      caseParameters.add(ctx);
      return null;
    }

  }

  // sets constant tenant ids on process instances, case instances and historic decision instances
  public static class SetValueTenantIdProvider implements TenantIdProvider {

    private final String tenantIdToSet;

    public SetValueTenantIdProvider(String tenantIdToSet) {
      this.tenantIdToSet = tenantIdToSet;
    }

    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      return tenantIdToSet;
    }

    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
      return tenantIdToSet;
    }

    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
      return tenantIdToSet;
    }

  }

  //only sets tenant ids on sub process instances
  public static class SetValueOnSubProcessInstanceTenantIdProvider implements TenantIdProvider {

    private final String tenantIdToSet;

    public SetValueOnSubProcessInstanceTenantIdProvider(String tenantIdToSet) {
      this.tenantIdToSet = tenantIdToSet;
    }

    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      return ctx.getSuperExecution() != null ? tenantIdToSet : null;
    }

    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
      return null;
    }

    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
      return null;
    }

  }

  // only sets tenant ids on root process instances
  public static class SetValueOnRootProcessInstanceTenantIdProvider implements TenantIdProvider {

    private final String tenantIdToSet;

    public SetValueOnRootProcessInstanceTenantIdProvider(String tenantIdToSet) {
      this.tenantIdToSet = tenantIdToSet;
    }

    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      return ctx.getSuperExecution() == null ? tenantIdToSet : null;
    }

    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
      return null;
    }

    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
      return null;
    }
  }

  //only sets tenant ids on historic decision instances when an execution exists
  public static class SetValueOnHistoricDecisionInstanceTenantIdProvider implements TenantIdProvider {

    private final String tenantIdToSet;

    public SetValueOnHistoricDecisionInstanceTenantIdProvider(String tenantIdToSet) {
      this.tenantIdToSet = tenantIdToSet;
    }

    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      return null;
    }

    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
      return ctx.getExecution() != null ? tenantIdToSet : null;
    }

    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
      return null;
    }
  }

  //only sets tenant ids on sub case instances
  public static class SetValueOnSubCaseInstanceTenantIdProvider implements TenantIdProvider {

    private final String tenantIdToSet;

    public SetValueOnSubCaseInstanceTenantIdProvider(String tenantIdToSet) {
      this.tenantIdToSet = tenantIdToSet;
    }

    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      return null;
    }

    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
      return null;
    }

    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
      return ctx.getSuperCaseExecution() != null ? tenantIdToSet : null;
    }
  }

  // only sets tenant ids on root case instances
  public static class SetValueOnRootCaseInstanceTenantIdProvider implements TenantIdProvider {

    private final String tenantIdToSet;

    public SetValueOnRootCaseInstanceTenantIdProvider(String tenantIdToSet) {
      this.tenantIdToSet = tenantIdToSet;
    }

    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      return null;
    }

    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
      return null;
    }

    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
      return ctx.getSuperCaseExecution() == null ? tenantIdToSet : null;
    }
  }

}
