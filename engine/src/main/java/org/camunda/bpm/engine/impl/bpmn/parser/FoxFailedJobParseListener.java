package org.camunda.bpm.engine.impl.bpmn.parser;

import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

public class FoxFailedJobParseListener extends AbstractBpmnParseListener {

  private static final String TYPE = "type";
  private static final String START_TIMER_EVENT = "startTimerEvent";
  private static final String BOUNDARY_TIMER = "boundaryTimer";
  private static final String INTERMEDIATE_SIGNAL_THROW = "intermediateSignalThrow";
  private static final String INTERMEDIATE_TIMER = "intermediateTimer";

  private static final String SIGNAL_EVENT_DEFINITION = "signalEventDefinition";
  private static final String EXTENSION_ELEMENTS = "extensionElements";
  private static final String FAILED_JOB_RETRY_TIME_CYCLE = "failedJobRetryTimeCycle";
  public static final String FOX_ENGINE_NS = "http://www.camunda.com/fox";
  public static final String FOX_FAILED_JOB_CONFIGURATION = "FOX_FAILED_JOB_CONFIGURATION";
  
  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
    String type = (String) startEventActivity.getProperty(TYPE);
    if (type != null && type.equals(START_TIMER_EVENT)){
      this.setFailedJobRetryTimeCycleValue(startEventElement, startEventActivity);
    }
  }
  
  @Override
  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
    String type = (String) nestedActivity.getProperty(TYPE);
    if (type != null && type.equals(BOUNDARY_TIMER)) {
      this.setFailedJobRetryTimeCycleValue(boundaryEventElement, nestedActivity);
    }
  }

  @Override
  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
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
  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    String type = (String) activity.getProperty(TYPE);
    if (type != null && type.equals(INTERMEDIATE_TIMER)) {
      this.setFailedJobRetryTimeCycleValue(intermediateEventElement, activity);
    }
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    if (activity.isAsync()) {
      this.setFailedJobRetryTimeCycleValue(scriptTaskElement, activity);
    }
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    if (activity.isAsync()) {
      this.setFailedJobRetryTimeCycleValue(serviceTaskElement, activity);
    }
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    if (activity.isAsync()) {
      this.setFailedJobRetryTimeCycleValue(businessRuleTaskElement, activity);
    }
  }

  @Override
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    if (activity.isAsync()) {
      this.setFailedJobRetryTimeCycleValue(taskElement, activity);
    }
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    if (activity.isAsync()) {
      this.setFailedJobRetryTimeCycleValue(userTaskElement, activity);
    }
  }

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    if (activity.isAsync()) {
      this.setFailedJobRetryTimeCycleValue(callActivityElement, activity);
    }
  }

  @Override
  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    if (activity.isAsync()) {
      this.setFailedJobRetryTimeCycleValue(receiveTaskElement, activity);
    }
  }

  @Override
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    if (activity.isAsync()) {
      this.setFailedJobRetryTimeCycleValue(sendTaskElement, activity);
    }
  }

  @Override
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    if (activity.isAsync()) {
      this.setFailedJobRetryTimeCycleValue(subProcessElement, activity);
    }
  }

  @Override
  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
    if (activity.isAsync()) {
      this.setFailedJobRetryTimeCycleValue(transactionElement, activity);
    }
  }

  private void setFailedJobRetryTimeCycleValue(Element element, ActivityImpl activity) {
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
