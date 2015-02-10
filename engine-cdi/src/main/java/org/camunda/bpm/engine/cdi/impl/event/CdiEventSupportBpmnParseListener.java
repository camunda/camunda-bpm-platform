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
package org.camunda.bpm.engine.cdi.impl.event;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;

import java.util.List;

/**
 * {@link BpmnParseListener} registering the {@link CdiEventListener} for
 * distributing execution events using the cdi event infrastructure
 *
 * @author Daniel Meyer
 */
public class CdiEventSupportBpmnParseListener implements BpmnParseListener {

  protected void addEndEventListener(ActivityImpl activity) {
    activity.addExecutionListener(ExecutionListener.EVENTNAME_END, new CdiEventListener());
  }

  protected void addStartEventListener(ActivityImpl activity) {
    activity.addExecutionListener(ExecutionListener.EVENTNAME_START, new CdiEventListener());
  }

  protected void addTaskCreateListeners(TaskDefinition taskDefinition) {
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, new CdiEventListener());
  }

  protected void addTaskAssignmentListeners(TaskDefinition taskDefinition) {
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, new CdiEventListener());
  }

  protected void addTaskCompleteListeners(TaskDefinition taskDefinition) {
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_COMPLETE, new CdiEventListener());
  }

  protected void addTaskDeleteListeners(TaskDefinition taskDefinition) {
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_DELETE, new CdiEventListener());
  }

  @Override
  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
  }

  @Override
  public void parseStartEvent(Element startEventElement, PvmScope scope, ActivityImpl startEventActivity) {
    addStartEventListener(startEventActivity);
    addEndEventListener(startEventActivity);
  }

  @Override
  public void parseExclusiveGateway(Element exclusiveGwElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseInclusiveGateway(Element inclusiveGwElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseParallelGateway(Element parallelGwElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseTask(Element taskElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseManualTask(Element manualTaskElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseUserTask(Element userTaskElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
    UserTaskActivityBehavior activityBehavior = (UserTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = activityBehavior.getTaskDefinition();
    addTaskCreateListeners(taskDefinition);
    addTaskAssignmentListeners(taskDefinition);
    addTaskCompleteListeners(taskDefinition);
    addTaskDeleteListeners(taskDefinition);
  }

  @Override
  public void parseEndEvent(Element endEventElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
    addStartEventListener(timerActivity);
    addEndEventListener(timerActivity);
  }

  @Override
  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, PvmActivity nestedErrorEventActivity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseSubProcess(Element subProcessElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseCallActivity(Element callActivityElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, PvmActivity activity) {
  }

  @Override
  public void parseSequenceFlow(Element sequenceFlowElement, PvmScope scopeElement, TransitionImpl transition) {
    transition.addExecutionListener(new CdiEventListener());
  }

  @Override
  public void parseSendTask(Element sendTaskElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
  }

  @Override
  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
  }

  @Override
  public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
    addStartEventListener(timerActivity);
    addEndEventListener(timerActivity);
  }

  @Override
  public void parseReceiveTask(Element receiveTaskElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity) {
    addStartEventListener(signalActivity);
    addEndEventListener(signalActivity);
  }

  @Override
  public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
    addStartEventListener(signalActivity);
    addEndEventListener(signalActivity);
  }

  @Override
  public void parseEventBasedGateway(Element eventBasedGwElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseTransaction(Element transactionElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseCompensateEventDefinition(Element compensateEventDefinition, PvmActivity compensationActivity) {

  }

  @Override
  public void parseIntermediateThrowEvent(Element intermediateEventElement, PvmScope scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseIntermediateCatchEvent(Element intermediateEventElement, PvmScope scope, ActivityImpl activity) {
  }

  @Override
  public void parseBoundaryEvent(Element boundaryEventElement, PvmScope scopeElement, ActivityImpl nestedActivity) {
  }

  @Override
  public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, PvmActivity nestedActivity) {
  }

  @Override
  public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, PvmActivity messageActivity) {
  }

}
