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
package org.camunda.bpm.engine.test.bpmn.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.Problem;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.bpmn.behavior.BoundaryConditionalEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CompensationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartConditionalEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateConditionalEventBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ThrowEscalationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.SystemPropertiesRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 *
 * @author Joram Barrez
 */
public class BpmnParseTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public SystemPropertiesRule systemProperties = SystemPropertiesRule.resetPropsAfterTest();

  public RepositoryService repositoryService;
  public RuntimeService runtimeService;
  public ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void setup() {
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    processEngineConfiguration.setEnableXxeProcessing(false);
  }

  @After
  public void tearDown() {
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void testInvalidSubProcessWithTimerStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithTimerStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a timer start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("timerEventDefinition is not allowed on start event within a subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_3");
    }
  }

  @Test
  public void testInvalidSubProcessWithMessageStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithMessageStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process definition could be parsed, although the sub process contains not a blanco start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("messageEventDefinition only allowed on start event if subprocess is an event subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_3");
    }
  }

  @Test
  public void testInvalidSubProcessWithoutStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithoutStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process definition could be parsed, although the sub process did not contain a start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("subProcess must define a startEvent element", e.getMessage());
      assertErrors(e.getResorceReports().get(0).getErrors(), "SubProcess_1");
    }
  }

  @Test
  public void testInvalidSubProcessWithConditionalStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithConditionalStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a conditional start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("The content of element 'bpmn2:conditionalEventDefinition' is not complete.", e.getMessage());
      testRule.assertTextPresent("conditionalEventDefinition is not allowed on start event within a subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(2);
      assertThat(errors.get(0).getElementIds()).isEmpty();
      assertThat(errors.get(1).getMainElementId()).isEqualTo("StartEvent_2");
    }
  }

  @Test
  public void testInvalidSubProcessWithSignalStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithSignalStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a signal start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("signalEventDefintion only allowed on start event if subprocess is an event subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_3");
    }
  }

  @Test
  public void testInvalidSubProcessWithErrorStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithErrorStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a error start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("errorEventDefinition only allowed on start event if subprocess is an event subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_3");
    }
  }

  @Test
  public void testInvalidSubProcessWithEscalationStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithEscalationStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a escalation start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("escalationEventDefinition is not allowed on start event within a subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_3");
    }
  }

  @Test
  public void testInvalidSubProcessWithCompensationStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithCompensationStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a compensation start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("compensateEventDefinition is not allowed on start event within a subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_3");
    }
  }

  @Test
  public void testInvalidTransactionWithMessageStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithMessageStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process definition could be parsed, although the sub process contains not a blanco start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("messageEventDefinition only allowed on start event if subprocess is an event subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_2");
    }
  }

  @Test
  public void testInvalidTransactionWithTimerStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithTimerStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a timer start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("timerEventDefinition is not allowed on start event within a subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_2");
    }
  }

  @Test
  public void testInvalidTransactionWithConditionalStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithConditionalStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a conditional start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("The content of element 'bpmn2:conditionalEventDefinition' is not complete.", e.getMessage());
      testRule.assertTextPresent("conditionalEventDefinition is not allowed on start event within a subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(2);
      assertThat(errors.get(0).getElementIds()).isEmpty();
      assertThat(errors.get(1).getMainElementId()).isEqualTo("StartEvent_3");
    }
  }

  @Test
  public void testInvalidTransactionWithSignalStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithSignalStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a signal start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("signalEventDefintion only allowed on start event if subprocess is an event subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_2");
    }
  }

  @Test
  public void testInvalidTransactionWithErrorStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithErrorStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a error start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("errorEventDefinition only allowed on start event if subprocess is an event subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_2");
    }
  }

  @Test
  public void testInvalidTransactionWithEscalationStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithEscalationStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a escalation start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("escalationEventDefinition is not allowed on start event within a subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_2");
    }
  }

  @Test
  public void testInvalidTransactionWithCompensationStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithCompensationStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a compensation start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("compensateEventDefinition is not allowed on start event within a subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_2");
    }
  }

  @Test
  public void testInvalidProcessDefinition() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidProcessDefinition");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail();
    } catch (ParseException e) {
      testRule.assertTextPresent("cvc-complex-type.3.2.2:", e.getMessage());
      testRule.assertTextPresent("invalidAttribute", e.getMessage());
      testRule.assertTextPresent("process", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getElementIds()).isEmpty();
    }
  }

  @Test
  public void testExpressionParsingErrors() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testExpressionParsingErrors");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could not be parsed, the expression contains an escalation start event.");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("Error parsing '${currentUser()': syntax error at position 15, encountered 'null', expected '}'", e.getMessage());
    }
  }

  @Test
  public void testXmlParsingErrors() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testXMLParsingErrors");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could not be parsed, the XML contains an escalation start event.");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("The end-tag for element type \"bpmndi:BPMNLabel\" must end with a '>' delimiter", e.getMessage());
    }
  }

  @Test
  public void testInvalidSequenceFlowInAndOutEventSubProcess() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSequenceFlowInAndOutEventSubProcess");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process has incoming and outgoing sequence flows");
    } catch (ParseException e) {
      testRule.assertTextPresent("start event of event subprocess must be of type 'error', 'message', 'timer', 'signal', 'compensation' or 'escalation'", e.getMessage());
      testRule.assertTextPresent("Invalid incoming sequence flow of event subprocess", e.getMessage());
      testRule.assertTextPresent("Invalid outgoing sequence flow of event subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(3);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("StartEvent_2");
      assertThat(errors.get(1).getMainElementId()).isEqualTo("SequenceFlow_2");
      assertThat(errors.get(2).getMainElementId()).isEqualTo("SequenceFlow_1");
    }
  }

  @Test
  public void testInvalidProcessWithoutStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidProcessWithoutStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process definition could be parsed, although the process did not contain a start event.");
    } catch (ParseException e) {
      testRule.assertTextPresent("process must define a startEvent element", e.getMessage());
      assertErrors(e.getResorceReports().get(0).getErrors(), "Process_1");
    }
  }

  /**
   * this test case check if the multiple start event is supported the test case
   * doesn't fail in this behavior because the {@link BpmnParse} parse the event
   * definitions with if-else, this means only the first event definition is
   * taken
   **/
  @Test
  public void testParseMultipleStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseMultipleStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail();
    } catch (ParseException e) {
      // fail in "regular" subprocess
      testRule.assertTextPresent("timerEventDefinition is not allowed on start event within a subprocess", e.getMessage());
      testRule.assertTextPresent("messageEventDefinition only allowed on start event if subprocess is an event subprocess", e.getMessage());
      // doesn't fail in event subprocess/process because the bpmn parser parse
      // only this first event definition
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(2);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("startSubProcess");
      assertThat(errors.get(1).getMainElementId()).isEqualTo("startSubProcess");
    }
  }

  @Test
  public void testParseWithBpmnNamespacePrefix() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseWithBpmnNamespacePrefix.bpmn20.xml").deploy();
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
  }

  @Test
  public void testParseWithMultipleDocumentation() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseWithMultipleDocumentation.bpmn20.xml").deploy();
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
  }

  @Test
  public void testParseCollaborationPlane() {
    repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseCollaborationPlane.bpmn").deploy();
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
  }

  @Test
  public void testInvalidAsyncAfterEventBasedGateway() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidAsyncAfterEventBasedGateway");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail();
    } catch (ParseException e) {
      // fail on asyncAfter
      testRule.assertTextPresent("'asyncAfter' not supported for", e.getMessage());
      assertErrors(e.getResorceReports().get(0).getErrors(), "eventBasedGateway");
    }
  }

  @Deployment
  @Test
  public void testParseDiagramInterchangeElements() {

    // Graphical information is not yet exposed publicly, so we need to do some
    // plumbing
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    ProcessDefinitionEntity processDefinitionEntity = commandExecutor.execute(new Command<ProcessDefinitionEntity>() {
      @Override
      public ProcessDefinitionEntity execute(CommandContext commandContext) {
        return Context.getProcessEngineConfiguration().getDeploymentCache().findDeployedLatestProcessDefinitionByKey("myProcess");
      }
    });

    assertNotNull(processDefinitionEntity);
    assertEquals(7, processDefinitionEntity.getActivities().size());

    // Check if diagram has been created based on Diagram Interchange when it's
    // not a headless instance
    List<String> resourceNames = repositoryService.getDeploymentResourceNames(processDefinitionEntity.getDeploymentId());
    if (processEngineConfiguration.isCreateDiagramOnDeploy()) {
      assertEquals(2, resourceNames.size());
    } else {
      assertEquals(1, resourceNames.size());
    }

    for (ActivityImpl activity : processDefinitionEntity.getActivities()) {

      if (activity.getId().equals("theStart")) {
        assertActivityBounds(activity, 70, 255, 30, 30);
      } else if (activity.getId().equals("task1")) {
        assertActivityBounds(activity, 176, 230, 100, 80);
      } else if (activity.getId().equals("gateway1")) {
        assertActivityBounds(activity, 340, 250, 40, 40);
      } else if (activity.getId().equals("task2")) {
        assertActivityBounds(activity, 445, 138, 100, 80);
      } else if (activity.getId().equals("gateway2")) {
        assertActivityBounds(activity, 620, 250, 40, 40);
      } else if (activity.getId().equals("task3")) {
        assertActivityBounds(activity, 453, 304, 100, 80);
      } else if (activity.getId().equals("theEnd")) {
        assertActivityBounds(activity, 713, 256, 28, 28);
      }

      for (PvmTransition sequenceFlow : activity.getOutgoingTransitions()) {
        assertTrue(((TransitionImpl) sequenceFlow).getWaypoints().size() >= 4);

        TransitionImpl transitionImpl = (TransitionImpl) sequenceFlow;
        if (transitionImpl.getId().equals("flowStartToTask1")) {
          assertSequenceFlowWayPoints(transitionImpl, 100, 270, 176, 270);
        } else if (transitionImpl.getId().equals("flowTask1ToGateway1")) {
          assertSequenceFlowWayPoints(transitionImpl, 276, 270, 340, 270);
        } else if (transitionImpl.getId().equals("flowGateway1ToTask2")) {
          assertSequenceFlowWayPoints(transitionImpl, 360, 250, 360, 178, 445, 178);
        } else if (transitionImpl.getId().equals("flowGateway1ToTask3")) {
          assertSequenceFlowWayPoints(transitionImpl, 360, 290, 360, 344, 453, 344);
        } else if (transitionImpl.getId().equals("flowTask2ToGateway2")) {
          assertSequenceFlowWayPoints(transitionImpl, 545, 178, 640, 178, 640, 250);
        } else if (transitionImpl.getId().equals("flowTask3ToGateway2")) {
          assertSequenceFlowWayPoints(transitionImpl, 553, 344, 640, 344, 640, 290);
        } else if (transitionImpl.getId().equals("flowGateway2ToEnd")) {
          assertSequenceFlowWayPoints(transitionImpl, 660, 270, 713, 270);
        }

      }
    }
  }

  @Deployment
  @Test
  public void testParseNamespaceInConditionExpressionType() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    ProcessDefinitionEntity processDefinitionEntity = commandExecutor.execute(new Command<ProcessDefinitionEntity>() {
      @Override
      public ProcessDefinitionEntity execute(CommandContext commandContext) {
        return Context.getProcessEngineConfiguration().getDeploymentCache().findDeployedLatestProcessDefinitionByKey("resolvableNamespacesProcess");
      }
    });

    // Test that the process definition has been deployed
    assertNotNull(processDefinitionEntity);
    PvmActivity activity = processDefinitionEntity.findActivity("ExclusiveGateway_1");
    assertNotNull(activity);

    // Test that the conditions has been resolved
    for (PvmTransition transition : activity.getOutgoingTransitions()) {
      if (transition.getDestination().getId().equals("Task_2")) {
        assertTrue(transition.getProperty("conditionText").equals("#{approved}"));
      } else if (transition.getDestination().getId().equals("Task_3")) {
        assertTrue(transition.getProperty("conditionText").equals("#{!approved}"));
      } else {
        fail("Something went wrong");
      }

    }
  }

  @Deployment
  @Test
  public void testParseDiagramInterchangeElementsForUnknownModelElements() {
  }

  /**
   * We want to make sure that BPMNs created with the namespace http://activiti.org/bpmn still work.
   */
  @Test
  @Deployment
  public void testParseDefinitionWithDeprecatedActivitiNamespace(){

  }

  @Test
  @Deployment
  public void testParseDefinitionWithCamundaNamespace(){

  }

  @Deployment
  @Test
  public void testParseCompensationEndEvent() {
    ActivityImpl endEvent = findActivityInDeployedProcessDefinition("end");

    assertEquals("compensationEndEvent", endEvent.getProperty("type"));
    assertEquals(Boolean.TRUE, endEvent.getProperty(BpmnParse.PROPERTYNAME_THROWS_COMPENSATION));
    assertEquals(CompensationEventActivityBehavior.class, endEvent.getActivityBehavior().getClass());
  }

  @Deployment
  @Test
  public void testParseCompensationStartEvent() {
    ActivityImpl compensationStartEvent = findActivityInDeployedProcessDefinition("compensationStartEvent");

    assertEquals("compensationStartEvent", compensationStartEvent.getProperty("type"));
    assertEquals(EventSubProcessStartEventActivityBehavior.class, compensationStartEvent.getActivityBehavior().getClass());

    ActivityImpl compensationEventSubProcess = (ActivityImpl) compensationStartEvent.getFlowScope();
    assertEquals(Boolean.TRUE, compensationEventSubProcess.getProperty(BpmnParse.PROPERTYNAME_IS_FOR_COMPENSATION));

    ScopeImpl subprocess = compensationEventSubProcess.getFlowScope();
    assertEquals(compensationEventSubProcess.getActivityId(), subprocess.getProperty(BpmnParse.PROPERTYNAME_COMPENSATION_HANDLER_ID));
  }

  @Deployment
  @Test
  public void testParseAsyncMultiInstanceBody(){
    ActivityImpl innerTask = findActivityInDeployedProcessDefinition("miTask");
    ActivityImpl miBody = innerTask.getParentFlowScopeActivity();

    assertTrue(miBody.isAsyncBefore());
    assertTrue(miBody.isAsyncAfter());

    assertFalse(innerTask.isAsyncBefore());
    assertFalse(innerTask.isAsyncAfter());
  }

  @Deployment
  @Test
  public void testParseAsyncActivityWrappedInMultiInstanceBody(){
    ActivityImpl innerTask = findActivityInDeployedProcessDefinition("miTask");
    assertTrue(innerTask.isAsyncBefore());
    assertTrue(innerTask.isAsyncAfter());

    ActivityImpl miBody = innerTask.getParentFlowScopeActivity();
    assertFalse(miBody.isAsyncBefore());
    assertFalse(miBody.isAsyncAfter());
  }

  @Deployment
  @Test
  public void testParseAsyncActivityWrappedInMultiInstanceBodyWithAsyncMultiInstance(){
    ActivityImpl innerTask = findActivityInDeployedProcessDefinition("miTask");
    assertEquals(true, innerTask.isAsyncBefore());
    assertEquals(false, innerTask.isAsyncAfter());

    ActivityImpl miBody = innerTask.getParentFlowScopeActivity();
    assertEquals(false, miBody.isAsyncBefore());
    assertEquals(true, miBody.isAsyncAfter());
  }

  @Test
  public void testParseSwitchedSourceAndTargetRefsForAssociations() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseSwitchedSourceAndTargetRefsForAssociations.bpmn20.xml").deploy();

    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.compensationMiActivity.bpmn20.xml")
  @Test
  public void testParseCompensationHandlerOfMiActivity() {
    ActivityImpl miActivity = findActivityInDeployedProcessDefinition("undoBookHotel");
    ScopeImpl flowScope = miActivity.getFlowScope();

    assertEquals(ActivityTypes.MULTI_INSTANCE_BODY, flowScope.getProperty(BpmnParse.PROPERTYNAME_TYPE));
    assertEquals("bookHotel" + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX, ((ActivityImpl) flowScope).getActivityId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.compensationMiSubprocess.bpmn20.xml")
  @Test
  public void testParseCompensationHandlerOfMiSubprocess() {
    ActivityImpl miActivity = findActivityInDeployedProcessDefinition("undoBookHotel");
    ScopeImpl flowScope = miActivity.getFlowScope();

    assertEquals(ActivityTypes.MULTI_INSTANCE_BODY, flowScope.getProperty(BpmnParse.PROPERTYNAME_TYPE));
    assertEquals("scope" + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX, ((ActivityImpl) flowScope).getActivityId());
  }

  @Deployment
  @Test
  public void testParseSignalStartEvent(){
    ActivityImpl signalStartActivity = findActivityInDeployedProcessDefinition("start");

    assertEquals(ActivityTypes.START_EVENT_SIGNAL, signalStartActivity.getProperty("type"));
    assertEquals(NoneStartEventActivityBehavior.class, signalStartActivity.getActivityBehavior().getClass());
  }

  @Deployment
  @Test
  public void testParseEscalationBoundaryEvent() {
    ActivityImpl escalationBoundaryEvent = findActivityInDeployedProcessDefinition("escalationBoundaryEvent");

    assertEquals(ActivityTypes.BOUNDARY_ESCALATION, escalationBoundaryEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(BoundaryEventActivityBehavior.class, escalationBoundaryEvent.getActivityBehavior().getClass());
  }

  @Deployment
  @Test
  public void testParseEscalationIntermediateThrowingEvent() {
    ActivityImpl escalationThrowingEvent = findActivityInDeployedProcessDefinition("escalationThrowingEvent");

    assertEquals(ActivityTypes.INTERMEDIATE_EVENT_ESCALATION_THROW, escalationThrowingEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(ThrowEscalationEventActivityBehavior.class, escalationThrowingEvent.getActivityBehavior().getClass());
  }

  @Deployment
  @Test
  public void testParseEscalationEndEvent() {
    ActivityImpl escalationEndEvent = findActivityInDeployedProcessDefinition("escalationEndEvent");

    assertEquals(ActivityTypes.END_EVENT_ESCALATION, escalationEndEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(ThrowEscalationEventActivityBehavior.class, escalationEndEvent.getActivityBehavior().getClass());
  }

  @Deployment
  @Test
  public void testParseEscalationStartEvent() {
    ActivityImpl escalationStartEvent = findActivityInDeployedProcessDefinition("escalationStartEvent");

    assertEquals(ActivityTypes.START_EVENT_ESCALATION, escalationStartEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(EventSubProcessStartEventActivityBehavior.class, escalationStartEvent.getActivityBehavior().getClass());
  }


  public void parseInvalidConditionalEvent(String processDefinitionResource, String elementId) {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), processDefinitionResource);
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, conditional event definition contains no condition.");
    } catch (ParseException e) {
      testRule.assertTextPresent("The content of element 'bpmn:conditionalEventDefinition' is not complete.", e.getMessage());
      testRule.assertTextPresent("Conditional event must contain an expression for evaluation.", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(2);
      assertThat(errors.get(0).getElementIds()).isEmpty();
      assertThat(errors.get(1).getMainElementId()).isEqualTo(elementId);
    }
  }

  @Test
  public void testParseInvalidConditionalBoundaryEvent() {
    parseInvalidConditionalEvent("testParseInvalidConditionalBoundaryEvent", "conditionalBoundaryEvent");
  }

  @Deployment
  @Test
  public void testParseConditionalBoundaryEvent() {
    ActivityImpl conditionalBoundaryEvent = findActivityInDeployedProcessDefinition("conditionalBoundaryEvent");

    assertEquals(ActivityTypes.BOUNDARY_CONDITIONAL, conditionalBoundaryEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(BoundaryConditionalEventActivityBehavior.class, conditionalBoundaryEvent.getActivityBehavior().getClass());
  }

  @Deployment
  @Test
  public void testParseAsyncBoundaryEvent() {
    ActivityImpl conditionalBoundaryEvent1 = findActivityInDeployedProcessDefinition("conditionalBoundaryEvent1");
    ActivityImpl conditionalBoundaryEvent2 = findActivityInDeployedProcessDefinition("conditionalBoundaryEvent2");

    assertTrue(conditionalBoundaryEvent1.isAsyncAfter());
    assertTrue(conditionalBoundaryEvent1.isAsyncBefore());

    assertFalse(conditionalBoundaryEvent2.isAsyncAfter());
    assertFalse(conditionalBoundaryEvent2.isAsyncBefore());
  }

  @Test
  public void testParseInvalidIntermediateConditionalEvent() {
    parseInvalidConditionalEvent("testParseInvalidIntermediateConditionalEvent", "intermediateConditionalEvent");
  }

  @Deployment
  @Test
  public void testParseIntermediateConditionalEvent() {
    ActivityImpl intermediateConditionalEvent = findActivityInDeployedProcessDefinition("intermediateConditionalEvent");

    assertEquals(ActivityTypes.INTERMEDIATE_EVENT_CONDITIONAL, intermediateConditionalEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(IntermediateConditionalEventBehavior.class, intermediateConditionalEvent.getActivityBehavior().getClass());
  }

  @Test
  public void testParseInvalidEventSubprocessConditionalStartEvent() {
    parseInvalidConditionalEvent("testParseInvalidEventSubprocessConditionalStartEvent", "conditionalStartEventSubProcess");
  }

  @Deployment
  @Test
  public void testParseEventSubprocessConditionalStartEvent() {
    ActivityImpl conditionalStartEventSubProcess = findActivityInDeployedProcessDefinition("conditionalStartEventSubProcess");

    assertEquals(ActivityTypes.START_EVENT_CONDITIONAL, conditionalStartEventSubProcess.getProperties().get(BpmnProperties.TYPE));
    assertEquals(EventSubProcessStartConditionalEventActivityBehavior.class, conditionalStartEventSubProcess.getActivityBehavior().getClass());

  }

  protected void assertActivityBounds(ActivityImpl activity, int x, int y, int width, int height) {
    assertEquals(x, activity.getX());
    assertEquals(y, activity.getY());
    assertEquals(width, activity.getWidth());
    assertEquals(height, activity.getHeight());
  }

  protected void assertSequenceFlowWayPoints(TransitionImpl sequenceFlow, Integer... waypoints) {
    assertEquals(waypoints.length, sequenceFlow.getWaypoints().size());
    for (int i = 0; i < waypoints.length; i++) {
      assertEquals(waypoints[i], sequenceFlow.getWaypoints().get(i));
    }
  }

  protected ActivityImpl findActivityInDeployedProcessDefinition(String activityId) {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinition);

    ProcessDefinitionEntity cachedProcessDefinition = processEngineConfiguration.getDeploymentCache()
                                                        .getProcessDefinitionCache()
                                                        .get(processDefinition.getId());
    return cachedProcessDefinition.findActivity(activityId);
  }

  @Test
  public void testNoCamundaInSourceThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaInSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:in extension element should contain source!");
    } catch (ParseException e) {
      testRule.assertTextPresent("Missing parameter 'source' or 'sourceExpression' when passing variables", e.getMessage());
      testRule.assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(2);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("callActivity");
      assertThat(errors.get(1).getMainElementId()).isEqualTo("callActivity");
    }
  }

  @Test
  public void testNoCamundaInSourceShouldWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaInSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
    }
  }

  @Test
  public void testEmptyCamundaInSourceThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaInSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:in extension element should contain source!");
    } catch (ParseException e) {
      testRule.assertTextPresent("Empty attribute 'source' when passing variables", e.getMessage());
      testRule.assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(2);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("callActivity");
      assertThat(errors.get(1).getMainElementId()).isEqualTo("callActivity");
    }
  }

  @Test
  public void testEmptyCamundaInSourceWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaInSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
    }
  }

  @Test
  public void testNoCamundaInTargetThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaInTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:in extension element should contain target!");
    } catch (ParseException e) {
      testRule.assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
      assertErrors(e.getResorceReports().get(0).getErrors(), "callActivity");
    }
  }

  @Test
  public void testNoCamundaInTargetWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaInTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:in extension element should contain target!");
    } catch (ParseException e) {
      testRule.assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
      assertErrors(e.getResorceReports().get(0).getErrors(), "callActivity");
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
    }
  }

  @Test
  public void testEmptyCamundaInTargetThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaInTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:in extension element should contain target!");
    } catch (ParseException e) {
      testRule.assertTextPresent("Empty attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
      assertErrors(e.getResorceReports().get(0).getErrors(), "callActivity");
    }
  }

  @Test
  public void testEmptyCamundaInTargetWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaInTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
    }
  }

  @Test
  public void testNoCamundaOutSourceThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaOutSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:out extension element should contain source!");
    } catch (ParseException e) {
      testRule.assertTextPresent("Missing parameter 'source' or 'sourceExpression' when passing variables", e.getMessage());
      testRule.assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(2);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("callActivity");
      assertThat(errors.get(1).getMainElementId()).isEqualTo("callActivity");
    }
  }

  @Test
  public void testNoCamundaOutSourceWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaOutSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
    }
  }

  @Test
  public void testEmptyCamundaOutSourceThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaOutSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:out extension element should contain source!");
    } catch (ParseException e) {
      testRule.assertTextPresent("Empty attribute 'source' when passing variables", e.getMessage());
      testRule.assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(2);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("callActivity");
      assertThat(errors.get(1).getMainElementId()).isEqualTo("callActivity");
    }
  }

  @Test
  public void testEmptyCamundaOutSourceWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaOutSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
    }
  }

  @Test
  public void testNoCamundaOutTargetThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaOutTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:out extension element should contain target!");
    } catch (ParseException e) {
      testRule.assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
      assertErrors(e.getResorceReports().get(0).getErrors(), "callActivity");
    }
  }

  @Test
  public void testNoCamundaOutTargetWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaOutTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:out extension element should contain target!");
    } catch (ParseException e) {
      testRule.assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
      assertErrors(e.getResorceReports().get(0).getErrors(), "callActivity");
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
    }
  }

  @Test
  public void testEmptyCamundaOutTargetThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaOutTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:out extension element should contain target!");
    } catch (ParseException e) {
      testRule.assertTextPresent("Empty attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
      assertErrors(e.getResorceReports().get(0).getErrors(), "callActivity");
    }
  }

  @Test
  public void testEmptyCamundaOutTargetWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaOutTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
    }
  }

  @Deployment
  @Test
  public void testParseProcessDefinitionTtl() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertNotNull(processDefinitions);
    assertEquals(1, processDefinitions.size());

    Integer timeToLive = processDefinitions.get(0).getHistoryTimeToLive();
    assertNotNull(timeToLive);
    assertEquals(5, timeToLive.intValue());

    assertTrue(processDefinitions.get(0).isStartableInTasklist());
  }

  @Deployment
  @Test
  public void testParseProcessDefinitionStringTtl() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertNotNull(processDefinitions);
    assertEquals(1, processDefinitions.size());

    Integer timeToLive = processDefinitions.get(0).getHistoryTimeToLive();
    assertNotNull(timeToLive);
    assertEquals(5, timeToLive.intValue());
  }

  @Test
  public void testParseProcessDefinitionMalformedStringTtl() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionMalformedStringTtl");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition historyTimeToLive value can not be parsed.");
    } catch (ParseException e) {
      testRule.assertTextPresent("Cannot parse historyTimeToLive", e.getMessage());
      assertErrors(e.getResorceReports().get(0).getErrors(), "oneTaskProcess");
    }
  }

  @Deployment
  @Test
  public void testParseProcessDefinitionEmptyTtl() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertNotNull(processDefinitions);
    assertEquals(1, processDefinitions.size());

    Integer timeToLive = processDefinitions.get(0).getHistoryTimeToLive();
    assertNull(timeToLive);
  }

  @Deployment
  @Test
  public void testParseProcessDefinitionWithoutTtl() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertNotNull(processDefinitions);
    assertEquals(1, processDefinitions.size());

    Integer timeToLive = processDefinitions.get(0).getHistoryTimeToLive();
    assertNull(timeToLive);
  }

  @Test
  public void testParseProcessDefinitionWithoutTtlWithConfigDefault() {
    processEngineConfiguration.setHistoryTimeToLive("6");
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionWithoutTtl");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
      assertNotNull(processDefinitions);
      assertEquals(1, processDefinitions.size());

      Integer timeToLive = processDefinitions.get(0).getHistoryTimeToLive();
      assertNotNull(timeToLive);
      assertEquals(6, timeToLive.intValue());
    } finally {
      processEngineConfiguration.setHistoryTimeToLive(null);
    }
  }

  @Test
  public void testParseProcessDefinitionWithoutTtlWithMalformedConfigDefault() {
    processEngineConfiguration.setHistoryTimeToLive("PP555DDD");
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionWithoutTtl");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition historyTimeToLive value can not be parsed.");
    } catch (ParseException e) {
      testRule.assertTextPresent("Cannot parse historyTimeToLive", e.getMessage());
    } finally {
      processEngineConfiguration.setHistoryTimeToLive(null);
    }
  }

  @Test
  public void testParseProcessDefinitionWithoutTtlWithInvalidConfigDefault() {
    processEngineConfiguration.setHistoryTimeToLive("invalidValue");
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionWithoutTtl");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition historyTimeToLive value can not be parsed.");
    } catch (ParseException e) {
      testRule.assertTextPresent("Cannot parse historyTimeToLive", e.getMessage());
    } finally {
      processEngineConfiguration.setHistoryTimeToLive(null);
    }
  }

  @Test
  public void testParseProcessDefinitionWithoutTtlWithNegativeConfigDefault() {
    processEngineConfiguration.setHistoryTimeToLive("-6");
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionWithoutTtl");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition historyTimeToLive value can not be parsed.");
    } catch (ParseException e) {
      testRule.assertTextPresent("Cannot parse historyTimeToLive", e.getMessage());
    } finally {
      processEngineConfiguration.setHistoryTimeToLive(null);
    }
  }

  @Test
  public void testParseProcessDefinitionInvalidTtl() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionInvalidTtl");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition historyTimeToLive value can not be parsed.");
    } catch (ParseException e) {
      testRule.assertTextPresent("Cannot parse historyTimeToLive", e.getMessage());
    }
  }

  @Test
  public void testParseProcessDefinitionNegativTtl() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionNegativeTtl");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition historyTimeToLive value can not be parsed.");
    } catch (ParseException e) {
      testRule.assertTextPresent("Cannot parse historyTimeToLive", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testParseProcessDefinitionStartable() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertNotNull(processDefinitions);
    assertEquals(1, processDefinitions.size());

    assertFalse(processDefinitions.get(0).isStartableInTasklist());
  }

  @Test
  public void testInvalidExecutionListenerClassDefinition() {
    // given
    String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidExecutionListenerClassDefinition");
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name(resource).addClasspathResource(resource);

    try {
      // when
      deploymentBuilder.deploy();
    } catch (ParseException e) {
      // then
      testRule.assertTextPresent("Attribute 'class' cannot be empty", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("Task_1v32izq");
    }
  }

  @Test
  public void testInvalidExecutionListenerDelegateDefinition() {
    // given
    String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidExecutionListenerDelegateDefinition");
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name(resource).addClasspathResource(resource);

    try {
      // when
      deploymentBuilder.deploy();
    } catch (ParseException e) {
      // then
      testRule.assertTextPresent("Attribute 'delegateExpression' cannot be empty", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("Task_1v32izq");
    }
  }

  @Test
  public void shouldPreventXxeProcessing() {
    // given
    String resource =
        TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionXXE");

    // when/then
    assertThatThrownBy(() -> repositoryService.createDeployment()
        .name(resource)
        .addClasspathResource(resource)
        .deploy())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("DOCTYPE is disallowed when the feature " +
          "\"http://apache.org/xml/features/disallow-doctype-decl\" set to true.");
  }

  @Test
  public void shouldAllowXxeProcessing() {
    // given
    processEngineConfiguration.setEnableXxeProcessing(true);

    String resource =
        TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionXXE");

    // when/then
    assertThatThrownBy(() -> repositoryService.createDeployment()
        .name(resource)
        .addClasspathResource(resource)
        .deploy())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Could not parse")
      .hasMessageContaining("file.txt");
  }

  @Test
  public void testFeatureSecureProcessingRejectsDefinitionDueToAttributeLimit() {
    // IBM JDKs do not check on attribute number limits, skip the test there
    Assume.assumeThat(System.getProperty("java.vm.vendor"), not(containsString("IBM")));
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionFSP");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Attribute Number Limit should have been exceeded while parsing the model!");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("JAXP00010002", e.getMessage());
    }
  }

  @Test
  public void testFeatureSecureProcessingAcceptsDefinitionWhenAttributeLimitOverridden() {
    // given
    System.setProperty("jdk.xml.elementAttributeLimit", "0");

    String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionFSP");
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name(resource).addClasspathResource(resource);

    // when
    testRule.deploy(deploymentBuilder);

    // then
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
  }

  @Test
  public void testFeatureSecureProcessingRestrictExternalSchemaAccess() {
    // given
    // the external schema access property is not supported on certain
    // IBM JDK versions, in which case schema access cannot be restricted
    Assume.assumeTrue(doesJdkSupportExternalSchemaAccessProperty());

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask()
        .endEvent()
        .done();

    DeploymentBuilder builder = repositoryService.createDeployment()
        .addModelInstance("process.bpmn", process);

    System.setProperty("javax.xml.accessExternalSchema", ""); // empty string prohibits all external schema access

    // when/then
    // fails, because the BPMN XSD references other external XSDs, e.g. BPMNDI
    assertThatThrownBy(() -> testRule.deploy(builder))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Could not parse 'process.bpmn'");

  }

  @Test
  public void testFeatureSecureProcessingAllowExternalSchemaAccess() {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask()
        .endEvent()
        .done();

    System.setProperty("javax.xml.accessExternalSchema", "all"); // empty string prohibits all external schema access

    // when
    DeploymentWithDefinitions deployment = testRule.deploy(process);

    // then
    assertThat(deployment).isNotNull();
  }

  @Test
  public void testTimerWithoutFullDefinition() {
    try {
      String timerWithoutDetails = "<?xml version='1.0' encoding='UTF-8'?>" +
          "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
          "  xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL'" +
          "  xmlns:camunda='http://camunda.org/schema/1.0/bpmn'" +
          "  targetNamespace='Examples'>" +
          "  <process id='process' isExecutable='true'>" +
          "    <startEvent id='start'>" +
          "      <timerEventDefinition id='TimerEventDefinition_1'/>" +
          "    </startEvent>" +
          "  </process>" +
          "</definitions>";
      repositoryService.createDeployment().addString("process.bpmn20.xml", timerWithoutDetails).deploy();
      fail("Exception expected: Process definition could be parsed, it contains uncomplete timer event definition.");
    } catch (ParseException e) {
      testRule.assertTextPresent("Timer needs configuration (either timeDate, timeCycle or timeDuration is needed).", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("start");
    }
  }

  @Test
  public void testSequenceFlowNoIdAndUnexistantDestination() {
    try {
      String incorrectSequenceFlow = "<?xml version='1.0' encoding='UTF-8'?>" +
          "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
          "  xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL'" +
          "  xmlns:camunda='http://camunda.org/schema/1.0/bpmn'" +
          "  targetNamespace='Examples'>" +
          "  <process id='process' isExecutable='true'>" +
          "    <startEvent id='start'/>" +
          "    <sequenceFlow sourceRef='start' targetRef='eventBasedGateway' />" +
          "  </process>" +
          "</definitions>";
      repositoryService.createDeployment().addString("process.bpmn20.xml", incorrectSequenceFlow).deploy();
      fail("Exception expected: Unexisting target.");
    } catch (ParseException e) {
      testRule.assertTextPresent("Invalid destination 'eventBasedGateway' of sequence flow 'null'", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(2);
      assertThat(errors.get(1).getMainElementId()).isNull();
      assertThat(errors.get(1).getElementIds()).isEmpty();
    }
  }

  @Test
  public void testMultipleTimerStartEvents() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testMultipleTimerStartEvents");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, it contains multiple multiple none start events or timer start events.");
    } catch (ParseException e) {
      testRule.assertTextPresent("multiple none start events or timer start events not supported on process definition", e.getMessage());
      testRule.assertTextPresent("multiple start events not supported for subprocess", e.getMessage());
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors).hasSize(4);
      assertThat(errors.get(0).getMainElementId()).isEqualTo("timerStart2");
      assertThat(errors.get(1).getMainElementId()).isEqualTo("plainStart1");
      assertThat(errors.get(2).getMainElementId()).isEqualTo("plainStart2");
      assertThat(errors.get(3).getMainElementId()).isEqualTo("plainStartInSub1");
    }
  }

  @Test
  public void testParseEmptyExtensionProperty() {
    // given process definition with empty property (key and value = null) is deployed
    // when
    repositoryService.createDeployment()
    .addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseEmptyExtensionProperty.bpmn").deploy();

    // then
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(1L);
  }

  protected boolean doesJdkSupportExternalSchemaAccessProperty() {
    String jvmVendor = System.getProperty("java.vm.vendor");
    String javaVersion = System.getProperty("java.version");

    boolean isIbmJDK = jvmVendor != null && jvmVendor.contains("IBM");
    boolean isJava6 = javaVersion != null && javaVersion.startsWith("1.6");
    boolean isJava7 = javaVersion != null && javaVersion.startsWith("1.7");

    return !isJava6 && !(isIbmJDK && isJava7);

  }


  protected void assertErrors(List<Problem> errors, String elementId) {
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0).getMainElementId()).isEqualTo(elementId);
  }

}
