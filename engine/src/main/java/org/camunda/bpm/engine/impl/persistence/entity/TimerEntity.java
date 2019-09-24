/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.calendar.BusinessCalendar;
import org.camunda.bpm.engine.impl.calendar.CycleBusinessCalendar;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.RepeatingFailedJobListener;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventJobHandler.TimerJobConfiguration;


/**
 * @author Tom Baeyens
 */
public class TimerEntity extends JobEntity {

  public static final String TYPE = "timer";

  private static final long serialVersionUID = 1L;

  protected String repeat;

  protected long repeatOffset;

  public TimerEntity() {
  }

  public TimerEntity(TimerDeclarationImpl timerDeclaration) {
    repeat = timerDeclaration.getRepeat();
  }

  protected TimerEntity(TimerEntity te) {
    jobHandlerConfiguration = te.jobHandlerConfiguration;
    jobHandlerType = te.jobHandlerType;
    isExclusive = te.isExclusive;
    repeat = te.repeat;
    repeatOffset = te.repeatOffset;
    retries = te.retries;
    executionId = te.executionId;
    processInstanceId = te.processInstanceId;
    jobDefinitionId = te.jobDefinitionId;
    suspensionState = te.suspensionState;
    deploymentId = te.deploymentId;
    processDefinitionId = te.processDefinitionId;
    processDefinitionKey = te.processDefinitionKey;
    tenantId = te.tenantId;
    priority = te.priority;
  }

  @Override
  protected void preExecute(CommandContext commandContext) {
    if (getJobHandler() instanceof TimerEventJobHandler) {
      TimerJobConfiguration configuration = (TimerJobConfiguration) getJobHandlerConfiguration();
      if (repeat != null && !configuration.isFollowUpJobCreated()) {
        // this timer is a repeating timer and
        // a follow up timer job has not been scheduled yet

        Date newDueDate = calculateRepeat();

        if (newDueDate != null) {
          // the listener is added to the transaction as SYNC on ROLLABCK,
          // when it is necessary to schedule a new timer job invocation.
          // If the transaction does not rollback, it is ignored.
          ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
          CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
          RepeatingFailedJobListener listener = createRepeatingFailedJobListener(commandExecutor);

          commandContext.getTransactionContext().addTransactionListener(
              TransactionState.ROLLED_BACK,
              listener);

          // create a new timer job
          createNewTimerJob(newDueDate);
        }
      }
    }
  }

  protected RepeatingFailedJobListener createRepeatingFailedJobListener(CommandExecutor commandExecutor) {
    return new RepeatingFailedJobListener(commandExecutor, getId());
  }

  public void createNewTimerJob(Date dueDate) {
    // create new timer job
    TimerEntity newTimer = new TimerEntity(this);
    newTimer.setDuedate(dueDate);
    Context
      .getCommandContext()
      .getJobManager()
      .schedule(newTimer);
  }

  public Date calculateRepeat() {
    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(CycleBusinessCalendar.NAME);
    return ((CycleBusinessCalendar) businessCalendar).resolveDuedate(repeat, null, repeatOffset);
  }

  public String getRepeat() {
    return repeat;
  }

  public void setRepeat(String repeat) {
    this.repeat = repeat;
  }

  public long getRepeatOffset() {
    return repeatOffset;
  }

  public void setRepeatOffset(long repeatOffset) {
    this.repeatOffset = repeatOffset;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Object getPersistentState() {
    Map<String, Object> persistentState = (HashMap) super.getPersistentState();
    persistentState.put("repeat", repeat);

    return persistentState;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[repeat=" + repeat
           + ", id=" + id
           + ", revision=" + revision
           + ", duedate=" + duedate
           + ", repeatOffset=" + repeatOffset
           + ", lockOwner=" + lockOwner
           + ", lockExpirationTime=" + lockExpirationTime
           + ", executionId=" + executionId
           + ", processInstanceId=" + processInstanceId
           + ", isExclusive=" + isExclusive
           + ", retries=" + retries
           + ", jobHandlerType=" + jobHandlerType
           + ", jobHandlerConfiguration=" + jobHandlerConfiguration
           + ", exceptionByteArray=" + exceptionByteArray
           + ", exceptionByteArrayId=" + exceptionByteArrayId
           + ", exceptionMessage=" + exceptionMessage
           + ", deploymentId=" + deploymentId
           + "]";
  }

}
