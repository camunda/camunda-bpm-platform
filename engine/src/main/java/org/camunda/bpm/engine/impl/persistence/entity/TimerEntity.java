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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.calendar.BusinessCalendar;
import org.camunda.bpm.engine.impl.calendar.CycleBusinessCalendar;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventJobHandler;


/**
 * @author Tom Baeyens
 */
public class TimerEntity extends JobEntity {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(TimerEntity.class.getName());

  protected String repeat;

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
    retries = te.retries;
    executionId = te.executionId;
    processInstanceId = te.processInstanceId;
    jobDefinitionId = te.jobDefinitionId;
    suspensionState = te.suspensionState;
    deploymentId = te.deploymentId;
    processDefinitionId = te.processDefinitionId;
    processDefinitionKey = te.processDefinitionKey;
  }

  @Override
  public void execute(CommandContext commandContext) {
    if (repeat != null) {
      // this timer is a repeating timer
      JobHandler jobHandler = getJobHandler();
      TimerEventJobHandler timerEventJobHandler = (TimerEventJobHandler) jobHandler;
      if (!timerEventJobHandler.isFollowUpJobCreated(jobHandlerConfiguration)) {
        // a follow up timer job has not been scheduled yet
        createNewTimerJob(timerEventJobHandler);
      }
    }

    super.execute(commandContext);

    if (log.isLoggable(Level.FINE)) {
      log.fine("Timer " + getId() + " fired. Deleting timer.");
    }

    delete(true);
  }

  protected void createNewTimerJob(final TimerEventJobHandler timerEventJobHandler) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
    CreateNewTimerJobCommand command = new CreateNewTimerJobCommand(this, timerEventJobHandler);
    commandExecutor.execute(command);
  }

  protected Date calculateRepeat() {
    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(CycleBusinessCalendar.NAME);
    return businessCalendar.resolveDuedate(repeat);
  }

  public String getRepeat() {
    return repeat;
  }

  public void setRepeat(String repeat) {
    this.repeat = repeat;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[repeat" + repeat
           + ", id=" + id
           + ", revision=" + revision
           + ", duedate=" + duedate
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

  protected class CreateNewTimerJobCommand implements Command<Void> {

    protected TimerEntity outerTimer;
    protected TimerEventJobHandler timerEventJobHandler;

    public CreateNewTimerJobCommand(TimerEntity outerTimer, TimerEventJobHandler timerEventJobHandler) {
      this.outerTimer = outerTimer;
      this.timerEventJobHandler = timerEventJobHandler;
    }

    public Void execute(CommandContext commandContext) {

      // load timer job into the cache of this inner command
      // to provoke a possible OptimisticLockingException
      // when e.g. someone else changes this job in parallel
      TimerEntity innerTimer = (TimerEntity) commandContext
          .getJobManager()
          .findJobById(outerTimer.getId());

      Date newDueDate = calculateRepeat();

      if (newDueDate != null) {
        // create new timer job
        TimerEntity newTimer = new TimerEntity(innerTimer);
        newTimer.setDuedate(newDueDate);
        commandContext
          .getJobManager()
          .schedule(newTimer);

        // update job handler configuration
        timerEventJobHandler.setFollowUpJobCreated(innerTimer);
        outerTimer.setJobHandlerConfiguration(innerTimer.getJobHandlerConfiguration());

        // increment revision to avoid an OptimisticLockingException
        // in the outer command
        outerTimer.setRevision(outerTimer.getRevisionNext());
      }

      return null;
    }

  }
}
