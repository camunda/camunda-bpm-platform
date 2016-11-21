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
package org.camunda.bpm.engine.test.api.runtime.migration.util;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance;
import org.camunda.bpm.engine.test.api.runtime.migration.util.SignalEventFactory.SignalTrigger;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class TimerEventFactory implements BpmnEventFactory {

  public static final String TIMER_DATE = "2016-02-11T12:13:14Z";

  @Override
  public MigratingBpmnEventTrigger addBoundaryEvent(ProcessEngine engine, BpmnModelInstance modelInstance, String activityId, String boundaryEventId) {
    ModifiableBpmnModelInstance.wrap(modelInstance)
        .activityBuilder(activityId)
        .boundaryEvent(boundaryEventId)
          .timerWithDate(TIMER_DATE)
        .done();

    TimerEventTrigger trigger = new TimerEventTrigger();
    trigger.engine = engine;
    trigger.activityId = boundaryEventId;
    trigger.handlerType = TimerExecuteNestedActivityJobHandler.TYPE;

    return trigger;
  }

  @Override
  public MigratingBpmnEventTrigger addEventSubProcess(ProcessEngine engine, BpmnModelInstance modelInstance, String parentId, String subProcessId, String startEventId) {
    ModifiableBpmnModelInstance.wrap(modelInstance)
      .addSubProcessTo(parentId)
      .id(subProcessId)
      .triggerByEvent()
      .embeddedSubProcess()
        .startEvent(startEventId).timerWithDuration("PT10M")
      .subProcessDone()
      .done();

    TimerEventTrigger trigger = new TimerEventTrigger();
    trigger.engine = engine;
    trigger.activityId = startEventId;
    trigger.handlerType = TimerStartEventSubprocessJobHandler.TYPE;

    return trigger;
  }


  protected static class TimerEventTrigger implements MigratingBpmnEventTrigger {

    protected ProcessEngine engine;
    protected String activityId;
    protected String handlerType;

    @Override
    public void trigger(String processInstanceId) {
      ManagementService managementService = engine.getManagementService();
      Job timerJob = managementService.createJobQuery().processInstanceId(processInstanceId).activityId(activityId).singleResult();

      if (timerJob == null) {
        throw new ProcessEngineException("No job for this event found in context of process instance " + processInstanceId);
      }

      managementService.executeJob(timerJob.getId());
    }

    @Override
    public void assertEventTriggerMigrated(MigrationTestRule migrationContext, String targetActivityId) {
      migrationContext.assertJobMigrated(activityId, targetActivityId, handlerType);
    }

    @Override
    public MigratingBpmnEventTrigger inContextOf(String newActivityId) {
      TimerEventTrigger newTrigger = new TimerEventTrigger();
      newTrigger.activityId = newActivityId;
      newTrigger.engine = engine;
      newTrigger.handlerType = handlerType;
      return newTrigger;
    }

  }

}
