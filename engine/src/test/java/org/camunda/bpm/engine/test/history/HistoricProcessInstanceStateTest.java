package org.camunda.bpm.engine.test.history;

import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.TerminateEventDefinition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
public class HistoricProcessInstanceStateTest {

  public static final String TERMINATION = "termination";
  public static final String PROCESS_ID = "process1";
  public static final String REASON = "very important reason";

  public ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule processEngineTestRule = new ProcessEngineTestRule(processEngineRule);

  @Rule
  public RuleChain ruleChain = RuleChain
      .outerRule(processEngineTestRule)
      .around(processEngineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  /**
   *
   */
  @Test
  public void testTerminatedInternalWithGateway () {
    BpmnModelInstance instance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .parallelGateway()
          .endEvent()
        .moveToLastGateway()
          .endEvent(TERMINATION)
      .done();
    initEndEvent(instance,TERMINATION);
    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(instance);
    processEngineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState(),is(HistoricProcessInstance.COMPLETED));
  }

  @Test
  public void testCompletedOnEndEvent () {
    BpmnModelInstance instance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .endEvent()
        .done();
    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(instance);
    processEngineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState(),is(HistoricProcessInstance.COMPLETED));
  }


  @Test
  public void testHappyPathWithSuspension () {
    BpmnModelInstance instance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .userTask()
        .endEvent()
      .done();
    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = processEngineRule.getRuntimeService()
        .startProcessInstanceById(processDefinition.getId());
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState(),is(HistoricProcessInstance.ACTIVE));

    //suspend
    processEngineRule.getRuntimeService().updateProcessInstanceSuspensionState()
        .byProcessInstanceId(processInstance.getId()).suspend();

    entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState(),is(HistoricProcessInstance.SUSPENDED));

    //activate
    processEngineRule.getRuntimeService().updateProcessInstanceSuspensionState()
        .byProcessInstanceId(processInstance.getId()).activate();

    entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState(),is(HistoricProcessInstance.ACTIVE));

    //complete task
    processEngineRule.getTaskService().complete(
        processEngineRule.getTaskService().createTaskQuery().active().singleResult().getId());

    //make sure happy path ended
    entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState(),is(HistoricProcessInstance.COMPLETED));
  }

  @Test
  public void testCancellationState() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess(PROCESS_ID)
          .startEvent()
          .userTask()
          .endEvent()
        .done();
    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = processEngineRule.getRuntimeService()
        .startProcessInstanceById(processDefinition.getId());
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState(),is(HistoricProcessInstance.ACTIVE));

    //same call as in ProcessInstanceResourceImpl
    processEngineRule.getRuntimeService().deleteProcessInstance(processInstance.getId(), REASON,false,true);
    entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState(),is(HistoricProcessInstance.EXTERNALLY_TERMINATED));
  }

  @Test
  public void testSateOfScriptTaskProcessWithTransactionCommitAndException () {
    BpmnModelInstance instance = Bpmn.createExecutableProcess(PROCESS_ID)
          .startEvent()
          //add wait state
          .camundaAsyncAfter()
          .scriptTask()
          .scriptText("throw new RuntimeException()")
          .scriptFormat("groovy")
          .endEvent()
        .done();
    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(instance);

    try {
      processEngineRule.getRuntimeService()
          .startProcessInstanceById(processDefinition.getId());
    } catch (Exception e) {
      //expected
    }

    assertThat(processEngineRule.getRuntimeService().createProcessInstanceQuery().active().list().size(),is(1));
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState(),is(HistoricProcessInstance.ACTIVE));
  }

  @Test
  public void testErrorEndEvent() {
    BpmnModelInstance process1 = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .endEvent()
        .error("1")
        .done();

    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(process1);
    processEngineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState(),is(HistoricProcessInstance.COMPLETED));
  }

  @Test
  @Deployment (resources = {"org/camunda/bpm/engine/test/history/HistoricProcessInstanceStateTest.testWithCallActivity.bpmn"})
  public void testWithCallActivity() {
    processEngineRule.getRuntimeService().startProcessInstanceByKey("Process_1");
    assertThat(processEngineRule.getRuntimeService().createProcessInstanceQuery().active().list().size(),is(0));
    List<HistoricProcessInstance> entities = processEngineRule.getHistoryService().createHistoricProcessInstanceQuery()
        .finishedBefore(new Date()).orderByProcessInstanceStartTime().asc().list();
    assertThat(entities.size(),is(2));
    assertThat(entities.get(0).getState(),is(HistoricProcessInstance.COMPLETED));
    assertThat(entities.get(1).getState(),is(HistoricProcessInstance.INTERNALLY_TERMINATED));
  }

  private HistoricProcessInstance getHistoricProcessInstanceWithAssertion(ProcessDefinition processDefinition) {
    List<HistoricProcessInstance> entities = processEngineRule.getHistoryService().createHistoricProcessInstanceQuery()
        .processDefinitionId(processDefinition.getId()).list();
    assertThat(entities,is(notNullValue()));
    assertThat(entities.size(),is(1));
    return entities.get(0);
  }

  public static void initEndEvent(BpmnModelInstance modelInstance, String endEventId) {
    EndEvent endEvent = modelInstance.getModelElementById(endEventId);
    TerminateEventDefinition terminateDefinition = modelInstance.newInstance(TerminateEventDefinition.class);
    endEvent.addChildElement(terminateDefinition);
  }
}
