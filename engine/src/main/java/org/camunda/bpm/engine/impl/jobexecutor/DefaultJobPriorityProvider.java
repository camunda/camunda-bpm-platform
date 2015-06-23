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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;


/**
 * @author Thorben Lindhauer
 *
 */
public class DefaultJobPriorityProvider implements JobPriorityProvider {

  @Override
  public int determinePriority(ExecutionEntity execution, JobDeclaration<?> jobDeclaration) {

    Integer activityPriority = getActivityPriority(execution, jobDeclaration);
    if (activityPriority != null) {
      return activityPriority;
    }

    Integer processDefinitionPriority = getProcessDefinitionPriority(execution, jobDeclaration);
    if (processDefinitionPriority != null) {
      return processDefinitionPriority;
    }

    return JobPriorityProvider.DEFAULT_PRIORITY;
  }

  protected Integer getProcessDefinitionPriority(ExecutionEntity execution, JobDeclaration<?> jobDeclaration) {
    if (execution != null) {
      ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
      ParameterValueProvider priorityProvider = (ParameterValueProvider) processDefinition.getProperty(BpmnParse.PROPERTYNAME_JOB_PRIORITY);

      if (priorityProvider != null) {
        return evaluateValueProvider(priorityProvider, execution, jobDeclaration);
      }
    }

    return null;
  }

  protected Integer getActivityPriority(ExecutionEntity execution, JobDeclaration<?> jobDeclaration) {
    if (jobDeclaration != null) {
      ParameterValueProvider priorityProvider = jobDeclaration.getJobPriorityProvider();
      if (priorityProvider != null) {
        return evaluateValueProvider(priorityProvider, execution, jobDeclaration);
      }
    }

    return null;
  }

  protected Integer evaluateValueProvider(ParameterValueProvider valueProvider, ExecutionEntity execution, JobDeclaration<?> jobDeclaration) {
    Object value = valueProvider.getValue(execution);

    if (!(value instanceof Integer)) {
      throw new ProcessEngineException("Priority for job " + jobDeclaration.getActivityId()
          + "/" + jobDeclaration.getJobHandlerType() + " instantiated "
          + "in context of " + execution + " is not an Integer");
    }
    else {
      return (Integer) value;
    }
  }

}
