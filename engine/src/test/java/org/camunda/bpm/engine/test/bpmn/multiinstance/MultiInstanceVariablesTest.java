package org.camunda.bpm.engine.test.bpmn.multiinstance;

import static org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior.NUMBER_OF_INSTANCES;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.CallActivityBuilder;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOut;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Askar Akhmerov
 */
public class MultiInstanceVariablesTest {

  public static final String ALL = "all";
  public static final String SUB_PROCESS_ID = "testProcess";
  public static final String PROCESS_ID = "process";
  public static final String CALL_ACTIVITY = "callActivity";

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Test
  public void testMultiInstanceWithAllInOutMapping() {
    BpmnModelInstance modelInstance = getBpmnModelInstance();

    CallActivityBuilder callActivityBuilder = ((CallActivity) modelInstance.getModelElementById(CALL_ACTIVITY)).builder();

    addAllIn(modelInstance, callActivityBuilder);

    addAllOut(modelInstance, callActivityBuilder);

    BpmnModelInstance testProcess = getBpmnSubProcessModelInstance();

    deployAndStartProcess(modelInstance, testProcess);
    assertThat(engineRule.getRuntimeService().createExecutionQuery().processDefinitionKey(SUB_PROCESS_ID).list().size(),is(2));

    List<Task> tasks = engineRule.getTaskService().createTaskQuery().active().list();
    for (Task task : tasks) {
      engineRule.getTaskService().setVariable(task.getId(),NUMBER_OF_INSTANCES,"3");
      engineRule.getTaskService().complete(task.getId());
    }

    assertThat(engineRule.getRuntimeService().createExecutionQuery().processDefinitionKey(SUB_PROCESS_ID).list().size(),is(0));
    assertThat(engineRule.getRuntimeService().createExecutionQuery().activityId(CALL_ACTIVITY).list().size(),is(0));
  }

  protected void addAllOut(BpmnModelInstance modelInstance, CallActivityBuilder callActivityBuilder) {
    CamundaOut camundaOut = modelInstance.newInstance(CamundaOut.class);
    camundaOut.setCamundaVariables(ALL);
    callActivityBuilder.addExtensionElement(camundaOut);
  }

  protected void addAllIn(BpmnModelInstance modelInstance, CallActivityBuilder callActivityBuilder) {
    CamundaIn camundaIn = modelInstance.newInstance(CamundaIn.class);
    camundaIn.setCamundaVariables(ALL);
    callActivityBuilder.addExtensionElement(camundaIn);
  }

  protected void deployAndStartProcess(BpmnModelInstance modelInstance, BpmnModelInstance testProcess) {
    engineRule.manageDeployment(engineRule.getRepositoryService().createDeployment()
        .addModelInstance("process.bpmn", modelInstance).deploy());
    engineRule.manageDeployment(engineRule.getRepositoryService().createDeployment()
        .addModelInstance("testProcess.bpmn", testProcess).deploy());
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_ID);
  }

  protected BpmnModelInstance getBpmnModelInstance() {
    return Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .callActivity(CALL_ACTIVITY)
          .calledElement(SUB_PROCESS_ID)
          .multiInstance()
            .cardinality("2")
            .multiInstanceDone()
        .endEvent()
        .done();
  }

  protected BpmnModelInstance getBpmnSubProcessModelInstance() {
    return Bpmn.createExecutableProcess(SUB_PROCESS_ID)
        .startEvent()
        .userTask("userTask")
        .endEvent()
        .done();
  }

}
