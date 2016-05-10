/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.history.producer;

import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * @author Sebastian Menski
 */
public class DefaultCmmnHistoryEventProducer implements CmmnHistoryEventProducer {

  public HistoryEvent createCaseInstanceCreateEvt(DelegateCaseExecution caseExecution) {
    final CaseExecutionEntity caseExecutionEntity = (CaseExecutionEntity) caseExecution;

    // create event instance
    HistoricCaseInstanceEventEntity evt = newCaseInstanceEventEntity(caseExecutionEntity);

    // initialize event
    initCaseInstanceEvent(evt, caseExecutionEntity, HistoryEventTypes.CASE_INSTANCE_CREATE);

    // set create time
    evt.setCreateTime(ClockUtil.getCurrentTime());

    // set create user id
    evt.setCreateUserId(Context.getCommandContext().getAuthenticatedUserId());

    // set super case instance id
    CmmnExecution superCaseExecution = caseExecutionEntity.getSuperCaseExecution();
    if (superCaseExecution != null) {
      evt.setSuperCaseInstanceId(superCaseExecution.getCaseInstanceId());
    }

    // set super process instance id
    ExecutionEntity superExecution = caseExecutionEntity.getSuperExecution();
    if (superExecution != null) {
      evt.setSuperProcessInstanceId(superExecution.getProcessInstanceId());
    }

    return evt;
  }

  public HistoryEvent createCaseInstanceUpdateEvt(DelegateCaseExecution caseExecution) {
    final CaseExecutionEntity caseExecutionEntity = (CaseExecutionEntity) caseExecution;

    // create event instance
    HistoricCaseInstanceEventEntity evt = loadCaseInstanceEventEntity(caseExecutionEntity);

    // initialize event
    initCaseInstanceEvent(evt, caseExecutionEntity, HistoryEventTypes.CASE_INSTANCE_UPDATE);

    return evt;
  }

  public HistoryEvent createCaseInstanceCloseEvt(DelegateCaseExecution caseExecution) {
    final CaseExecutionEntity caseExecutionEntity = (CaseExecutionEntity) caseExecution;

    // create event instance
    HistoricCaseInstanceEventEntity evt = loadCaseInstanceEventEntity(caseExecutionEntity);

    // initialize event
    initCaseInstanceEvent(evt, caseExecutionEntity, HistoryEventTypes.CASE_INSTANCE_CLOSE);

    // set end time
    evt.setEndTime(ClockUtil.getCurrentTime());

    if (evt.getStartTime() != null) {
      evt.setDurationInMillis(evt.getEndTime().getTime() - evt.getStartTime().getTime());
    }

    return evt;
  }

  public HistoryEvent createCaseActivityInstanceCreateEvt(DelegateCaseExecution caseExecution) {
    final CaseExecutionEntity caseExecutionEntity = (CaseExecutionEntity) caseExecution;

    // create event instance
    HistoricCaseActivityInstanceEventEntity evt = newCaseActivityInstanceEventEntity(caseExecutionEntity);

    // initialize event
    initCaseActivityInstanceEvent(evt, caseExecutionEntity, HistoryEventTypes.CASE_ACTIVITY_INSTANCE_CREATE);

    // set start time
    evt.setCreateTime(ClockUtil.getCurrentTime());

    return evt;
  }

  public HistoryEvent createCaseActivityInstanceUpdateEvt(DelegateCaseExecution caseExecution) {
    final CaseExecutionEntity caseExecutionEntity = (CaseExecutionEntity) caseExecution;

    // create event instance
    HistoricCaseActivityInstanceEventEntity evt = loadCaseActivityInstanceEventEntity(caseExecutionEntity);

    // initialize event
    initCaseActivityInstanceEvent(evt, caseExecutionEntity, HistoryEventTypes.CASE_ACTIVITY_INSTANCE_UPDATE);

    if (caseExecutionEntity.getTask() != null) {
      evt.setTaskId(caseExecutionEntity.getTask().getId());
    }

    if (caseExecutionEntity.getSubProcessInstance() != null) {
      evt.setCalledProcessInstanceId(caseExecutionEntity.getSubProcessInstance().getId());
    }

    if (caseExecutionEntity.getSubCaseInstance() != null) {
      evt.setCalledCaseInstanceId(caseExecutionEntity.getSubCaseInstance().getId());
    }

    return evt;
  }

  public HistoryEvent createCaseActivityInstanceEndEvt(DelegateCaseExecution caseExecution) {
    final CaseExecutionEntity caseExecutionEntity = (CaseExecutionEntity) caseExecution;

    // create event instance
    HistoricCaseActivityInstanceEventEntity evt = loadCaseActivityInstanceEventEntity(caseExecutionEntity);

    // initialize event
    initCaseActivityInstanceEvent(evt, caseExecutionEntity, HistoryEventTypes.CASE_ACTIVITY_INSTANCE_END);

    // set end time
    evt.setEndTime(ClockUtil.getCurrentTime());

    // calculate duration
    if (evt.getStartTime() != null) {
      evt.setDurationInMillis(evt.getEndTime().getTime() - evt.getStartTime().getTime());
    }

    return evt;
  }

  protected HistoricCaseInstanceEventEntity newCaseInstanceEventEntity(CaseExecutionEntity caseExecutionEntity) {
    return new HistoricCaseInstanceEventEntity();
  }

  protected HistoricCaseInstanceEventEntity loadCaseInstanceEventEntity(CaseExecutionEntity caseExecutionEntity) {
    return newCaseInstanceEventEntity(caseExecutionEntity);
  }

  protected void initCaseInstanceEvent(HistoricCaseInstanceEventEntity evt, CaseExecutionEntity caseExecutionEntity, HistoryEventTypes eventType) {
    evt.setId(caseExecutionEntity.getCaseInstanceId());
    evt.setEventType(eventType.getEventName());
    evt.setCaseDefinitionId(caseExecutionEntity.getCaseDefinitionId());
    evt.setCaseInstanceId(caseExecutionEntity.getCaseInstanceId());
    evt.setCaseExecutionId(caseExecutionEntity.getId());
    evt.setBusinessKey(caseExecutionEntity.getBusinessKey());
    evt.setState(caseExecutionEntity.getState());
    evt.setTenantId(caseExecutionEntity.getTenantId());
  }

  protected HistoricCaseActivityInstanceEventEntity newCaseActivityInstanceEventEntity(CaseExecutionEntity caseExecutionEntity) {
    return new HistoricCaseActivityInstanceEventEntity();
  }

  protected HistoricCaseActivityInstanceEventEntity loadCaseActivityInstanceEventEntity(CaseExecutionEntity caseExecutionEntity) {
    return newCaseActivityInstanceEventEntity(caseExecutionEntity);
  }

  protected void initCaseActivityInstanceEvent(HistoricCaseActivityInstanceEventEntity evt, CaseExecutionEntity caseExecutionEntity, HistoryEventTypes eventType) {
    evt.setId(caseExecutionEntity.getId());
    evt.setParentCaseActivityInstanceId(caseExecutionEntity.getParentId());
    evt.setEventType(eventType.getEventName());
    evt.setCaseDefinitionId(caseExecutionEntity.getCaseDefinitionId());
    evt.setCaseInstanceId(caseExecutionEntity.getCaseInstanceId());
    evt.setCaseExecutionId(caseExecutionEntity.getId());
    evt.setCaseActivityInstanceState(caseExecutionEntity.getState());

    evt.setRequired(caseExecutionEntity.isRequired());

    evt.setCaseActivityId(caseExecutionEntity.getActivityId());
    evt.setCaseActivityName(caseExecutionEntity.getActivityName());
    evt.setCaseActivityType(caseExecutionEntity.getActivityType());

    evt.setTenantId(caseExecutionEntity.getTenantId());
  }

}
