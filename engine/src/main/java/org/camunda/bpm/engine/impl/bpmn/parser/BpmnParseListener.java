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

package org.camunda.bpm.engine.impl.bpmn.parser;

import java.util.List;

import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
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
  void parseStartEvent(Element startEventElement, PvmScope scope, ActivityImpl startEventActivity);
  void parseExclusiveGateway(Element exclusiveGwElement, PvmScope scope, ActivityImpl activity);
  void parseInclusiveGateway(Element inclusiveGwElement, PvmScope scope, ActivityImpl activity);
  void parseParallelGateway(Element parallelGwElement, PvmScope scope, ActivityImpl activity);
  void parseScriptTask(Element scriptTaskElement, PvmScope scope, ActivityImpl activity);
  void parseServiceTask(Element serviceTaskElement, PvmScope scope, ActivityImpl activity);
  void parseBusinessRuleTask(Element businessRuleTaskElement, PvmScope scope, ActivityImpl activity);
  void parseTask(Element taskElement, PvmScope scope, ActivityImpl activity);
  void parseManualTask(Element manualTaskElement, PvmScope scope, ActivityImpl activity);
  void parseUserTask(Element userTaskElement, PvmScope scope, ActivityImpl activity);
  void parseEndEvent(Element endEventElement, PvmScope scope, ActivityImpl activity);
  void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity);
  void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, PvmActivity nestedErrorEventActivity);
  void parseSubProcess(Element subProcessElement, PvmScope scope, ActivityImpl activity);
  void parseCallActivity(Element callActivityElement, PvmScope scope, ActivityImpl activity);
  void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, PvmActivity activity);
  void parseSequenceFlow(Element sequenceFlowElement, PvmScope scopeElement, TransitionImpl transition);
  void parseSendTask(Element sendTaskElement, PvmScope scope, ActivityImpl activity);
  void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity);
  void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity);
  void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions);
  void parseReceiveTask(Element receiveTaskElement, PvmScope scope, ActivityImpl activity);
  void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity);
  void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, PvmActivity nestedActivity);
  void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity);
  void parseEventBasedGateway(Element eventBasedGwElement, PvmScope scope, ActivityImpl activity);
  void parseTransaction(Element transactionElement, PvmScope scope, ActivityImpl activity);
  void parseCompensateEventDefinition(Element compensateEventDefinition, PvmActivity compensationActivity);
  void parseIntermediateThrowEvent(Element intermediateEventElement, PvmScope scope, ActivityImpl activity);
  void parseIntermediateCatchEvent(Element intermediateEventElement, PvmScope scope, ActivityImpl activity);
  void parseBoundaryEvent(Element boundaryEventElement, PvmScope scopeElement, ActivityImpl nestedActivity);
  void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, PvmActivity messageActivity);



}
