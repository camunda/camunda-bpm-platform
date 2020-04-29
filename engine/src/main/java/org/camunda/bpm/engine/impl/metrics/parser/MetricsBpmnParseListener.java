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
package org.camunda.bpm.engine.impl.metrics.parser;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.runtime.Execution;

/**
 * @author Daniel Meyer
 *
 */
public class MetricsBpmnParseListener extends AbstractBpmnParseListener {

  public static MetricsExecutionListener ROOT_PROCESS_INSTANCE_START_COUNTER =
      new MetricsExecutionListener(Metrics.ROOT_PROCESS_INSTANCE_START,
                                   delegateExecution -> (delegateExecution.getId().equals(
                                           ((ExecutionEntity) delegateExecution)
                                               .getRootProcessInstanceId())));
  public static MetricsExecutionListener ACTIVITY_INSTANCE_START_COUNTER =
      new MetricsExecutionListener(Metrics.ACTIVTY_INSTANCE_START);
  public static MetricsExecutionListener ACTIVITY_INSTANCE_END_COUNTER =
      new MetricsExecutionListener(Metrics.ACTIVTY_INSTANCE_END);

  protected void addListeners(ActivityImpl activity) {
    activity.addBuiltInListener(ExecutionListener.EVENTNAME_START, ACTIVITY_INSTANCE_START_COUNTER);
    activity.addBuiltInListener(ExecutionListener.EVENTNAME_END, ACTIVITY_INSTANCE_END_COUNTER);
  }

  @Override
  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
    processDefinition.addBuiltInListener(ExecutionListener.EVENTNAME_START,
                                         ROOT_PROCESS_INSTANCE_START_COUNTER);
  }

  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl activity) {
    addListeners(activity);
  }

  public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
    addListeners(activity);
  }
}
