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
package org.camunda.bpm.engine.cdi.impl.event;

import java.util.List;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;

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

  protected void addTaskUpdateListeners(TaskDefinition taskDefinition) {
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_UPDATE, new CdiEventListener());
  }

  protected void addTaskDeleteListeners(TaskDefinition taskDefinition) {
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_DELETE, new CdiEventListener());
  }

  @Override
  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
  }

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
    addStartEventListener(startEventActivity);
    addEndEventListener(startEventActivity);
  }

  @Override
  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
    UserTaskActivityBehavior activityBehavior = (UserTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = activityBehavior.getTaskDefinition();
    addTaskCreateListeners(taskDefinition);
    addTaskAssignmentListeners(taskDefinition);
    addTaskCompleteListeners(taskDefinition);
    addTaskUpdateListeners(taskDefinition);
    addTaskDeleteListeners(taskDefinition);
  }

  @Override
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
    addStartEventListener(timerActivity);
    addEndEventListener(timerActivity);
  }

  @Override
  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
  }

  @Override
  public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
    transition.addExecutionListener(new CdiEventListener());
  }

  @Override
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
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
  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
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
  public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {

  }

  @Override
  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  @Override
  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
  }

  @Override
  public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity) {
  }

  @Override
  public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl messageActivity) {
  }

  @Override
  public void parseBoundaryEscalationEventDefinition(Element escalationEventDefinition, boolean interrupting, ActivityImpl boundaryEventActivity) {
  }

  @Override
  public void parseBoundaryConditionalEventDefinition(Element element, boolean interrupting, ActivityImpl conditionalActivity) {
  }

  @Override
  public void parseIntermediateConditionalEventDefinition(Element conditionalEventDefinition, ActivityImpl conditionalActivity) {
  }

  @Override
  public void parseConditionalStartEventForEventSubprocess(Element element, ActivityImpl conditionalActivity, boolean interrupting) {
  }
}
