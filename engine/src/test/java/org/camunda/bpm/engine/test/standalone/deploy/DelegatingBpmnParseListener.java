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
package org.camunda.bpm.engine.test.standalone.deploy;

import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;

public class DelegatingBpmnParseListener implements BpmnParseListener {

  public static BpmnParseListener DELEGATE;

  @Override
  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
    DELEGATE.parseProcess(processElement, processDefinition);
  }

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope,
      ActivityImpl startEventActivity) {
    DELEGATE.parseStartEvent(startEventElement, scope, startEventActivity);
  }

  @Override
  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope,
      ActivityImpl activity) {
    DELEGATE.parseExclusiveGateway(exclusiveGwElement, scope, activity);
  }

  @Override
  public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope,
      ActivityImpl activity) {
    DELEGATE.parseInclusiveGateway(inclusiveGwElement, scope, activity);
  }

  @Override
  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope,
      ActivityImpl activity) {
    DELEGATE.parseParallelGateway(parallelGwElement, scope, activity);
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    DELEGATE.parseScriptTask(scriptTaskElement, scope, activity);
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    DELEGATE.parseServiceTask(serviceTaskElement, scope, activity);
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope,
      ActivityImpl activity) {
    DELEGATE.parseBusinessRuleTask(businessRuleTaskElement, scope, activity);
  }

  @Override
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    DELEGATE.parseTask(taskElement, scope, activity);
  }

  @Override
  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    DELEGATE.parseManualTask(manualTaskElement, scope, activity);
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    DELEGATE.parseUserTask(userTaskElement, scope, activity);
  }

  @Override
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    DELEGATE.parseEndEvent(endEventElement, scope, activity);
  }

  @Override
  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting,
      ActivityImpl timerActivity) {
    DELEGATE.parseBoundaryTimerEventDefinition(timerEventDefinition, interrupting, timerActivity);
  }

  @Override
  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting,
      ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
    DELEGATE.parseBoundaryErrorEventDefinition(errorEventDefinition, interrupting, activity, nestedErrorEventActivity);
  }

  @Override
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    DELEGATE.parseSubProcess(subProcessElement, scope, activity);
  }

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope,
      ActivityImpl activity) {
    DELEGATE.parseCallActivity(callActivityElement, scope, activity);
  }

  @Override
  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration,
      ActivityImpl activity) {
    DELEGATE.parseProperty(propertyElement, variableDeclaration, activity);
  }

  @Override
  public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement,
      TransitionImpl transition) {
    DELEGATE.parseSequenceFlow(sequenceFlowElement, scopeElement, transition);
  }

  @Override
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    DELEGATE.parseSendTask(sendTaskElement, scope, activity);
  }

  @Override
  public void parseMultiInstanceLoopCharacteristics(Element activityElement,
      Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
    DELEGATE.parseMultiInstanceLoopCharacteristics(activityElement, multiInstanceLoopCharacteristicsElement, activity);
  }

  @Override
  public void parseIntermediateTimerEventDefinition(Element timerEventDefinition,
      ActivityImpl timerActivity) {
    DELEGATE.parseIntermediateTimerEventDefinition(timerEventDefinition, timerActivity);
  }

  @Override
  public void parseRootElement(Element rootElement,
      List<ProcessDefinitionEntity> processDefinitions) {
    DELEGATE.parseRootElement(rootElement, processDefinitions);
  }

  @Override
  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    DELEGATE.parseReceiveTask(receiveTaskElement, scope, activity);
  }

  @Override
  public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition,
      ActivityImpl signalActivity) {
    DELEGATE.parseIntermediateSignalCatchEventDefinition(signalEventDefinition, signalActivity);
  }

  @Override
  public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition,
      ActivityImpl nestedActivity) {
    DELEGATE.parseIntermediateMessageCatchEventDefinition(messageEventDefinition, nestedActivity);
  }

  @Override
  public void parseBoundarySignalEventDefinition(Element signalEventDefinition,
      boolean interrupting, ActivityImpl signalActivity) {
    DELEGATE.parseBoundarySignalEventDefinition(signalEventDefinition, interrupting, signalActivity);
  }

  @Override
  public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope,
      ActivityImpl activity) {
    DELEGATE.parseEventBasedGateway(eventBasedGwElement, scope, activity);
  }

  @Override
  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
    DELEGATE.parseTransaction(transactionElement, scope, activity);
  }

  @Override
  public void parseCompensateEventDefinition(Element compensateEventDefinition,
      ActivityImpl compensationActivity) {
    DELEGATE.parseCompensateEventDefinition(compensateEventDefinition, compensationActivity);
  }

  @Override
  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope,
      ActivityImpl activity) {
    DELEGATE.parseIntermediateThrowEvent(intermediateEventElement, scope, activity);
  }

  @Override
  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope,
      ActivityImpl activity) {
    DELEGATE.parseIntermediateCatchEvent(intermediateEventElement, scope, activity);
  }

  @Override
  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement,
      ActivityImpl nestedActivity) {
    DELEGATE.parseBoundaryEvent(boundaryEventElement, scopeElement, nestedActivity);
  }

  @Override
  public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting,
      ActivityImpl messageActivity) {
    DELEGATE.parseBoundaryMessageEventDefinition(element, interrupting, messageActivity);
  }

  @Override
  public void parseBoundaryEscalationEventDefinition(Element escalationEventDefinition,
      boolean interrupting, ActivityImpl boundaryEventActivity) {
    DELEGATE.parseBoundaryEscalationEventDefinition(escalationEventDefinition, interrupting, boundaryEventActivity);
  }

  @Override
  public void parseBoundaryConditionalEventDefinition(Element element, boolean interrupting,
      ActivityImpl conditionalActivity) {
    DELEGATE.parseBoundaryConditionalEventDefinition(element, interrupting, conditionalActivity);
  }

  @Override
  public void parseIntermediateConditionalEventDefinition(Element conditionalEventDefinition,
      ActivityImpl conditionalActivity) {
    DELEGATE.parseIntermediateConditionalEventDefinition(conditionalEventDefinition, conditionalActivity);
  }

  @Override
  public void parseConditionalStartEventForEventSubprocess(Element element,
      ActivityImpl conditionalActivity, boolean interrupting) {
    DELEGATE.parseConditionalStartEventForEventSubprocess(element, conditionalActivity, interrupting);
  }

  @Override
  public void parseIoMapping(Element extensionElements, ActivityImpl activity, IoMapping inputOutput) {
    DELEGATE.parseIoMapping(extensionElements, activity, inputOutput);
  }
}
