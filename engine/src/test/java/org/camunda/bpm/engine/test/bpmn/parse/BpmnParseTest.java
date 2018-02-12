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

package org.camunda.bpm.engine.test.bpmn.parse;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CompensationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ThrowEscalationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.impl.bpmn.behavior.BoundaryConditionalEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartConditionalEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateConditionalEventBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
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
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 *
 * @author Joram Barrez
 */
public class BpmnParseTest extends PluggableProcessEngineTestCase {
  public void testInvalidSubProcessWithTimerStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithTimerStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a timer start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("timerEventDefinition is not allowed on start event within a subprocess", e.getMessage());
    }
  }

  public void testInvalidSubProcessWithMessageStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithMessageStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process definition could be parsed, although the sub process contains not a blanco start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("messageEventDefinition only allowed on start event if subprocess is an event subprocess", e.getMessage());
    }
  }

  public void testInvalidSubProcessWithConditionalStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithConditionalStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a conditional start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("conditionalEventDefinition is not allowed on start event within a subprocess", e.getMessage());
    }
  }

  public void testInvalidSubProcessWithSignalStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithSignalStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a signal start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("signalEventDefintion only allowed on start event if subprocess is an event subprocess", e.getMessage());
    }
  }

  public void testInvalidSubProcessWithErrorStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithErrorStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a error start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("errorEventDefinition only allowed on start event if subprocess is an event subprocess", e.getMessage());
    }
  }

  public void testInvalidSubProcessWithEscalationStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithEscalationStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a escalation start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("escalationEventDefinition is not allowed on start event within a subprocess", e.getMessage());
    }
  }

  public void testInvalidSubProcessWithCompensationStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSubProcessWithCompensationStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a compensation start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("compensateEventDefinition is not allowed on start event within a subprocess", e.getMessage());
    }
  }

  public void testInvalidTransactionWithMessageStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithMessageStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process definition could be parsed, although the sub process contains not a blanco start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("messageEventDefinition only allowed on start event if subprocess is an event subprocess", e.getMessage());
    }
  }

  public void testInvalidTransactionWithTimerStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithTimerStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a timer start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("timerEventDefinition is not allowed on start event within a subprocess", e.getMessage());
    }
  }

  public void testInvalidTransactionWithConditionalStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithConditionalStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a conditional start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("conditionalEventDefinition is not allowed on start event within a subprocess", e.getMessage());
    }
  }

  public void testInvalidTransactionWithSignalStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithSignalStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a signal start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("signalEventDefintion only allowed on start event if subprocess is an event subprocess", e.getMessage());
    }
  }

  public void testInvalidTransactionWithErrorStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithErrorStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a error start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("errorEventDefinition only allowed on start event if subprocess is an event subprocess", e.getMessage());
    }
  }

  public void testInvalidTransactionWithEscalationStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithEscalationStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a escalation start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("escalationEventDefinition is not allowed on start event within a subprocess", e.getMessage());
    }
  }

  public void testInvalidTransactionWithCompensationStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidTransactionWithCompensationStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process contains a compensation start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("compensateEventDefinition is not allowed on start event within a subprocess", e.getMessage());
    }
  }

  public void testInvalidProcessDefinition() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidProcessDefinition");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresent("cvc-complex-type.3.2.2:", e.getMessage());
      assertTextPresent("invalidAttribute", e.getMessage());
      assertTextPresent("process", e.getMessage());
    }
  }

  public void testExpressionParsingErrors() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testExpressionParsingErrors");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could not be parsed, the expression contains an escalation start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("Error parsing '${currentUser()': syntax error at position 15, encountered 'null', expected '}'", e.getMessage());
    }
  }

  public void testXmlParsingErrors() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testXMLParsingErrors");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could not be parsed, the XML contains an escalation start event.");
    } catch (ProcessEngineException e) {
      assertTextPresent("The end-tag for element type \"bpmndi:BPMNLabel\" must end with a '>' delimiter", e.getMessage());
    }
  }

  public void testInvalidSequenceFlowInAndOutEventSubProcess() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidSequenceFlowInAndOutEventSubProcess");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, although the sub process has incoming and outgoing sequence flows");
    } catch (ProcessEngineException e) {
      assertTextPresent("Invalid incoming sequence flow of event subprocess", e.getMessage());
      assertTextPresent("Invalid outgoing sequence flow of event subprocess", e.getMessage());
    }
  }

  /**
   * this test case check if the multiple start event is supported the test case
   * doesn't fail in this behavior because the {@link BpmnParse} parse the event
   * definitions with if-else, this means only the first event definition is
   * taken
   **/
  public void testParseMultipleStartEvent() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseMultipleStartEvent");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail();
    } catch (ProcessEngineException e) {
      // fail in "regular" subprocess
      assertTextPresent("timerEventDefinition is not allowed on start event within a subprocess", e.getMessage());
      assertTextPresent("messageEventDefinition only allowed on start event if subprocess is an event subprocess", e.getMessage());
      // doesn't fail in event subprocess/process because the bpmn parser parse
      // only this first event definition
    }
  }

  public void testParseWithBpmnNamespacePrefix() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseWithBpmnNamespacePrefix.bpmn20.xml").deploy();
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());

    repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
  }

  public void testParseWithMultipleDocumentation() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseWithMultipleDocumentation.bpmn20.xml").deploy();
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());

    repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
  }

  public void testParseCollaborationPlane() {
    repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseCollaborationPlane.bpmn").deploy();
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());

    repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
  }

  public void testInvalidAsyncAfterEventBasedGateway() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidAsyncAfterEventBasedGateway");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail();
    } catch (ProcessEngineException e) {
      // fail on asyncAfter
      assertTextPresent("'asyncAfter' not supported for", e.getMessage());
    }
  }

  @Deployment
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
  public void testParseCompensationEndEvent() {
    ActivityImpl endEvent = findActivityInDeployedProcessDefinition("end");

    assertEquals("compensationEndEvent", endEvent.getProperty("type"));
    assertEquals(Boolean.TRUE, endEvent.getProperty(BpmnParse.PROPERTYNAME_THROWS_COMPENSATION));
    assertEquals(CompensationEventActivityBehavior.class, endEvent.getActivityBehavior().getClass());
  }

  @Deployment
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
  public void testParseAsyncMultiInstanceBody(){
    ActivityImpl innerTask = findActivityInDeployedProcessDefinition("miTask");
    ActivityImpl miBody = innerTask.getParentFlowScopeActivity();

    assertTrue(miBody.isAsyncBefore());
    assertTrue(miBody.isAsyncAfter());

    assertFalse(innerTask.isAsyncBefore());
    assertFalse(innerTask.isAsyncAfter());
  }

  @Deployment
  public void testParseAsyncActivityWrappedInMultiInstanceBody(){
    ActivityImpl innerTask = findActivityInDeployedProcessDefinition("miTask");
    assertTrue(innerTask.isAsyncBefore());
    assertTrue(innerTask.isAsyncAfter());

    ActivityImpl miBody = innerTask.getParentFlowScopeActivity();
    assertFalse(miBody.isAsyncBefore());
    assertFalse(miBody.isAsyncAfter());
  }

  @Deployment
  public void testParseAsyncActivityWrappedInMultiInstanceBodyWithAsyncMultiInstance(){
    ActivityImpl innerTask = findActivityInDeployedProcessDefinition("miTask");
    assertEquals(true, innerTask.isAsyncBefore());
    assertEquals(false, innerTask.isAsyncAfter());

    ActivityImpl miBody = innerTask.getParentFlowScopeActivity();
    assertEquals(false, miBody.isAsyncBefore());
    assertEquals(true, miBody.isAsyncAfter());
  }

  public void testParseSwitchedSourceAndTargetRefsForAssociations() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseSwitchedSourceAndTargetRefsForAssociations.bpmn20.xml").deploy();

    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());

    repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.compensationMiActivity.bpmn20.xml")
  public void testParseCompensationHandlerOfMiActivity() {
    ActivityImpl miActivity = findActivityInDeployedProcessDefinition("undoBookHotel");
    ScopeImpl flowScope = miActivity.getFlowScope();

    assertEquals(ActivityTypes.MULTI_INSTANCE_BODY, flowScope.getProperty(BpmnParse.PROPERTYNAME_TYPE));
    assertEquals("bookHotel" + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX, ((ActivityImpl) flowScope).getActivityId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.compensationMiSubprocess.bpmn20.xml")
  public void testParseCompensationHandlerOfMiSubprocess() {
    ActivityImpl miActivity = findActivityInDeployedProcessDefinition("undoBookHotel");
    ScopeImpl flowScope = miActivity.getFlowScope();

    assertEquals(ActivityTypes.MULTI_INSTANCE_BODY, flowScope.getProperty(BpmnParse.PROPERTYNAME_TYPE));
    assertEquals("scope" + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX, ((ActivityImpl) flowScope).getActivityId());
  }

  @Deployment
  public void testParseSignalStartEvent(){
    ActivityImpl signalStartActivity = findActivityInDeployedProcessDefinition("start");

    assertEquals(ActivityTypes.START_EVENT_SIGNAL, signalStartActivity.getProperty("type"));
    assertEquals(NoneStartEventActivityBehavior.class, signalStartActivity.getActivityBehavior().getClass());
  }

  @Deployment
  public void testParseEscalationBoundaryEvent() {
    ActivityImpl escalationBoundaryEvent = findActivityInDeployedProcessDefinition("escalationBoundaryEvent");

    assertEquals(ActivityTypes.BOUNDARY_ESCALATION, escalationBoundaryEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(BoundaryEventActivityBehavior.class, escalationBoundaryEvent.getActivityBehavior().getClass());
  }

  @Deployment
  public void testParseEscalationIntermediateThrowingEvent() {
    ActivityImpl escalationThrowingEvent = findActivityInDeployedProcessDefinition("escalationThrowingEvent");

    assertEquals(ActivityTypes.INTERMEDIATE_EVENT_ESCALATION_THROW, escalationThrowingEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(ThrowEscalationEventActivityBehavior.class, escalationThrowingEvent.getActivityBehavior().getClass());
  }

  @Deployment
  public void testParseEscalationEndEvent() {
    ActivityImpl escalationEndEvent = findActivityInDeployedProcessDefinition("escalationEndEvent");

    assertEquals(ActivityTypes.END_EVENT_ESCALATION, escalationEndEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(ThrowEscalationEventActivityBehavior.class, escalationEndEvent.getActivityBehavior().getClass());
  }

  @Deployment
  public void testParseEscalationStartEvent() {
    ActivityImpl escalationStartEvent = findActivityInDeployedProcessDefinition("escalationStartEvent");

    assertEquals(ActivityTypes.START_EVENT_ESCALATION, escalationStartEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(EventSubProcessStartEventActivityBehavior.class, escalationStartEvent.getActivityBehavior().getClass());
  }


  public void parseInvalidConditionalEvent(String processDefinitionResource) {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), processDefinitionResource);
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition could be parsed, conditional event definition contains no condition.");
    } catch (ProcessEngineException e) {
      assertTextPresent("Conditional event must contain an expression for evaluation.", e.getMessage());
    }
  }

  public void testParseInvalidConditionalBoundaryEvent() {
    parseInvalidConditionalEvent("testParseInvalidConditionalBoundaryEvent");
  }

  @Deployment
  public void testParseConditionalBoundaryEvent() {
    ActivityImpl conditionalBoundaryEvent = findActivityInDeployedProcessDefinition("conditionalBoundaryEvent");

    assertEquals(ActivityTypes.BOUNDARY_CONDITIONAL, conditionalBoundaryEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(BoundaryConditionalEventActivityBehavior.class, conditionalBoundaryEvent.getActivityBehavior().getClass());
  }

  @Deployment
  public void testParseAsyncBoundaryEvent() {
    ActivityImpl conditionalBoundaryEvent1 = findActivityInDeployedProcessDefinition("conditionalBoundaryEvent1");
    ActivityImpl conditionalBoundaryEvent2 = findActivityInDeployedProcessDefinition("conditionalBoundaryEvent2");

    assertTrue(conditionalBoundaryEvent1.isAsyncAfter());
    assertTrue(conditionalBoundaryEvent1.isAsyncBefore());

    assertFalse(conditionalBoundaryEvent2.isAsyncAfter());
    assertFalse(conditionalBoundaryEvent2.isAsyncBefore());
  }

  public void testParseInvalidIntermediateConditionalEvent() {
    parseInvalidConditionalEvent("testParseInvalidIntermediateConditionalEvent");
  }

  @Deployment
  public void testParseIntermediateConditionalEvent() {
    ActivityImpl intermediateConditionalEvent = findActivityInDeployedProcessDefinition("intermediateConditionalEvent");

    assertEquals(ActivityTypes.INTERMEDIATE_EVENT_CONDITIONAL, intermediateConditionalEvent.getProperties().get(BpmnProperties.TYPE));
    assertEquals(IntermediateConditionalEventBehavior.class, intermediateConditionalEvent.getActivityBehavior().getClass());
  }

  public void testParseInvalidEventSubprocessConditionalStartEvent() {
    parseInvalidConditionalEvent("testParseInvalidEventSubprocessConditionalStartEvent");
  }

  @Deployment
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

  public void testNoCamundaInSourceThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaInSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:in extension element should contain source!");
    } catch (ProcessEngineException e) {
      assertTextPresent("Missing parameter 'source' or 'sourceExpression' when passing variables", e.getMessage());
    }
  }

  public void testNoCamundaInSourceShouldWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaInSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
      repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
    }
  }

  public void testEmptyCamundaInSourceThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaInSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:in extension element should contain source!");
    } catch (ProcessEngineException e) {
      assertTextPresent("Empty attribute 'source' when passing variables", e.getMessage());
    }
  }

  public void testEmptyCamundaInSourceWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaInSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
      repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
    }
  }

  public void testNoCamundaInTargetThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaInTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:in extension element should contain target!");
    } catch (ProcessEngineException e) {
      assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
    }
  }

  public void testNoCamundaInTargetWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaInTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:in extension element should contain target!");
    } catch (ProcessEngineException e) {
      assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
    }
  }

  public void testEmptyCamundaInTargetThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaInTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:in extension element should contain target!");
    } catch (ProcessEngineException e) {
      assertTextPresent("Empty attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
    }
  }

  public void testEmptyCamundaInTargetWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaInTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
      repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
    }
  }

  public void testNoCamundaOutSourceThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaOutSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:out extension element should contain source!");
    } catch (ProcessEngineException e) {
      assertTextPresent("Missing parameter 'source' or 'sourceExpression' when passing variables", e.getMessage());
    }
  }

  public void testNoCamundaOutSourceWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaOutSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
      repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
    }
  }

  public void testEmptyCamundaOutSourceThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaOutSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:out extension element should contain source!");
    } catch (ProcessEngineException e) {
      assertTextPresent("Empty attribute 'source' when passing variables", e.getMessage());
    }
  }

  public void testEmptyCamundaOutSourceWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaOutSourceThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
      repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
    }
  }

  public void testNoCamundaOutTargetThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaOutTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:out extension element should contain target!");
    } catch (ProcessEngineException e) {
      assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
    }
  }

  public void testNoCamundaOutTargetWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testNoCamundaOutTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:out extension element should contain target!");
    } catch (ProcessEngineException e) {
      assertTextPresent("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
    }
  }

  public void testEmptyCamundaOutTargetThrowsError() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaOutTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Process camunda:out extension element should contain target!");
    } catch (ProcessEngineException e) {
      assertTextPresent("Empty attribute 'target' when attribute 'source' or 'sourceExpression' is set", e.getMessage());
    }
  }

  public void testEmptyCamundaOutTargetWithoutValidation() {
    try {
      processEngineConfiguration.setDisableStrictCallActivityValidation(true);

      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testEmptyCamundaOutTargetThrowsError");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    } finally {
      processEngineConfiguration.setDisableStrictCallActivityValidation(false);
      repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
    }
  }

  @Deployment
  public void testParseProcessDefinitionTtl() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertNotNull(processDefinitions);
    assertEquals(1, processDefinitions.size());

    Integer timeToLive = processDefinitions.get(0).getHistoryTimeToLive();
    assertNotNull(timeToLive);
    assertEquals(5, timeToLive.intValue());
  }

  @Deployment
  public void testParseProcessDefinitionStringTtl() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertNotNull(processDefinitions);
    assertEquals(1, processDefinitions.size());

    Integer timeToLive = processDefinitions.get(0).getHistoryTimeToLive();
    assertNotNull(timeToLive);
    assertEquals(5, timeToLive.intValue());
  }

  public void testParseProcessDefinitionMalformedStringTtl() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionMalformedStringTtl");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition historyTimeToLive value can not be parsed.");
    } catch (ProcessEngineException e) {
      assertTextPresent("Cannot parse historyTimeToLive", e.getMessage());
    }
  }

  @Deployment
  public void testParseProcessDefinitionEmptyTtl() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertNotNull(processDefinitions);
    assertEquals(1, processDefinitions.size());

    Integer timeToLive = processDefinitions.get(0).getHistoryTimeToLive();
    assertNull(timeToLive);
  }

  @Deployment
  public void testParseProcessDefinitionWithoutTtl() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertNotNull(processDefinitions);
    assertEquals(1, processDefinitions.size());

    Integer timeToLive = processDefinitions.get(0).getHistoryTimeToLive();
    assertNull(timeToLive);
  }

  public void testParseProcessDefinitionInvalidTtl() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionInvalidTtl");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition historyTimeToLive value can not be parsed.");
    } catch (ProcessEngineException e) {
      assertTextPresent("Cannot parse historyTimeToLive", e.getMessage());
    }
  }

  public void testParseProcessDefinitionNegativTtl() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testParseProcessDefinitionNegativeTtl");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail("Exception expected: Process definition historyTimeToLive value can not be parsed.");
    } catch (ProcessEngineException e) {
      assertTextPresent("Cannot parse historyTimeToLive", e.getMessage());
    }
  }

}
