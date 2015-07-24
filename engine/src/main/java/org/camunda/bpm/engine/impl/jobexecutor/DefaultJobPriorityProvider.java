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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;


/**
 * @author Thorben Lindhauer
 *
 */
public class DefaultJobPriorityProvider implements JobPriorityProvider {

  private static final Logger LOG = Logger.getLogger(DefaultJobPriorityProvider.class.getName());

  public static int DEFAULT_PRIORITY = 0;

  public static int DEFAULT_PRIORITY_ON_RESOLUTION_FAILURE = 0;

  public static int getDefaultPriority() {
    return DEFAULT_PRIORITY;
  }

  public static int getDefaultPriorityOnResolutionFailure() {
    return DEFAULT_PRIORITY_ON_RESOLUTION_FAILURE;
  }

  @Override
  public int determinePriority(ExecutionEntity execution, JobDeclaration<?, ?> jobDeclaration) {

    Integer jobDefinitionPriority = getJobDefinitionPriority(execution, jobDeclaration);
    if (jobDefinitionPriority != null) {
      return jobDefinitionPriority;
    }

    Integer activityPriority = getActivityPriority(execution, jobDeclaration);
    if (activityPriority != null) {
      return activityPriority;
    }

    Integer processDefinitionPriority = getProcessDefinitionPriority(execution, jobDeclaration);
    if (processDefinitionPriority != null) {
      return processDefinitionPriority;
    }

    return getDefaultPriority();
  }

  protected Integer getJobDefinitionPriority(ExecutionEntity execution, JobDeclaration<?, ?> jobDeclaration) {
    JobDefinitionEntity jobDefinition = getJobDefinitionFor(jobDeclaration);

    if (jobDefinition != null) {
      return jobDefinition.getOverridingJobPriority();
    }

    return null;
  }

  protected Integer getProcessDefinitionPriority(ExecutionEntity execution, JobDeclaration<?, ?> jobDeclaration) {
    ProcessDefinitionImpl processDefinition = jobDeclaration.getProcessDefinition();

    if (processDefinition != null) {
      ParameterValueProvider priorityProvider = (ParameterValueProvider) processDefinition.getProperty(BpmnParse.PROPERTYNAME_JOB_PRIORITY);

      if (priorityProvider != null) {
        return evaluateValueProvider(priorityProvider, execution, jobDeclaration);
      }
    }

    return null;
  }

  protected JobDefinitionEntity getJobDefinitionFor(JobDeclaration<?, ?> jobDeclaration) {
    if (jobDeclaration.getJobDefinitionId() != null) {
      return Context.getCommandContext()
          .getJobDefinitionManager()
          .findById(jobDeclaration.getJobDefinitionId());
    }
    else {
      return null;
    }
  }

  protected Integer getActivityPriority(ExecutionEntity execution, JobDeclaration<?, ?> jobDeclaration) {
    if (jobDeclaration != null) {
      ParameterValueProvider priorityProvider = jobDeclaration.getJobPriorityProvider();
      if (priorityProvider != null) {
        return evaluateValueProvider(priorityProvider, execution, jobDeclaration);
      }
    }

    return null;
  }

  protected Integer evaluateValueProvider(ParameterValueProvider valueProvider, ExecutionEntity execution, JobDeclaration<?, ?> jobDeclaration) {
    Object value;
    try {
      value = valueProvider.getValue(execution);

    } catch (ProcessEngineException e) {

      if (Context.getProcessEngineConfiguration().isEnableGracefulDegradationOnContextSwitchFailure()
          && isSymptomOfContextSwitchFailure(e, execution)) {

        value = getDefaultPriorityOnResolutionFailure();

        LOG.log(Level.WARNING, "Could not determine priority for job created in context of execution " + execution
            + ". Using default priority " + value, e);
      }
      else {
        throw e;
      }
    }

    if (!(value instanceof Number)) {
      throw new ProcessEngineException(describeContext(jobDeclaration, execution)
          + ": Priority value is not an Integer");
    }
    else {
      Number numberValue = (Number) value;
      if (isValidIntegerValue(numberValue)) {
        return numberValue.intValue();
      }
      else {
        throw new ProcessEngineException(describeContext(jobDeclaration, execution)
            + ": Priority value must be either Short, Integer, or Long in Integer range");
      }
    }
  }

  protected boolean isSymptomOfContextSwitchFailure(Throwable t, ExecutionEntity contextExecution) {
    // a context switch failure can occur, if the current engine has no PA registration for the deployment
    // subclasses may assert the actual throwable to narrow down the diagnose
    return ProcessApplicationContextUtil.getTargetProcessApplication(contextExecution) == null;
  }

  protected String describeContext(JobDeclaration<?, ?> jobDeclaration, ExecutionEntity executionEntity) {
    return "Job " + jobDeclaration.getActivityId()
            + "/" + jobDeclaration.getJobHandlerType() + " instantiated "
            + "in context of " + executionEntity;
  }

  protected boolean isValidIntegerValue(Number value) {
    return
      value instanceof Short ||
      value instanceof Integer ||
      (value instanceof Long && ((long) value.intValue()) == value.longValue());
  }

}
