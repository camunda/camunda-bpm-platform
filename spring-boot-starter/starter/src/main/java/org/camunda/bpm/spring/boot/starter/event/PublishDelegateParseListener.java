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
package org.camunda.bpm.spring.boot.starter.event;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.spring.boot.starter.property.EventingProperty;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;

import static org.camunda.bpm.engine.delegate.ExecutionListener.*;
import static org.camunda.bpm.engine.delegate.TaskListener.*;

/**
 * Parse listener adding provided execution and task listeners.
 */
public class PublishDelegateParseListener extends AbstractBpmnParseListener {

  private static final List<String> TASK_EVENTS = Arrays.asList(
    EVENTNAME_COMPLETE,
    EVENTNAME_ASSIGNMENT,
    EVENTNAME_CREATE,
    EVENTNAME_DELETE,
    EVENTNAME_UPDATE);
  private static final List<String> EXECUTION_EVENTS = Arrays.asList(
    EVENTNAME_START,
    EVENTNAME_END);

  private final TaskListener taskListener;
  private final ExecutionListener executionListener;

  public PublishDelegateParseListener(final ApplicationEventPublisher publisher, final EventingProperty property) {

    if (property.isTask()) {
      this.taskListener = delegateTask -> {
        publisher.publishEvent(delegateTask);
        publisher.publishEvent(new TaskEvent(delegateTask));
      };
    } else {
      this.taskListener = null;
    }

    if (property.isExecution()) {
      this.executionListener = delegateExecution -> {
        publisher.publishEvent(delegateExecution);
        publisher.publishEvent(new ExecutionEvent(delegateExecution));
      };
    } else {
      this.executionListener = null;
    }
  }


  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addTaskListener(taskDefinition(activity));
    addExecutionListener(activity);
  }

  @Override
  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
    // DO NOT IMPLEMENT
    // we do not notify on entering a multi-instance activity, this will be done for every single execution inside that loop.
  }

  @Override
  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
    if (executionListener != null) {
      for (String event : EXECUTION_EVENTS) {
        processDefinition.addListener(event, executionListener);
      }
    }
  }

  @Override
  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
    addExecutionListener(transition);
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  @Override
  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
    addExecutionListener(activity);
  }

  private void addExecutionListener(final ActivityImpl activity) {
    if (executionListener != null) {
      for (String event : EXECUTION_EVENTS) {
        activity.addListener(event, executionListener);
      }
    }
  }

  private void addExecutionListener(final TransitionImpl transition) {
    if (executionListener != null) {
      transition.addListener(EVENTNAME_TAKE, executionListener);
    }
  }

  private void addTaskListener(TaskDefinition taskDefinition) {
    if (taskListener != null) {
      for (String event : TASK_EVENTS) {
        taskDefinition.addTaskListener(event, taskListener);
      }
    }
  }

  /**
   * Retrieves task definition.
   *
   * @param activity the taskActivity
   * @return taskDefinition for activity
   */
  private TaskDefinition taskDefinition(final ActivityImpl activity) {
    final UserTaskActivityBehavior activityBehavior = (UserTaskActivityBehavior) activity.getActivityBehavior();
    return activityBehavior.getTaskDefinition();
  }
}
