package org.camunda.bpm.engine.test.bpmn.event.compensate.helper;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.junit.After;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompensationConceptsTests extends PluggableProcessEngineTest {

    protected String deploymentId;

    protected CamundaInputParameter findInputParameterByName(BaseElement baseElement, String name) {
        Collection<CamundaInputParameter> camundaInputParameters = baseElement.getExtensionElements().getElementsQuery().filterByType(CamundaInputOutput.class).singleResult().getCamundaInputParameters();
        for (CamundaInputParameter camundaInputParameter : camundaInputParameters) {
            if (camundaInputParameter.getCamundaName().equals(name)) {
                return camundaInputParameter;
            }
        }
        throw new BpmnModelException("Unable to find camunda:inputParameter with name '" + name + "' for element with id '" + baseElement.getId() + "'");
    }

    @After
    public void clear() {
        Mocks.reset();

        if (deploymentId != null) {
            repositoryService.deleteDeployment(deploymentId, true);
            deploymentId = null;
        }
    }


    private void completeTasks(String taskName, int times) {
        List<org.camunda.bpm.engine.task.Task> tasks = taskService.createTaskQuery().taskName(taskName).list();

        assertTrue("Actual there are " + tasks.size() + " open tasks with name '" + taskName + "'. Expected at least " + times, times <= tasks.size());

        Iterator<org.camunda.bpm.engine.task.Task> taskIterator = tasks.iterator();
        for (int i = 0; i < times; i++) {
            Task task = taskIterator.next();
            taskService.complete(task.getId());
        }
    }

    private void completeTask(String taskName) {
        completeTasks(taskName, 1);
    }


    @Test
    public void test123123() {
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("foo").startEvent("start").userTask("userTask").camundaInputParameter("var", "Hello World${'!'}").endEvent("end").done();

        testRule.deploy(modelInstance);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("foo");

        VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().variableName("var").singleResult();

        // then
        assertEquals("Hello World!", variableInstance.getValue());
        UserTask serviceTask = modelInstance.getModelElementById("userTask");

        CamundaInputParameter inputParameter = findInputParameterByName(serviceTask, "var");
        Assertions.assertThat(inputParameter.getCamundaName()).isEqualTo("var");


    }

    @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensationConceptsTest.sandroTest.bpmn20.xml")
    @Test
    public void sandroTest() {
        String processInstanceId = runtimeService.startProcessInstanceByKey("bookingProcess").getId();

        completeTask("Book Flight");
        completeTask("Book Hotel");
        completeTask("Book Car");
        completeTask("Pay Booking");

        testRule.assertProcessEnded(processInstanceId);

        List<HistoricActivityInstance> historicActivityInstance = historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().list();
        assertEquals(6, historicActivityInstance.size());

        assertEquals("startEvent", historicActivityInstance.get(0).getActivityId());
        assertEquals("bookFlight", historicActivityInstance.get(1).getActivityId());
        assertEquals("bookHotel", historicActivityInstance.get(2).getActivityId());
        assertEquals("bookCar", historicActivityInstance.get(3).getActivityId());
        assertEquals("payBooking", historicActivityInstance.get(4).getActivityId());
        assertEquals("endEvent", historicActivityInstance.get(5).getActivityId());

        processInstanceId = runtimeService.startProcessInstanceByKey("bookingProcess").getId();
        completeTask("Book Flight");
        completeTask("Book Hotel");
        completeTask("Book Car");

        Task task = taskService.createTaskQuery().taskName("Pay Booking").singleResult();
        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getName()).isEqualTo("Pay Booking");
        taskService.handleBpmnError(task.getId(), "errorCode");
        historicActivityInstance = historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().list();
        assertEquals(16, historicActivityInstance.size());

        assertEquals("startEvent", historicActivityInstance.get(6).getActivityId());
        assertEquals("bookFlight", historicActivityInstance.get(7).getActivityId());
        assertEquals("bookHotel", historicActivityInstance.get(8).getActivityId());
        assertEquals("bookCar", historicActivityInstance.get(9).getActivityId());
        assertEquals("payBooking", historicActivityInstance.get(10).getActivityId());

        // following have same start times, leading to flaky test if position is tested
        // assertEquals("errorEvent", historicActivityInstance.get(11).getActivityId());
        //assertEquals("compensationThrowEndEvent", historicActivityInstance.get(12).getActivityId());

        assertEquals("cancelCar", historicActivityInstance.get(13).getActivityId());
        assertEquals("cancelHotel", historicActivityInstance.get(14).getActivityId());
        assertEquals("cancelFlight", historicActivityInstance.get(15).getActivityId());
        testRule.assertProcessNotEnded(processInstanceId);

        completeTask("Cancel Flight");
        completeTask("Cancel Hotel");
        completeTask("Cancel Car");
        testRule.assertProcessEnded(processInstanceId);
    }

/*
    @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
    @Test
    public void testDeleteInstanceWithEventScopeExecution()
    {
        // given
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("foo")
                .startEvent("start")
                .userTask("userTask")
                .endEvent("end")
                .done();

        testRule.deploy(modelInstance);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("foo");

        // then
        List<HistoricActivityInstance> historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
                .orderByActivityId().asc().list();
        assertEquals(2, historicActivityInstance.size());

        assertEquals("start", historicActivityInstance.get(0).getActivityId());
        assertEquals("userTask", historicActivityInstance.get(1).getActivityId());

        // then
        HistoricActivityInstance userTask = historyService.createHistoricActivityInstanceQuery()
                .activityId("end")
                .singleResult();
        assertEquals("start", historicActivityInstance.get(0).getActivityId());

    }

    @Test
    @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
    public void shouldResolveMethodExpressionWithTwoNullParameter() {
        // given
        BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
                .startEvent()
                .exclusiveGateway()
                .condition("true", "${myBean.myMethod(execution.getVariable('v'), "
                        + "execution.getVariable('w'), execution.getVariable('x'), "
                        + "execution.getVariable('y'), execution.getVariable('z'))}")
                .userTask("userTask")
                .moveToLastGateway()
                .condition("false", "${false}")
                .endEvent()
                .done();

        deploymentId = repositoryService.createDeployment()
                .addModelInstance("testProcess.bpmn", process)
                .deploy()
                .getId();

        Mocks.register("myBean", new ExpressionManagerTest.MyBean());


        // when
        runtimeService.startProcessInstanceByKey("testProcess");


        // then
        HistoricActivityInstance userTask = historyService.createHistoricActivityInstanceQuery()
                .activityId("userTask")
                .singleResult();

        Assertions.assertThat(userTask).isNotNull();
    }

    @Test
    public void testCompositeExpressionForInputValue() {

        // given
        BpmnModelInstance instance = Bpmn.createExecutableProcess("Process")
                .startEvent()
                .receiveTask()
                .camundaInputParameter("var", "Hello World${'!'}")
                .endEvent("end")
                .done();

        testRule.deploy(instance);
        runtimeService.startProcessInstanceByKey("Process");

        // when
        VariableInstance variableInstance = runtimeService
                .createVariableInstanceQuery()
                .variableName("var")
                .singleResult();

        // then
        assertEquals("Hello World!", variableInstance.getValue());
    }

    @Test
    public void testCompensationTask() {
        BpmnModelInstance modelInstance = Bpmn.createProcess()
                .startEvent()
                .userTask("task")
                .boundaryEvent("boundary")
                .compensateEventDefinition().compensateEventDefinitionDone()
                .compensationStart()
                .userTask("compensate").name("compensate")
                .compensationDone()
                .endEvent("theend")
                .done();

        // Checking Association
        Collection<Association> associations = modelInstance.getModelElementsByType(Association.class);
        Assertions.assertThat(associations).hasSize(1);
        Association association = associations.iterator().next();
        Assertions.assertThat(association.getSource().getId()).isEqualTo("boundary");
        Assertions.assertThat(association.getTarget().getId()).isEqualTo("compensate");
        Assertions.assertThat(association.getAssociationDirection()).isEqualTo(AssociationDirection.One);

        // Checking Sequence flow
        UserTask task = modelInstance.getModelElementById("task");
        Collection<SequenceFlow> outgoing = task.getOutgoing();
        Assertions.assertThat(outgoing).hasSize(1);
        SequenceFlow flow = outgoing.iterator().next();
        Assertions.assertThat(flow.getSource().getId()).isEqualTo("task");
        Assertions.assertThat(flow.getTarget().getId()).isEqualTo("theend");

    }*/
}
