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

package org.camunda.bpm.engine.impl.core.model;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * Default implementation for {@link BaseCallableElement#getTenantIdProvider()}.
 * Uses the tenant id of the calling definition.
 */
public class DefaultCallableElementTenantIdProvider implements ParameterValueProvider {

  @Override
  public Object getValue(VariableScope execution) {
    if (execution instanceof ExecutionEntity) {
      return getProcessDefinitionTenantId((ExecutionEntity) execution);

    } else if (execution instanceof CaseExecutionEntity) {
      return getCaseDefinitionTenantId((CaseExecutionEntity) execution);

    } else {
      throw new ProcessEngineException("Unexpected execution of type " + execution.getClass().getName());
    }
  }

  protected String getProcessDefinitionTenantId(ExecutionEntity execution) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();
    return processDefinition.getTenantId();
  }

  protected String getCaseDefinitionTenantId(CaseExecutionEntity caseExecution) {
    CaseDefinitionEntity caseDefinition = (CaseDefinitionEntity) caseExecution.getCaseDefinition();
    return caseDefinition.getTenantId();
  }

}
