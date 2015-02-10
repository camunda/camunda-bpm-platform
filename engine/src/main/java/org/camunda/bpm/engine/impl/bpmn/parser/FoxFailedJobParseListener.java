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

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

public class FoxFailedJobParseListener extends AbstractBpmnParseListener {

  protected static final String TYPE = "type";
  protected static final String START_TIMER_EVENT = "startTimerEvent";
  protected static final String BOUNDARY_TIMER = "boundaryTimer";
  protected static final String INTERMEDIATE_SIGNAL_THROW = "intermediateSignalThrow";
  protected static final String INTERMEDIATE_TIMER = "intermediateTimer";

  protected static final String SIGNAL_EVENT_DEFINITION = "signalEventDefinition";
  protected static final String EXTENSION_ELEMENTS = "extensionElements";
  protected static final String FAILED_JOB_RETRY_TIME_CYCLE = "failedJobRetryTimeCycle";
  public static final String FOX_ENGINE_NS = "http://www.camunda.com/fox";
  public static final String FOX_FAILED_JOB_CONFIGURATION = "FOX_FAILED_JOB_CONFIGURATION";

  @Override
  public void parseStartEvent(Element startEventElement, PvmScope scope, ActivityImpl startEventActivity) {
    String type = (String) startEventActivity.getProperty(TYPE);
    if (type != null && type.equals(START_TIMER_EVENT)){
      this.setFailedJobRetryTimeCycleValue(startEventElement, startEventActivity);
    }
  }

  @Override
  public void parseBoundaryEvent(Element boundaryEventElement, PvmScope scopeElement, ActivityImpl nestedActivity) {
    String type = (String) nestedActivity.getProperty(TYPE);
    if (type != null && type.equals(BOUNDARY_TIMER)) {
      this.setFailedJobRetryTimeCycleValue(boundaryEventElement, nestedActivity);
    }
  }

  @Override
  public void parseIntermediateThrowEvent(Element intermediateEventElement, PvmScope scope, ActivityImpl activity) {
    String type = (String) activity.getProperty(TYPE);
    if (type != null && type.equals(INTERMEDIATE_SIGNAL_THROW)) {
      Element signalDefElement = intermediateEventElement.element(SIGNAL_EVENT_DEFINITION);
      boolean asynch = "true".equals(signalDefElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async", "false"));
      if (asynch) {
        this.setFailedJobRetryTimeCycleValue(intermediateEventElement, activity);
      }
    }
  }

  @Override
  public void parseIntermediateCatchEvent(Element intermediateEventElement, PvmScope scope, ActivityImpl activity) {
    String type = (String) activity.getProperty(TYPE);
    if (type != null && type.equals(INTERMEDIATE_TIMER)) {
      this.setFailedJobRetryTimeCycleValue(intermediateEventElement, activity);
    }
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, PvmScope scope, ActivityImpl activity) {
    if (isAsync(activity)) {
      this.setFailedJobRetryTimeCycleValue(scriptTaskElement, activity);
    }
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, PvmScope scope, ActivityImpl activity) {
    if (isAsync(activity)) {
      this.setFailedJobRetryTimeCycleValue(serviceTaskElement, activity);
    }
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, PvmScope scope, ActivityImpl activity) {
    if (isAsync(activity)) {
      this.setFailedJobRetryTimeCycleValue(businessRuleTaskElement, activity);
    }
  }

  @Override
  public void parseTask(Element taskElement, PvmScope scope, ActivityImpl activity) {
    if (isAsync(activity)) {
      this.setFailedJobRetryTimeCycleValue(taskElement, activity);
    }
  }

  @Override
  public void parseUserTask(Element userTaskElement, PvmScope scope, ActivityImpl activity) {
    if (isAsync(activity)) {
      this.setFailedJobRetryTimeCycleValue(userTaskElement, activity);
    }
  }

  @Override
  public void parseCallActivity(Element callActivityElement, PvmScope scope, ActivityImpl activity) {
    if (isAsync(activity)) {
      this.setFailedJobRetryTimeCycleValue(callActivityElement, activity);
    }
  }

  @Override
  public void parseReceiveTask(Element receiveTaskElement, PvmScope scope, ActivityImpl activity) {
    if (isAsync(activity)) {
      this.setFailedJobRetryTimeCycleValue(receiveTaskElement, activity);
    }
  }

  @Override
  public void parseSendTask(Element sendTaskElement, PvmScope scope, ActivityImpl activity) {
    if (isAsync(activity)) {
      this.setFailedJobRetryTimeCycleValue(sendTaskElement, activity);
    }
  }

  @Override
  public void parseSubProcess(Element subProcessElement, PvmScope scope, ActivityImpl activity) {
    if (isAsync(activity)) {
      this.setFailedJobRetryTimeCycleValue(subProcessElement, activity);
    }
  }

  @Override
  public void parseTransaction(Element transactionElement, PvmScope scope, ActivityImpl activity) {
    if (activity.isAsyncBefore()) {
      this.setFailedJobRetryTimeCycleValue(transactionElement, activity);
    }
  }

  protected boolean isAsync(PvmActivity activity) {
    return activity.isAsyncBefore() || activity.isAsyncAfter();
  }

  protected void setFailedJobRetryTimeCycleValue(Element element, ActivityImpl activity) {
    Element extensionElements = element.element(EXTENSION_ELEMENTS);
    if (extensionElements != null) {
      Element failedJobRetryTimeCycleElement = extensionElements.elementNS(FOX_ENGINE_NS, FAILED_JOB_RETRY_TIME_CYCLE);
      if (failedJobRetryTimeCycleElement == null) { // try to get it from the activiti namespace
        failedJobRetryTimeCycleElement = extensionElements.elementNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, FAILED_JOB_RETRY_TIME_CYCLE);
      }
      if (failedJobRetryTimeCycleElement != null) {
        String failedJobRetryTimeCycleValue = failedJobRetryTimeCycleElement.getText();
        activity.setProperty(FOX_FAILED_JOB_CONFIGURATION, failedJobRetryTimeCycleValue);
      }
    }
  }
}
