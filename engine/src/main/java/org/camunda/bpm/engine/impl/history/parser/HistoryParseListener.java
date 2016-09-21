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

package org.camunda.bpm.engine.impl.history.parser;

import java.util.List;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmEvent;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;

/**
 * <p>This class is responsible for wiring history as execution listeners into process execution.
 *
 * <p>NOTE: the role of this class has changed since 7.0: in order to customize history behavior it is
 * usually not necessary to override this class but rather the {@link HistoryEventProducer} for
 * customizing data acquisition and {@link HistoryEventHandler} for customizing the persistence behavior
 * or if you need a history event stream.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 * @author Bernd Ruecker (camunda)
 * @author Christian Lipphardt (camunda)
 *
 * @author Daniel Meyer
 */
public class HistoryParseListener implements BpmnParseListener {

  // Cached listeners
  // listeners can be reused for a given process engine instance but cannot be cached in static fields since
  // different process engine instances on the same Classloader may have different HistoryEventProducer
  // configurations wired
  protected ExecutionListener PROCESS_INSTANCE_START_LISTENER;
  protected ExecutionListener PROCESS_INSTANCE_END_LISTENER;

  protected ExecutionListener ACTIVITY_INSTANCE_START_LISTENER;
  protected ExecutionListener ACTIVITY_INSTANCE_END_LISTENER;

  protected TaskListener USER_TASK_ASSIGNMENT_HANDLER;
  protected TaskListener USER_TASK_ID_HANDLER;

  // The history level set in the process engine configuration
  protected HistoryLevel historyLevel;

  public HistoryParseListener(HistoryLevel historyLevel, HistoryEventProducer historyEventProducer) {
    this.historyLevel = historyLevel;
    initExecutionListeners(historyEventProducer, historyLevel);
  }

  protected void initExecutionListeners(HistoryEventProducer historyEventProducer, HistoryLevel historyLevel) {
    PROCESS_INSTANCE_START_LISTENER = new ProcessInstanceStartListener(historyEventProducer, historyLevel);
    PROCESS_INSTANCE_END_LISTENER = new ProcessInstanceEndListener(historyEventProducer, historyLevel);

    ACTIVITY_INSTANCE_START_LISTENER = new ActivityInstanceStartListener(historyEventProducer, historyLevel);
    ACTIVITY_INSTANCE_END_LISTENER = new ActivityInstanceEndListener(historyEventProducer, historyLevel);

    USER_TASK_ASSIGNMENT_HANDLER = new ActivityInstanceUpdateListener(historyEventProducer, historyLevel);
    USER_TASK_ID_HANDLER = USER_TASK_ASSIGNMENT_HANDLER;
  }

  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.PROCESS_INSTANCE_END, null)) {
      processDefinition.addBuiltInListener(PvmEvent.EVENTNAME_END, PROCESS_INSTANCE_END_LISTENER);
    }
  }

  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);

    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.TASK_INSTANCE_CREATE, null)) {
      TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition();
      taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, USER_TASK_ASSIGNMENT_HANDLER);
      taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_CREATE, USER_TASK_ID_HANDLER);
    }
  }

  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
  }

  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
  }

  public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
  }

  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
  }

  public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
  }

  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
  }

  public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
  }

  public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseMultiInstanceLoopCharacteristics(Element activityElement,
          Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity) {
  }

  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {
  }

  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    // do not write history for link events
    if(!activity.getProperty("type").equals("intermediateLinkCatch")) {
      addActivityHandlers(activity);
    }
  }

  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity) {
  }

  public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl messageActivity) {
  }

  public void parseBoundaryEscalationEventDefinition(Element escalationEventDefinition, boolean interrupting, ActivityImpl boundaryEventActivity) {
  }

  public void parseBoundaryConditionalEventDefinition(Element element, boolean interrupting, ActivityImpl conditionalActivity) {
  }

  public void parseIntermediateConditionalEventDefinition(Element conditionalEventDefinition, ActivityImpl conditionalActivity) {
  }

  public void parseConditionalStartEventForEventSubprocess(Element element, ActivityImpl conditionalActivity, boolean interrupting) {
  }

  // helper methods ///////////////////////////////////////////////////////////

  protected void addActivityHandlers(ActivityImpl activity) {
    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.ACTIVITY_INSTANCE_START, null)) {
      activity.addBuiltInListener(PvmEvent.EVENTNAME_START, ACTIVITY_INSTANCE_START_LISTENER, 0);
    }
    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.ACTIVITY_INSTANCE_END, null)) {
      activity.addBuiltInListener(PvmEvent.EVENTNAME_END, ACTIVITY_INSTANCE_END_LISTENER);
    }
  }

}
