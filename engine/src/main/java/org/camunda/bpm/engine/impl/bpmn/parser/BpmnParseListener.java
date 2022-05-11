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
package org.camunda.bpm.engine.impl.bpmn.parser;

import java.util.List;

import org.camunda.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;

/**
 * Listener which can be registered within the engine to receive events during parsing (and
 * maybe influence it). Instead of implementing this interface you might consider to extend
 * the {@link AbstractBpmnParseListener}, which contains an empty implementation for all methods
 * and makes your implementation easier and more robust to future changes.
 *
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Joram Barrez
 */
public interface BpmnParseListener {

  void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition);
  void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity);
  void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity);
  void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity);
  void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity);
  void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity);
  void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity);
  void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity);
  void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity);
  void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity);
  void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity);
  void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity);
  void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity);
  void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity);
  void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity);
  void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity);
  void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity);
  void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition);
  void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity);
  void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity);
  void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity);
  void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions);
  void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity);
  void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity);
  void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity);
  void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity);
  void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity);
  void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity);
  void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity);
  void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity);
  void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity);
  void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity);
  void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl messageActivity);
  void parseBoundaryEscalationEventDefinition(Element escalationEventDefinition, boolean interrupting, ActivityImpl boundaryEventActivity);
  void parseBoundaryConditionalEventDefinition(Element element, boolean interrupting, ActivityImpl conditionalActivity);
  void parseIntermediateConditionalEventDefinition(Element conditionalEventDefinition, ActivityImpl conditionalActivity);
  void parseConditionalStartEventForEventSubprocess(Element element, ActivityImpl conditionalActivity, boolean interrupting);
  void parseIoMapping(Element extensionElements, ActivityImpl activity, IoMapping inputOutput);
}
