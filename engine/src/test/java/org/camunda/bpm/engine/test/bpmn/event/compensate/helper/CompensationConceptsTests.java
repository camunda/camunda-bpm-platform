package org.camunda.bpm.engine.test.bpmn.event.compensate.helper;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.junit.Assert.assertEquals;

public class CompensationConceptsTests extends PluggableProcessEngineTest {

/*    @Deployment
    @Test
    public void testCompensateSubprocess() {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

        assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));

        runtimeService.signal(processInstance.getId());
        testRule.assertProcessEnded(processInstance.getId());

    }*/

    @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.activityWithCompensationEndEvent.bpmn20.xml")
    @Test
    public void testActivityInstanceTreeForCompensationEndEvent(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

        ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
        assertThat(tree).hasStructure(
                describeActivityInstanceTree(processInstance.getProcessDefinitionId())
                        .activity("end")
                        .activity("undoBookHotel")
                        .done());
    }

    @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensationConceptsTest.sandroTest.bpmn20.xml")
    @Test
    public void sandroTest(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

        ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
        assertThat(tree).hasStructure(
                describeActivityInstanceTree(processInstance.getProcessDefinitionId())
                        .activity("end")
                        .activity("undoBookHotel")
                        .done());
    }


    @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
    @Test
    public void testDeleteInstanceWithEventScopeExecution()
    {
        // given
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("foo")
                .startEvent("start")
                .subProcess("subProcess")
                .embeddedSubProcess()
                .startEvent("subProcessStart")
                .endEvent("subProcessEnd")
                .subProcessDone()
                .userTask("userTask")
                .done();

        modelInstance = ModifiableBpmnModelInstance.modify(modelInstance)
                .addSubProcessTo("subProcess")
                .id("eventSubProcess")
                .triggerByEvent()
                .embeddedSubProcess()
                .startEvent()
                .compensateEventDefinition()
                .compensateEventDefinitionDone()
                .endEvent()
                .done();

        testRule.deploy(modelInstance);

        long dayInMillis = 1000 * 60 * 60 * 24;
        Date date1 = new Date(10 * dayInMillis);
        ClockUtil.setCurrentTime(date1);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("foo");

        // when
        Date date2 = new Date(date1.getTime() + dayInMillis);
        ClockUtil.setCurrentTime(date2);
        runtimeService.deleteProcessInstance(processInstance.getId(), null);

        // then
        List<HistoricActivityInstance> historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
                .orderByActivityId().asc().list();
        assertEquals(5, historicActivityInstance.size());

        assertEquals("start", historicActivityInstance.get(0).getActivityId());
        assertEquals(date1, historicActivityInstance.get(0).getEndTime());
        assertEquals("subProcess", historicActivityInstance.get(1).getActivityId());
        assertEquals(date1, historicActivityInstance.get(1).getEndTime());
        /*assertEquals("subProcessEnd", historicActivityInstance.get(2).getActivityId());
        assertEquals(date1, historicActivityInstance.get(2).getEndTime());
        assertEquals("subProcessStart", historicActivityInstance.get(3).getActivityId());
        assertEquals(date1, historicActivityInstance.get(3).getEndTime());
        assertEquals("userTask", historicActivityInstance.get(4).getActivityId());
        assertEquals(date2, historicActivityInstance.get(4).getEndTime());*/


    }
}
