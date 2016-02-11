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
package org.camunda.bpm.engine.impl.jobexecutor;

import java.text.SimpleDateFormat;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * @author Roman Smirnov
 *
 */
public abstract class TimerJobDeclaration<S> extends JobDeclaration<S, TimerEntity> {

  private static final long serialVersionUID = 1L;

  protected Expression description;
  protected TimerDeclarationType type;

  protected String rawJobHandlerConfiguration;

  protected String repeat;

  public TimerJobDeclaration(Expression expression, TimerDeclarationType type, String jobHandlerType) {
    super(jobHandlerType);
    this.description = expression;
    this.type = type;
  }

  public String getRepeat() {
    return repeat;
  }

  public TimerDeclarationType getTimerDeclarationType() {
    return type;
  }

  protected TimerEntity newJobInstance(S execution) {

    TimerEntity timer = new TimerEntity(this);
    if (execution instanceof ExecutionEntity) {
      timer.setExecution((ExecutionEntity) execution);
    }
    else if (execution instanceof CaseExecutionEntity) {
      timer.setCaseExecution((CaseExecutionEntity) execution);
    }

    return timer;
  }

  protected String prepareRepeat(String dueDate) {
    if (dueDate.startsWith("R") && dueDate.split("/").length==2) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      return dueDate.replace("/","/"+sdf.format(ClockUtil.getCurrentTime())+"/");
    }
    return dueDate;
  }

  public TimerEntity createTimerInstance(S execution) {
    return createTimer(execution);
  }

  public TimerEntity createTimer(String deploymentId) {
    TimerEntity timer = super.createJobInstance((S) null);
    timer.setDeploymentId(deploymentId);
    scheduleTimer(timer);
    return timer;
  }

  public TimerEntity createTimer(S execution) {
    TimerEntity timer = super.createJobInstance(execution);
    scheduleTimer(timer);
    return timer;
  }

  protected void scheduleTimer(TimerEntity timer) {
    Context
      .getCommandContext()
      .getJobManager()
      .schedule(timer);
  }

  public void setRawJobHandlerConfiguration(String rawJobHandlerConfiguration) {
    this.rawJobHandlerConfiguration = rawJobHandlerConfiguration;
  }

  @Override
  protected JobHandlerConfiguration resolveJobHandlerConfiguration(S context) {
    return resolveJobHandler().newConfiguration(rawJobHandlerConfiguration);
  }
}
