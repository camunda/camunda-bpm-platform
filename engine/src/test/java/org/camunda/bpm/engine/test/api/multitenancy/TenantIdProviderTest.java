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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProvider;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderCaseInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderHistoricDecisionInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderProcessInstanceContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Daniel Meyer
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class TenantIdProviderTest {

  protected static final String CONFIGURATION_RESOURCE = "org/camunda/bpm/engine/test/api/multitenancy/TenantIdProviderTest.camunda.cfg.xml";

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";
  protected static final String DECISION_DEFINITION_KEY = "decision";
  protected static final String CASE_DEFINITION_KEY = "caseTaskCase";

  protected static final String DMN_FILE = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";

  protected static final String CMMN_FILE = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTask.cmmn";
  protected static final String CMMN_FILE_WITH_MANUAL_ACTIVATION = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTaskWithManualActivation.cmmn";
  protected static final String CMMN_VARIABLE_FILE = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTaskVariables.cmmn";
  protected static final String CMMN_SUBPROCESS_FILE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  protected static final String TENANT_ID = "tenant1";

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(CONFIGURATION_RESOURCE);
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);


  @After
  public void tearDown() {
    TestTenantIdProvider.reset();
  }

  // root process instance //////////////////////////////////

  @Test
  public void providerCalledForProcessDefinitionWithoutTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment without tenant id
    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done());

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // then the tenant id provider is invoked
    assertThat(tenantIdProvider.parameters.size(), is(1));
  }

  @Test
  public void providerNotCalledForProcessDefinitionWithTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done());

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.parameters.size(), is(0));
  }



  @Test
  public void providerCalledForStartedProcessInstanceByStartFormWithoutTenantId() {
    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment without a tenant id
    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
                                            "org/camunda/bpm/engine/test/api/form/util/request.form");

    // when a process instance is started with a start form
    String processDefinitionId = engineRule.getRepositoryService()
                                           .createProcessDefinitionQuery()
                                           .singleResult()
                                           .getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("employeeName", "demo");

    ProcessInstance procInstance = engineRule.getFormService().submitStartForm(processDefinitionId, properties);
    assertNotNull(procInstance);

    // then the tenant id provider is invoked
    assertThat(tenantIdProvider.parameters.size(), is(1));
  }


  @Test
  public void providerNotCalledForStartedProcessInstanceByStartFormWithTenantId() {
    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
                                            "org/camunda/bpm/engine/test/api/form/util/request.form");

    // when a process instance is started with a start form
    String processDefinitionId = engineRule.getRepositoryService()
                                           .createProcessDefinitionQuery()
                                           .singleResult()
                                           .getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("employeeName", "demo");

    ProcessInstance procInstance = engineRule.getFormService().submitStartForm(processDefinitionId, properties);
    assertNotNull(procInstance);

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.parameters.size(), is(0));
  }

  @Test
  public void providerCalledForStartedProcessInstanceByModificationWithoutTenantId() {
    // given a deployment without a tenant id
    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;
    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
                                                .startEvent().userTask("task")
                                                .endEvent().done(),
                                            "org/camunda/bpm/engine/test/api/form/util/request.form");

    // when a process instance is created and the instance is set to a starting point
    String processInstanceId = engineRule.getRuntimeService()
                                         .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
                                         .startBeforeActivity("task").execute().getProcessInstanceId();

    //then provider is called
    assertNotNull(engineRule.getRuntimeService().getActivityInstance(processInstanceId));
    assertThat(tenantIdProvider.parameters.size(), is(1));
  }

  @Test
  public void providerNotCalledForStartedProcessInstanceByModificationWithTenantId() {
    // given a deployment with a tenant id
    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;
    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
                                                                   .startEvent().userTask("task")
                                                                   .endEvent().done(),
                                            "org/camunda/bpm/engine/test/api/form/util/request.form");

    // when a process instance is created and the instance is set to a starting point
    String processInstanceId = engineRule.getRuntimeService()
                                         .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
                                         .startBeforeActivity("task").execute().getProcessInstanceId();

    //then provider should not be called
    assertNotNull(engineRule.getRuntimeService().getActivityInstance(processInstanceId));
    assertThat(tenantIdProvider.parameters.size(), is(0));
  }

  @Test
  public void providerCalledWithVariables() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done());

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY, Variables.createVariables().putValue("varName", true));

    // then the tenant id provider is passed in the variable
    assertThat(tenantIdProvider.parameters.size(), is(1));
    assertThat((Boolean) tenantIdProvider.parameters.get(0).getVariables().get("varName"), is(true));
  }

  @Test
  public void providerCalledWithProcessDefinition() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done());
    ProcessDefinition deployedProcessDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery().singleResult();

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // then the tenant id provider is passed in the process definition
    ProcessDefinition passedProcessDefinition = tenantIdProvider.parameters.get(0).getProcessDefinition();
    assertThat(passedProcessDefinition, is(notNullValue()));
    assertThat(passedProcessDefinition.getId(), is(deployedProcessDefinition.getId()));
  }

  @Test
  public void setsTenantId() {

    String tenantId = TENANT_ID;
    StaticTenantIdTestProvider tenantIdProvider = new StaticTenantIdTestProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done());

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // then the tenant id provider can set the tenant id to a value
    ProcessInstance processInstance = engineRule.getRuntimeService().createProcessInstanceQuery().singleResult();
    assertThat(processInstance.getTenantId(), is(tenantId));
  }

  @Test
  public void setNullTenantId() {

    String tenantId = null;
    StaticTenantIdTestProvider tenantIdProvider = new StaticTenantIdTestProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done());

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // then the tenant id provider can set the tenant id to null
    ProcessInstance processInstance = engineRule.getRuntimeService().createProcessInstanceQuery().singleResult();
    assertThat(processInstance.getTenantId(), is(nullValue()));
  }

  // sub process instance //////////////////////////////////

  @Test
  public void providerCalledForProcessDefinitionWithoutTenantId_SubProcessInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment without tenant id
    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey("superProcess");

    // then the tenant id provider is invoked twice
    assertThat(tenantIdProvider.parameters.size(), is(2));
  }

  @Test
  public void providerNotCalledForProcessDefinitionWithTenantId_SubProcessInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey("superProcess");

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.parameters.size(), is(0));
  }

  @Test
  public void providerCalledWithVariables_SubProcessInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).camundaIn("varName", "varName").done());

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey("superProcess", Variables.createVariables().putValue("varName", true));

    // then the tenant id provider is passed in the variable
    assertThat(tenantIdProvider.parameters.get(1).getVariables().size(), is(1));
    assertThat((Boolean) tenantIdProvider.parameters.get(1).getVariables().get("varName"), is(true));
  }

  @Test
  public void providerCalledWithProcessDefinition_SubProcessInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());
    ProcessDefinition deployedProcessDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey("superProcess");

    // then the tenant id provider is passed in the process definition
    ProcessDefinition passedProcessDefinition = tenantIdProvider.parameters.get(1).getProcessDefinition();
    assertThat(passedProcessDefinition, is(notNullValue()));
    assertThat(passedProcessDefinition.getId(), is(deployedProcessDefinition.getId()));
  }

  @Test
  public void providerCalledWithSuperProcessInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());
    ProcessDefinition superProcessDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("superProcess").singleResult();


    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey("superProcess");

    // then the tenant id provider is passed in the process definition
    DelegateExecution superExecution = tenantIdProvider.parameters.get(1).getSuperExecution();
    assertThat(superExecution, is(notNullValue()));
    assertThat(superExecution.getProcessDefinitionId(), is(superProcessDefinition.getId()));
  }

  @Test
  public void setsTenantId_SubProcessInstance() {

    String tenantId = TENANT_ID;
    SetValueOnSubProcessInstanceTenantIdProvider tenantIdProvider = new SetValueOnSubProcessInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey("superProcess");

    // then the tenant id provider can set the tenant id to a value
    ProcessInstance subProcessInstance = engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    assertThat(subProcessInstance.getTenantId(), is(tenantId));

    // and the super process instance is not assigned a tenant id
    ProcessInstance superProcessInstance = engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionKey("superProcess").singleResult();
    assertThat(superProcessInstance.getTenantId(), is(nullValue()));
  }

  @Test
  public void setNullTenantId_SubProcessInstance() {

    String tenantId = null;
    SetValueOnSubProcessInstanceTenantIdProvider tenantIdProvider = new SetValueOnSubProcessInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey("superProcess");

    // then the tenant id provider can set the tenant id to null
    ProcessInstance processInstance = engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    assertThat(processInstance.getTenantId(), is(nullValue()));
  }

  @Test
  public void tenantIdInheritedFromSuperProcessInstance() {

    String tenantId = TENANT_ID;
    SetValueOnRootProcessInstanceTenantIdProvider tenantIdProvider = new SetValueOnRootProcessInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done(),
        Bpmn.createExecutableProcess("superProcess").startEvent().callActivity().calledElement(PROCESS_DEFINITION_KEY).done());

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey("superProcess");

    // then the tenant id is inherited to the sub process instance even tough it is not set by the provider
    ProcessInstance processInstance = engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    assertThat(processInstance.getTenantId(), is(tenantId));
  }

  // process task in case //////////////////////////////

  @Test
  public void providerCalledForProcessDefinitionWithoutTenantId_ProcessTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(
        Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done(),
        "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTask.cmmn");

    // if the case is started
    engineRule.getCaseService().createCaseInstanceByKey("testCase");
    CaseExecution caseExecution = engineRule.getCaseService().createCaseExecutionQuery().activityId("PI_ProcessTask_1").singleResult();


    // then the tenant id provider is invoked once for the process instance
    assertThat(tenantIdProvider.parameters.size(), is(1));
  }

  @Test
  public void providerNotCalledForProcessDefinitionWithTenantId_ProcessTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deployForTenant(TENANT_ID,
        Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done(),
        "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTask.cmmn");

    // if the case is started
    engineRule.getCaseService().createCaseInstanceByKey("testCase");
    CaseExecution caseExecution = engineRule.getCaseService().createCaseExecutionQuery().activityId("PI_ProcessTask_1").singleResult();

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.parameters.size(), is(0));
  }

  @Test
  public void providerCalledWithVariables_ProcessTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(
        Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done(),
        "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTask.cmmn");

    // if the case is started
    engineRule.getCaseService().createCaseInstanceByKey("testCase", Variables.createVariables().putValue("varName", true));
    CaseExecution caseExecution = engineRule.getCaseService().createCaseExecutionQuery().activityId("PI_ProcessTask_1").singleResult();

    // then the tenant id provider is passed in the variable
    assertThat(tenantIdProvider.parameters.size(), is(1));

    VariableMap variables = tenantIdProvider.parameters.get(0).getVariables();
    assertThat(variables.size(), is(1));
    assertThat((Boolean) variables.get("varName"), is(true));
  }

  @Test
  public void providerCalledWithProcessDefinition_ProcessTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(
        Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done(),
        "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTask.cmmn");

    // if the case is started
    engineRule.getCaseService().createCaseInstanceByKey("testCase");
    CaseExecution caseExecution = engineRule.getCaseService().createCaseExecutionQuery().activityId("PI_ProcessTask_1").singleResult();

    // then the tenant id provider is passed in the process definition
    assertThat(tenantIdProvider.parameters.size(), is(1));
    assertThat(tenantIdProvider.parameters.get(0).getProcessDefinition(), is(notNullValue()));
  }

  @Test
  public void providerCalledWithSuperCaseExecution() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(
        Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().done(),
        "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTask.cmmn");

    // if the case is started
    engineRule.getCaseService().createCaseInstanceByKey("testCase");
    CaseExecution caseExecution = engineRule.getCaseService().createCaseExecutionQuery().activityId("PI_ProcessTask_1").singleResult();

    // then the tenant id provider is handed in the super case execution
    assertThat(tenantIdProvider.parameters.size(), is(1));
    assertThat(tenantIdProvider.parameters.get(0).getSuperCaseExecution(), is(notNullValue()));
  }

  // historic decision instance //////////////////////////////////

  @Test
  public void providerCalledForDecisionDefinitionWithoutTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment without tenant id
    testRule.deploy(DMN_FILE);

    // if a decision definition is evaluated
    engineRule.getDecisionService().evaluateDecisionTableByKey(DECISION_DEFINITION_KEY).variables(createVariables()).evaluate();

    // then the tenant id provider is invoked
    assertThat(tenantIdProvider.dmnParameters.size(), is(1));
  }

  @Test
  public void providerNotCalledForDecisionDefinitionWithTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    testRule.deployForTenant(TENANT_ID, DMN_FILE);

    // if a decision definition is evaluated
    engineRule.getDecisionService().evaluateDecisionTableByKey(DECISION_DEFINITION_KEY).variables(createVariables()).evaluate();

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.dmnParameters.size(), is(0));
  }

  @Test
  public void providerCalledWithDecisionDefinition() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(DMN_FILE);
    DecisionDefinition deployedDecisionDefinition = engineRule.getRepositoryService().createDecisionDefinitionQuery().singleResult();

    // if a decision definition is evaluated
    engineRule.getDecisionService().evaluateDecisionTableByKey(DECISION_DEFINITION_KEY).variables(createVariables()).evaluate();

    // then the tenant id provider is passed in the decision definition
    DecisionDefinition passedDecisionDefinition = tenantIdProvider.dmnParameters.get(0).getDecisionDefinition();
    assertThat(passedDecisionDefinition, is(notNullValue()));
    assertThat(passedDecisionDefinition.getId(), is(deployedDecisionDefinition.getId()));
  }

  @Test
  public void setsTenantIdForHistoricDecisionInstance() {

    String tenantId = TENANT_ID;
    StaticTenantIdTestProvider tenantIdProvider = new StaticTenantIdTestProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(DMN_FILE);

    // if a decision definition is evaluated
    engineRule.getDecisionService().evaluateDecisionTableByKey(DECISION_DEFINITION_KEY).variables(createVariables()).evaluate();

    // then the tenant id provider can set the tenant id to a value
    HistoricDecisionInstance historicDecisionInstance = engineRule.getHistoryService().createHistoricDecisionInstanceQuery().singleResult();
    assertThat(historicDecisionInstance.getTenantId(), is(tenantId));
  }

  @Test
  public void setNullTenantIdForHistoricDecisionInstance() {

    String tenantId = null;
    StaticTenantIdTestProvider tenantIdProvider = new StaticTenantIdTestProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(DMN_FILE);

    // if a decision definition is evaluated
    engineRule.getDecisionService().evaluateDecisionTableByKey(DECISION_DEFINITION_KEY).variables(createVariables()).evaluate();

    // then the tenant id provider can set the tenant id to null
    HistoricDecisionInstance historicDecisionInstance = engineRule.getHistoryService().createHistoricDecisionInstanceQuery().singleResult();
    assertThat(historicDecisionInstance.getTenantId(), is(nullValue()));
  }

  @Test
  public void providerCalledForHistoricDecisionDefinitionWithoutTenantId_BusinessRuleTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .businessRuleTask()
        .camundaDecisionRef(DECISION_DEFINITION_KEY)
      .endEvent()
      .done();

    // given a deployment without tenant id
    testRule.deploy(process, DMN_FILE);

    // if a decision definition is evaluated
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY, createVariables());

    // then the tenant id provider is invoked
    assertThat(tenantIdProvider.dmnParameters.size(), is(1));
  }

  @Test
  public void providerNotCalledForHistoricDecisionDefinitionWithTenantId_BusinessRuleTask() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    testRule.deployForTenant(TENANT_ID,
        Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
          .startEvent()
          .businessRuleTask()
            .camundaDecisionRef(DECISION_DEFINITION_KEY)
          .endEvent()
        .done(),
        DMN_FILE);

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY, createVariables());

    // then the tenant id providers are not invoked
    assertThat(tenantIdProvider.dmnParameters.size(), is(0));
  }

  @Test
  public void providerCalledWithExecution_BusinessRuleTasks() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .businessRuleTask()
          .camundaDecisionRef(DECISION_DEFINITION_KEY)
        .camundaAsyncAfter()
        .endEvent()
        .done();

    testRule.deploy(process, DMN_FILE);

    // if a process instance is started
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY, createVariables());
    Execution execution = engineRule.getRuntimeService().createExecutionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();

    // then the tenant id provider is invoked
    assertThat(tenantIdProvider.dmnParameters.size(), is(1));
    ExecutionEntity passedExecution = (ExecutionEntity) tenantIdProvider.dmnParameters.get(0).getExecution();
    assertThat(passedExecution, is(notNullValue()));
    assertThat(passedExecution.getParent().getId(), is(execution.getId()));
  }

  @Test
  public void setsTenantIdForHistoricDecisionInstance_BusinessRuleTask() {

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

    testRule.deploy(process, DMN_FILE);

    // if a process instance is started
    engineRule.getRuntimeService().createProcessInstanceByKey(PROCESS_DEFINITION_KEY).setVariables(createVariables()).execute();

    // then the tenant id provider can set the tenant id to a value
    HistoricDecisionInstance historicDecisionInstance = engineRule.getHistoryService().createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult();
    assertThat(historicDecisionInstance.getTenantId(), is(tenantId));
  }

  @Test
  public void setNullTenantIdForHistoricDecisionInstance_BusinessRuleTask() {

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

    testRule.deploy(process, DMN_FILE);

    // if a process instance is started
    engineRule.getRuntimeService().createProcessInstanceByKey(PROCESS_DEFINITION_KEY).setVariables(createVariables()).execute();

    // then the tenant id provider can set the tenant id to a value
    HistoricDecisionInstance historicDecisionInstance = engineRule.getHistoryService().createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult();
    assertThat(historicDecisionInstance.getTenantId(), is(nullValue()));
  }

  protected VariableMap createVariables() {
    return Variables.createVariables().putValue("status", "gold");
  }

  // root case instance //////////////////////////////////

  @Test
  public void providerCalledForCaseDefinitionWithoutTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment without tenant id
    testRule.deploy(CMMN_FILE_WITH_MANUAL_ACTIVATION);

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider is invoked
    assertThat(tenantIdProvider.caseParameters.size(), is(1));
  }

  @Test
  public void providerNotCalledForCaseInstanceWithTenantId() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    testRule.deployForTenant(TENANT_ID, CMMN_FILE_WITH_MANUAL_ACTIVATION);

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.caseParameters.size(), is(0));
  }

  @Test
  public void providerCalledForCaseInstanceWithVariables() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(CMMN_FILE_WITH_MANUAL_ACTIVATION);

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).setVariables(Variables.createVariables().putValue("varName", true)).create();

    // then the tenant id provider is passed in the variable
    assertThat(tenantIdProvider.caseParameters.size(), is(1));
    assertThat((Boolean) tenantIdProvider.caseParameters.get(0).getVariables().get("varName"), is(true));
  }

  @Test
  public void providerCalledWithCaseDefinition() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(CMMN_FILE_WITH_MANUAL_ACTIVATION);
    CaseDefinition deployedCaseDefinition = engineRule.getRepositoryService().createCaseDefinitionQuery().singleResult();

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider is passed in the case definition
    CaseDefinition passedCaseDefinition = tenantIdProvider.caseParameters.get(0).getCaseDefinition();
    assertThat(passedCaseDefinition, is(notNullValue()));
    assertThat(passedCaseDefinition.getId(), is(deployedCaseDefinition.getId()));
  }

  @Test
  public void setsTenantIdForCaseInstance() {

    String tenantId = TENANT_ID;
    StaticTenantIdTestProvider tenantIdProvider = new StaticTenantIdTestProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(CMMN_FILE_WITH_MANUAL_ACTIVATION);

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider can set the tenant id to a value
    CaseInstance caseInstance = engineRule.getCaseService().createCaseInstanceQuery().singleResult();
    assertThat(caseInstance.getTenantId(), is(tenantId));
  }

  @Test
  public void setNullTenantIdForCaseInstance() {

    String tenantId = null;
    StaticTenantIdTestProvider tenantIdProvider = new StaticTenantIdTestProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(CMMN_FILE_WITH_MANUAL_ACTIVATION);

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider can set the tenant id to null
    CaseInstance caseInstance = engineRule.getCaseService().createCaseInstanceQuery().singleResult();
    assertThat(caseInstance.getTenantId(), is(nullValue()));
  }

  // sub case instance //////////////////////////////////

  @Test
  public void providerCalledForCaseDefinitionWithoutTenantId_SubCaseInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment without tenant id
    testRule.deploy(CMMN_SUBPROCESS_FILE,CMMN_FILE);

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider is invoked twice
    assertThat(tenantIdProvider.caseParameters.size(), is(2));
  }

  @Test
  public void providerNotCalledForCaseDefinitionWithTenantId_SubCaseInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    // given a deployment with a tenant id
    testRule.deployForTenant(TENANT_ID, CMMN_SUBPROCESS_FILE, CMMN_FILE);

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider is not invoked
    assertThat(tenantIdProvider.caseParameters.size(), is(0));
  }

  @Test
  public void providerCalledWithVariables_SubCaseInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(CMMN_SUBPROCESS_FILE, CMMN_VARIABLE_FILE);

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).setVariables(Variables.createVariables().putValue("varName", true)).create();

    // then the tenant id provider is passed in the variable
    assertThat(tenantIdProvider.caseParameters.get(1).getVariables().size(), is(1));
    assertThat((Boolean) tenantIdProvider.caseParameters.get(1).getVariables().get("varName"), is(true));
  }

  @Test
  public void providerCalledWithCaseDefinition_SubCaseInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(CMMN_SUBPROCESS_FILE, CMMN_FILE);
    CaseDefinition deployedCaseDefinition = engineRule.getRepositoryService().createCaseDefinitionQuery().caseDefinitionKey("oneTaskCase").singleResult();

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider is passed in the case definition
    CaseDefinition passedCaseDefinition = tenantIdProvider.caseParameters.get(1).getCaseDefinition();
    assertThat(passedCaseDefinition, is(notNullValue()));
    assertThat(passedCaseDefinition.getId(), is(deployedCaseDefinition.getId()));
  }

  @Test
  public void providerCalledWithSuperCaseInstance() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(CMMN_SUBPROCESS_FILE, CMMN_FILE_WITH_MANUAL_ACTIVATION);
    CaseDefinition superCaseDefinition = engineRule.getRepositoryService().createCaseDefinitionQuery().caseDefinitionKey(CASE_DEFINITION_KEY).singleResult();


    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();
    startCaseTask();

    // then the tenant id provider is passed in the case definition
    DelegateCaseExecution superCaseExecution = tenantIdProvider.caseParameters.get(1).getSuperCaseExecution();
    assertThat(superCaseExecution, is(notNullValue()));
    assertThat(superCaseExecution.getCaseDefinitionId(), is(superCaseDefinition.getId()));
  }

  @Test
  public void setsTenantId_SubCaseInstance() {

    String tenantId = TENANT_ID;
    SetValueOnSubCaseInstanceTenantIdProvider tenantIdProvider = new SetValueOnSubCaseInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(CMMN_SUBPROCESS_FILE, CMMN_FILE);

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider can set the tenant id to a value
    CaseInstance subCaseInstance = engineRule.getCaseService().createCaseInstanceQuery().caseDefinitionKey("oneTaskCase").singleResult();
    assertThat(subCaseInstance.getTenantId(), is(tenantId));

    // and the super case instance is not assigned a tenant id
    CaseInstance superCaseInstance = engineRule.getCaseService().createCaseInstanceQuery().caseDefinitionKey(CASE_DEFINITION_KEY).singleResult();
    assertThat(superCaseInstance.getTenantId(), is(nullValue()));
  }

  @Test
  public void setNullTenantId_SubCaseInstance() {

    String tenantId = null;
    SetValueOnSubCaseInstanceTenantIdProvider tenantIdProvider = new SetValueOnSubCaseInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(CMMN_SUBPROCESS_FILE, CMMN_FILE);

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider can set the tenant id to null
    CaseInstance caseInstance = engineRule.getCaseService().createCaseInstanceQuery().caseDefinitionKey("oneTaskCase").singleResult();
    assertThat(caseInstance.getTenantId(), is(nullValue()));
  }

  @Test
  public void tenantIdInheritedFromSuperCaseInstance() {

    String tenantId = TENANT_ID;
    SetValueOnRootCaseInstanceTenantIdProvider tenantIdProvider = new SetValueOnRootCaseInstanceTenantIdProvider(tenantId);
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(CMMN_SUBPROCESS_FILE, CMMN_FILE);

    // if a case instance is created
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id is inherited to the sub case instance even tough it is not set by the provider
    CaseInstance caseInstance = engineRule.getCaseService().createCaseInstanceQuery().caseDefinitionKey("oneTaskCase").singleResult();
    assertThat(caseInstance.getTenantId(), is(tenantId));
  }

  @Test
  public void providerCalledForCaseInstanceWithSuperCaseExecution() {

    ContextLoggingTenantIdProvider tenantIdProvider = new ContextLoggingTenantIdProvider();
    TestTenantIdProvider.delegate = tenantIdProvider;

    testRule.deploy(CMMN_SUBPROCESS_FILE, CMMN_FILE);

    // if the case is started
    engineRule.getCaseService().withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    // then the tenant id provider is handed in the super case execution
    assertThat(tenantIdProvider.caseParameters.size(), is(2));
    assertThat(tenantIdProvider.caseParameters.get(1).getSuperCaseExecution(), is(notNullValue()));
  }

  protected void startCaseTask() {
    CaseExecution caseExecution = engineRule.getCaseService().createCaseExecutionQuery().activityId("PI_CaseTask_1").singleResult();
    engineRule.getCaseService().withCaseExecution(caseExecution.getId()).manualStart();
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
