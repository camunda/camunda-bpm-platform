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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.PropertyKey;
import org.camunda.bpm.engine.impl.core.model.PropertyListKey;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.ParseUtil;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.util.xml.Namespace;

public class DefaultFailedJobParseListener extends AbstractBpmnParseListener {

  protected static final String TYPE = "type";
  protected static final String START_TIMER_EVENT = "startTimerEvent";
  protected static final String BOUNDARY_TIMER = "boundaryTimer";
  protected static final String INTERMEDIATE_SIGNAL_THROW = "intermediateSignalThrow";
  protected static final String INTERMEDIATE_TIMER = "intermediateTimer";
  protected static final String SIGNAL_EVENT_DEFINITION = "signalEventDefinition";
  protected static final String MULTI_INSTANCE_LOOP_CHARACTERISTICS = "multiInstanceLoopCharacteristics";

  protected static final String EXTENSION_ELEMENTS = "extensionElements";
  protected static final String FAILED_JOB_RETRY_TIME_CYCLE = "failedJobRetryTimeCycle";

  /**
   * deprecated since 7.4, use camunda ns.
   */
  @Deprecated
  public static final Namespace FOX_ENGINE_NS = new Namespace("http://www.camunda.com/fox");

  public static final PropertyKey<FailedJobParseRetryConf> FAILED_JOB_CONFIGURATION = new PropertyKey<FailedJobParseRetryConf>("FAILED_JOB_CONFIGURATION");
  public static final PropertyListKey<String> FAILED_JOB_INTERVALS = new PropertyListKey<String>("FAILED_JOB_INTERVALS");

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
    String type = startEventActivity.getProperties().get(BpmnProperties.TYPE);
    if (type != null && type.equals(START_TIMER_EVENT)) {
      this.setFailedJobRetryTimeCycleValue(startEventElement, startEventActivity);
    }
  }

  @Override
  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
    String type = nestedActivity.getProperties().get(BpmnProperties.TYPE);
    if ((type != null && type.equals(BOUNDARY_TIMER)) || isAsync(nestedActivity)) {
      setFailedJobRetryTimeCycleValue(boundaryEventElement, nestedActivity);
    }
  }

  @Override
  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    String type = activity.getProperties().get(BpmnProperties.TYPE);
    if (type != null) {
      this.setFailedJobRetryTimeCycleValue(intermediateEventElement, activity);
    }
  }

  @Override
  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    String type = activity.getProperties().get(BpmnProperties.TYPE);
    if (type != null && type.equals(INTERMEDIATE_TIMER)) {
      this.setFailedJobRetryTimeCycleValue(intermediateEventElement, activity);
    }
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    parseActivity(scriptTaskElement, activity);
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    parseActivity(serviceTaskElement, activity);
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    parseActivity(businessRuleTaskElement, activity);
  }

  @Override
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    parseActivity(taskElement, activity);
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    parseActivity(userTaskElement, activity);
  }

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    parseActivity(callActivityElement, activity);
  }

  @Override
  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    parseActivity(receiveTaskElement, activity);
  }

  @Override
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    parseActivity(sendTaskElement, activity);
  }

  @Override
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    parseActivity(subProcessElement, activity);
  }

  @Override
  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
    parseActivity(transactionElement, activity);
  }

  protected boolean isAsync(ActivityImpl activity) {
    return activity.isAsyncBefore() || activity.isAsyncAfter();
  }

  protected void parseActivity(Element element, ActivityImpl activity) {

    if (isMultiInstance(activity)) {
      // in case of multi-instance, the extension elements is set according to the async attributes
      // the extension for multi-instance body is set on the element of the activity
      ActivityImpl miBody = activity.getParentFlowScopeActivity();
      if (isAsync(miBody)) {
        setFailedJobRetryTimeCycleValue(element, miBody);
      }
      // the extension for inner activity is set on the multiInstanceLoopCharacteristics element
      if (isAsync(activity)) {
        Element multiInstanceLoopCharacteristics = element.element(MULTI_INSTANCE_LOOP_CHARACTERISTICS);
        setFailedJobRetryTimeCycleValue(multiInstanceLoopCharacteristics, activity);
      }

    } else if (isAsync(activity)) {
      setFailedJobRetryTimeCycleValue(element, activity);
    }
  }

  protected void setFailedJobRetryTimeCycleValue(Element element, ActivityImpl activity) {
    Element extensionElements = element.element(EXTENSION_ELEMENTS);
    FailedJobParseRetryConf retryConf = null;
    if (extensionElements != null) {
      Element failedJobRetryTimeCycleElement = extensionElements.elementNS(FOX_ENGINE_NS, FAILED_JOB_RETRY_TIME_CYCLE);
      if (failedJobRetryTimeCycleElement == null) {
        // try to get it from the activiti namespace
        failedJobRetryTimeCycleElement = extensionElements.elementNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, FAILED_JOB_RETRY_TIME_CYCLE);
      }
      if (failedJobRetryTimeCycleElement != null) {
        String failedJobRetryTimeCycleValue = failedJobRetryTimeCycleElement.getText();
        ArrayList<String> parsedIntervalsList = ParseUtil.parseRetryIntervals(failedJobRetryTimeCycleValue);
        retryConf = processRetryConf(parsedIntervalsList, failedJobRetryTimeCycleValue);
      }
    }
    else {
      List<String> parsedIntervalsList = Context.getProcessEngineConfiguration().getParsedRetryIntervals();
      String failedJobRetryTimeCycle = Context.getProcessEngineConfiguration().getFailedJobRetryTimeCycle();
      retryConf = processRetryConf(parsedIntervalsList, failedJobRetryTimeCycle);
    }
    if (retryConf != null) {
      activity.getProperties().set(FAILED_JOB_CONFIGURATION, retryConf);
    }
  }

  private FailedJobParseRetryConf processRetryConf(List<String> parsedIntervalsList, String failedJobRetryTimeCycle) {
    FailedJobParseRetryConf retryConf = null;
    if (parsedIntervalsList != null && parsedIntervalsList.size() > 1) {
      retryConf = new FailedJobParseRetryConf(parsedIntervalsList);
    } else if (failedJobRetryTimeCycle != null) {
      ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
      Expression expression = expressionManager.createExpression(failedJobRetryTimeCycle);
      retryConf = new FailedJobParseRetryConf(expression);
    }
    return retryConf;
  }

  protected boolean isMultiInstance(ActivityImpl activity) {
    // #isMultiInstance() don't work since the property is not set yet
    ActivityImpl parent = activity.getParentFlowScopeActivity();
    return parent != null && parent.getActivityBehavior() instanceof MultiInstanceActivityBehavior;
  }

}
