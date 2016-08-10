package org.camunda.bpm.engine.test.bpmn.async;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.IntermediateThrowEventImpl;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFailedJobRetryTimeCycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

/**
 * @author Askar Akhmerov
 */
public class RetryCmdDeployment {

  public static final String FAILING_EVENT = "failingEvent";
  public static final String PROCESS_ID = "failedIntermediateThrowingEventAsync";
  private static final String SCHEDULE = "R5/PT5M";
  private static final String PROCESS_ID_2 = "failingSignalProcess";
  public static final String MESSAGE = "start";
  private BpmnModelInstance[] bpmnModelInstances;

  public static RetryCmdDeployment deployment() {
    return new RetryCmdDeployment();
  }

  public static BpmnModelInstance prepareSignalEventProcess() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .signal(MESSAGE)
        .endEvent()
        .done();
    return withRetryCycle(modelInstance,FAILING_EVENT);
  }


  public static BpmnModelInstance prepareSignalFailure() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID_2)
        .startEvent()
            .signal(MESSAGE)
          .serviceTask()
            .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
    return modelInstance;
  }

  private static BpmnModelInstance withRetryCycle(BpmnModelInstance modelInstance,String activityId) {
    CamundaFailedJobRetryTimeCycle result = modelInstance.newInstance(CamundaFailedJobRetryTimeCycle.class);
    result.setTextContent(SCHEDULE);
    ((IntermediateThrowEventImpl)modelInstance.getModelElementById(activityId)).builder().addExtensionElement(result);
    return modelInstance;
  }

  public static BpmnModelInstance prepareMessageEventProcess() {
    return withRetryCycle(Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
              .message(MESSAGE)
            .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, SendMessageDelegate.class.getName())
        .endEvent()
        .done(),FAILING_EVENT);
  }

  public static BpmnModelInstance prepareMessageFailure() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID_2)
        .startEvent()
          .message(MESSAGE)
          .serviceTask()
            .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
    return modelInstance;
  }

  public static BpmnModelInstance prepareEscalationEventProcess() {
    return modify(withRetryCycle(Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .escalation(MESSAGE)
        .endEvent()
        .done(),FAILING_EVENT))
        .addSubProcessTo(PROCESS_ID)
          .triggerByEvent()
          .id(PROCESS_ID_2)
          .embeddedSubProcess()
            .startEvent()
              .escalation(MESSAGE)
              .serviceTask()
                .camundaClass(FailingDelegate.class.getName())
            .endEvent()
        .done();
  }


  public static BpmnModelInstance prepareCompensationEventProcess() {
    return modify(withRetryCycle(Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .subProcess("subProcess")
            .embeddedSubProcess()
              .startEvent()
              .endEvent()
          .subProcessDone()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .compensateEventDefinition()
            .compensateEventDefinitionDone()
        .endEvent()
        .done(),FAILING_EVENT))
        .addSubProcessTo("subProcess")
        .id(PROCESS_ID_2)
        .triggerByEvent()
        .embeddedSubProcess()
          .startEvent()
            .compensateEventDefinition()
            .compensateEventDefinitionDone()
              .serviceTask()
                .camundaClass(FailingDelegate.class.getName())
          .endEvent()
        .done();
  }


  public RetryCmdDeployment withEventProcess(BpmnModelInstance... bpmnModelInstances) {
    this.bpmnModelInstances = bpmnModelInstances;
    return this;
  }

  public static Collection<RetryCmdDeployment[]> asParameters(RetryCmdDeployment... deployments) {
    List<RetryCmdDeployment[]> deploymentList = new ArrayList<RetryCmdDeployment[]>();
    for (RetryCmdDeployment deployment : deployments) {
      deploymentList.add(new RetryCmdDeployment[]{ deployment });
    }

    return deploymentList;
  }

  public BpmnModelInstance[] getBpmnModelInstances() {
    return bpmnModelInstances;
  }

  public void setBpmnModelInstances(BpmnModelInstance[] bpmnModelInstances) {
    this.bpmnModelInstances = bpmnModelInstances;
  }

  public static class SendMessageDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
      runtimeService.correlateMessage(MESSAGE);
    }
  }
}
