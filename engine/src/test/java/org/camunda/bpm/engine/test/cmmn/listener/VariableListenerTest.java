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
package org.camunda.bpm.engine.test.cmmn.listener;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.CaseVariableListener;
import org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance;
import org.camunda.bpm.engine.delegate.VariableListener;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.context.CaseExecutionContext;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class VariableListenerTest extends PluggableProcessEngineTestCase {

  protected Map<Object, Object> beans = null;

  protected void setUp() throws Exception {
    super.setUp();

    LogVariableListener.reset();
    beans = processEngineConfiguration.getBeans();
  }

  @Deployment
  public void testAnyEventListenerByClass() {
    CaseInstance caseInstance = caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i set a variable on a higher scope
    caseService.withCaseExecution(caseInstance.getId()).setVariable("anInstanceVariable", "anInstanceValue").execute();

    // then the listener is not invoked
    assertTrue(LogVariableListener.getInvocations().isEmpty());

    // when i set a variable on the human task (ie the source execution matters although the variable ends up in the same place)
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("aTaskVariable", "aTaskValue").execute();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.CREATE)
      .name("aTaskVariable")
      .value("aTaskValue")
      .matches(LogVariableListener.getInvocations().get(0));

    LogVariableListener.reset();

    // when i update the variable on the human task
    caseService.withCaseExecution(taskExecution.getId()).setVariable("aTaskVariable", "aNewTaskValue").execute();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());
    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.UPDATE)
      .name("aTaskVariable")
      .value("aNewTaskValue")
      .activityInstanceId(taskExecution.getId())
      .matches(LogVariableListener.getInvocations().get(0));
    LogVariableListener.reset();

    // when i remove the variable from the human task
    caseService.withCaseExecution(taskExecution.getId()).removeVariable("aTaskVariable").execute();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());
    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.DELETE)
      .name("aTaskVariable")
      .value(null)
      .activityInstanceId(taskExecution.getId())
      .matches(LogVariableListener.getInvocations().get(0));

    LogVariableListener.reset();
  }

  @Deployment
  public void testCreateEventListenerByClass() {
    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i create a variable on the human task
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("aTaskVariable", "aTaskValue").execute();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.CREATE)
      .name("aTaskVariable")
      .value("aTaskValue")
      .matches(LogVariableListener.getInvocations().get(0));

    LogVariableListener.reset();

    // when i update the variable on the human task
    caseService.withCaseExecution(taskExecution.getId()).setVariable("aTaskVariable", "aNewTaskValue").execute();

    // then the listener is not invoked
    assertTrue(LogVariableListener.getInvocations().isEmpty());

    // when i remove the variable from the human task
    caseService.withCaseExecution(taskExecution.getId()).removeVariable("aTaskVariable").execute();

    // then the listener is not invoked
    assertTrue(LogVariableListener.getInvocations().isEmpty());
  }

  @Deployment
  public void testUpdateEventListenerByClass() {
    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i create a variable on the human task
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("aTaskVariable", "aTaskValue").execute();

    // then the listener is not invoked
    assertTrue(LogVariableListener.getInvocations().isEmpty());

    // when i update the variable on the human task
    caseService.withCaseExecution(taskExecution.getId()).setVariable("aTaskVariable", "aNewTaskValue").execute();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.UPDATE)
      .name("aTaskVariable")
      .value("aNewTaskValue")
      .activityInstanceId(taskExecution.getId())
      .matches(LogVariableListener.getInvocations().get(0));

    LogVariableListener.reset();


    // when i remove the variable from the human task
    caseService.withCaseExecution(taskExecution.getId()).removeVariable("aTaskVariable").execute();

    // then the listener is not invoked
    assertTrue(LogVariableListener.getInvocations().isEmpty());
  }


  @Deployment
  public void testVariableListenerInvokedFromSourceScope() {
    CaseInstance caseInstance = caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i create a variable on the case instance
    caseService.withCaseExecution(caseInstance.getId()).setVariable("aTaskVariable", "aTaskValue").execute();

    // then the listener is not invoked
    assertEquals(0, LogVariableListener.getInvocations().size());

    // when i update the variable from the task execution
    caseService.withCaseExecution(taskExecution.getId()).setVariable("aTaskVariable", "aTaskValue").execute();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    DelegateVariableInstanceSpec
      .fromCaseExecution(caseInstance)
      .sourceExecution(taskExecution)
      .event(VariableListener.UPDATE)
      .name("aTaskVariable")
      .value("aTaskValue")
      .activityInstanceId(caseInstance.getId())
      .matches(LogVariableListener.getInvocations().get(0));

    LogVariableListener.reset();
  }

  @Deployment
  public void testDeleteEventListenerByClass() {
    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i create a variable on the human task
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("aTaskVariable", "aTaskValue").execute();

    // then the listener is not invoked
    assertTrue(LogVariableListener.getInvocations().isEmpty());

    // when i update the variable on the human task
    caseService.withCaseExecution(taskExecution.getId()).setVariable("aTaskVariable", "aNewTaskValue").execute();

    // then the listener is not invoked
    assertTrue(LogVariableListener.getInvocations().isEmpty());

    // when i remove the variable from the human task
    caseService.withCaseExecution(taskExecution.getId()).removeVariable("aTaskVariable").execute();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.DELETE)
      .name("aTaskVariable")
      .value(null)
      .activityInstanceId(taskExecution.getId())
      .matches(LogVariableListener.getInvocations().get(0));

    LogVariableListener.reset();
  }

  @Deployment
  public void testVariableListenerByDelegateExpression() {
    beans.put("listener", new LogVariableListener());

    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i create a variable on the human task
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("aTaskVariable", "aTaskValue").execute();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.CREATE)
      .name("aTaskVariable")
      .value("aTaskValue")
      .matches(LogVariableListener.getInvocations().get(0));

    LogVariableListener.reset();
  }

  @Deployment
  public void testVariableListenerByExpression() {
    SimpleBean simpleBean = new SimpleBean();
    beans.put("bean", simpleBean);

    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i create a variable on the human task
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("aTaskVariable", "aTaskValue").execute();

    // then the listener is invoked
    assertTrue(simpleBean.wasInvoked());
  }

  @Deployment
  public void testVariableListenerByScript() {
    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i create a variable on the human task
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("aTaskVariable", "aTaskValue").execute();

    // then the listener is invoked
    assertTrue(SimpleBean.wasStaticallyInvoked());

    SimpleBean.reset();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/listener/VariableListenerTest.testListenerOnParentScope.cmmn")
  public void testListenerSourceExecution() {
    CaseInstance caseInstance = caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i set a variable on a deeper scope execution but actually on the parent
    caseService.withCaseExecution(taskExecution.getId()).setVariable("aTaskVariable", "aTaskValue").execute();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    // and the source execution is the execution the variable was set on
    DelegateVariableInstanceSpec
      .fromCaseExecution(caseInstance)
      .sourceExecution(taskExecution)
      .event(VariableListener.CREATE)
      .name("aTaskVariable")
      .value("aTaskValue")
      .matches(LogVariableListener.getInvocations().get(0));

    LogVariableListener.reset();
  }

  @Deployment
  public void testListenerOnParentScope() {
    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i set a variable on a deeper scope
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("aTaskVariable", "aTaskValue").execute();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.CREATE)
      .name("aTaskVariable")
      .value("aTaskValue")
      .matches(LogVariableListener.getInvocations().get(0));

    LogVariableListener.reset();
  }

  @Deployment
  public void testChildListenersNotInvoked() {
    CaseInstance caseInstance = caseService
      .withCaseDefinitionByKey("case")
      .create();

    // when i set a variable on the parent scope
    caseService.withCaseExecution(caseInstance.getId()).setVariableLocal("aTaskVariable", "aTaskValue").execute();

    // then the listener is not invoked
    assertEquals(0, LogVariableListener.getInvocations().size());

    LogVariableListener.reset();
  }

  @Deployment
  public void testListenerOnAncestorScope() {
    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution stageExecution =
        caseService.createCaseExecutionQuery().activityId("PI_Stage_1").singleResult();
    assertNotNull(stageExecution);

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i set a variable on a deeper scope
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("aTaskVariable", "aTaskValue").execute();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.CREATE)
      .name("aTaskVariable")
      .value("aTaskValue")
      .matches(LogVariableListener.getInvocations().get(0));

    LogVariableListener.reset();
  }

  @Deployment
  public void testInvalidListenerClassName() {
    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    try {
      caseService
        .withCaseExecution(taskExecution.getId())
        .setVariableLocal("aTaskVariable", "aTaskValue")
        .execute();

      fail("expected exception during variable listener invocation");
    } catch (ProcessEngineException e) {
      // happy path
    }
  }

  @Deployment
  public void testListenerDoesNotImplementInterface() {
    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    try {
      caseService
        .withCaseExecution(taskExecution.getId())
        .setVariableLocal("aTaskVariable", "aTaskValue")
        .execute();

      fail("expected exception during variable listener invocation");
    } catch (ProcessEngineException e) {
      // happy path
    }
  }

  @Deployment
  public void testDelegateInstanceIsProcessEngineAware() {
    CaseInstance caseInstance = caseService
      .withCaseDefinitionByKey("case")
      .create();
    assertFalse(ProcessEngineAwareListener.hasFoundValidRuntimeService());

    // when i set a variable that causes the listener to be notified
    caseService.withCaseExecution(caseInstance.getId()).setVariableLocal("aTaskVariable", "aTaskValue").execute();

    // then the listener is invoked and has found process engine services
    assertTrue(ProcessEngineAwareListener.hasFoundValidRuntimeService());

    ProcessEngineAwareListener.reset();
  }

  /**
   * TODO: add when history for case execution variables is implemented
   */
  @Deployment
  public void FAILING_testListenerDoesNotInterfereWithHistory() {
    CaseInstance caseInstance = caseService
      .withCaseDefinitionByKey("case")
      .create();

    // when i set a variable that causes the listener to be notified
    // and that listener sets the same variable to another value (here "value2")
    caseService.withCaseExecution(caseInstance.getId()).setVariableLocal("variable", "value1").execute();

    // then there should be two historic variable updates for both values
    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_FULL.getId()) {
      List<HistoricDetail> variableUpdates = historyService.createHistoricDetailQuery().variableUpdates().list();

      assertEquals(2, variableUpdates.size());

      for (HistoricDetail detail : variableUpdates) {
        HistoricVariableUpdate update = (HistoricVariableUpdate) detail;
        boolean update1Processed = false;
        boolean update2Processed = false;

        if (!update1Processed && update.getValue().equals("value1")) {
          update1Processed = true;
        } else if (!update2Processed && update.getValue().equals("value2")) {
          update2Processed = true;
        } else {
          fail("unexpected variable update");
        }
      }
    }
  }

  @Deployment
  public void testListenerInvocationFinishesBeforeSubsequentInvocations() {
    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i set a variable and the listener itself sets another variable
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("variable", "value1").execute();

    // then all listeners for the first variable update are invoked first
    // and then the listeners for the second update are invoked
    List<DelegateCaseVariableInstance> invocations = LogAndUpdateVariableListener.getInvocations();
    assertEquals(6, invocations.size());

    // the first invocations should regard the first value
    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.CREATE)
      .name("variable")
      .value("value1")
      .matches(LogAndUpdateVariableListener.getInvocations().get(0));

    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.CREATE)
      .name("variable")
      .value("value1")
      .matches(LogAndUpdateVariableListener.getInvocations().get(1));

    // the second invocations should regard the updated value
    // there are four invocations since both listeners have set "value2" and both were again executed, i.e. 2*2 = 4

    for (int i = 2; i < 6; i++) {
      DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.UPDATE)
      .name("variable")
      .value("value2")
      .matches(LogAndUpdateVariableListener.getInvocations().get(i));
    }

    LogAndUpdateVariableListener.reset();
  }

  @Deployment
  public void testTwoListenersOnSameScope() {
    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i set a variable
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("testVariable", "value1").execute();

    // then both listeners are invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.CREATE)
      .name("testVariable")
      .value("value1")
      .matches(LogVariableListener.getInvocations().get(0));

    assertEquals(1, LogAndUpdateVariableListener.getInvocations().size());

    DelegateVariableInstanceSpec
      .fromCaseExecution(taskExecution)
      .event(VariableListener.CREATE)
      .name("testVariable")
      .value("value1")
      .matches(LogAndUpdateVariableListener.getInvocations().get(0));

    LogVariableListener.reset();
    LogAndUpdateVariableListener.reset();

  }

  @Deployment
  public void testVariableListenerByClassWithFieldExpressions() {
    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i set a variable
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("testVariable", "value1").execute();

    // then the field expressions are resolved
    assertEquals(1, LogInjectedValuesListener.getResolvedStringValueExpressions().size());
    assertEquals("injectedValue", LogInjectedValuesListener.getResolvedStringValueExpressions().get(0));

    assertEquals(1, LogInjectedValuesListener.getResolvedJuelExpressions().size());
    assertEquals("cam", LogInjectedValuesListener.getResolvedJuelExpressions().get(0));

    LogInjectedValuesListener.reset();
  }

  @Deployment
  public void testVariableListenerByDelegateExpressionWithFieldExpressions() {
    beans.put("listener", new LogInjectedValuesListener());

    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i set a variable
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("testVariable", "value1").execute();

    // then the field expressions are resolved
    assertEquals(1, LogInjectedValuesListener.getResolvedStringValueExpressions().size());
    assertEquals("injectedValue", LogInjectedValuesListener.getResolvedStringValueExpressions().get(0));

    assertEquals(1, LogInjectedValuesListener.getResolvedJuelExpressions().size());
    assertEquals("cam", LogInjectedValuesListener.getResolvedJuelExpressions().get(0));

    LogInjectedValuesListener.reset();
  }

  @Deployment
  public void testVariableListenerExecutionContext() {
    CaseInstance caseInstance = caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i set a variable
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("testVariable", "value1").execute();

    // then the listener is invoked
    assertEquals(1, LogExecutionContextListener.getCaseExecutionContexts().size());
    CaseExecutionContext executionContext = LogExecutionContextListener.getCaseExecutionContexts().get(0);

    assertNotNull(executionContext);

    // although this is not inside a command, checking for IDs should be ok
    assertEquals(caseInstance.getId(), executionContext.getCaseInstance().getId());
    assertEquals(taskExecution.getId(), executionContext.getExecution().getId());

    LogExecutionContextListener.reset();
  }

  @Deployment
  public void testInvokeBuiltinListenersOnly() {
    // disable custom variable listener invocation
    processEngineConfiguration.setInvokeCustomVariableListeners(false);

    // add a builtin variable listener the hard way
    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();
    processEngineConfiguration
      .getDeploymentCache()
      .getCaseDefinitionById(caseDefinition.getId())
      .findActivity("PI_HumanTask_1")
      .addBuiltInVariableListener(CaseVariableListener.CREATE, new LogVariableListener());

    caseService
      .withCaseDefinitionByKey("case")
      .create();

    CaseExecution taskExecution =
        caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);

    // when i set a variable
    caseService.withCaseExecution(taskExecution.getId()).setVariableLocal("testVariable", "value1").execute();

    // then the builtin listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    // but the custom listener is not invoked
    assertEquals(0, LogExecutionContextListener.getCaseExecutionContexts().size());

    LogVariableListener.reset();
    LogExecutionContextListener.reset();

    // restore configuration
    processEngineConfiguration.setInvokeCustomVariableListeners(true);
  }

  public void testDefaultCustomListenerInvocationSetting() {
    assertTrue(processEngineConfiguration.isInvokeCustomVariableListeners());
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/cmmn/listener/VariableListenerTest.testVariableListenerWithProcessTask.cmmn",
      "org/camunda/bpm/engine/test/cmmn/listener/VariableListenerTest.testVariableListenerWithProcessTask.bpmn20.xml"
      })
  public void testVariableListenerWithProcessTask() {
    CaseInstance caseInstance = caseService.createCaseInstanceByKey("case");

    CaseExecution processTask = caseService
        .createCaseExecutionQuery()
        .activityId("PI_ProcessTask_1")
        .singleResult();

    String processTaskId = processTask.getId();

    caseService
      .withCaseExecution(processTaskId)
      .manualStart();

    // then the listener is invoked
    assertEquals(1, LogVariableListener.getInvocations().size());

    DelegateVariableInstanceSpec
      .fromCaseExecution(caseInstance)
      .sourceExecution(processTask)
      .event(VariableListener.CREATE)
      .name("aVariable")
      .value("aValue")
      .matches(LogVariableListener.getInvocations().get(0));

    LogVariableListener.reset();
  }


  protected void tearDown() throws Exception {
    beans.clear();

    super.tearDown();
  }

}
