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
package org.camunda.bpm.engine.impl.cmmn.cmd;

import java.io.Serializable;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmmn.CaseInstanceBuilderImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.runtime.CaseInstance;

/**
 * @author Roman Smirnov
 *
 */
public class CreateCaseInstanceCmd implements Command<CaseInstance>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected Map<String, Object> variables;
  protected String businessKey;

  public CreateCaseInstanceCmd(CaseInstanceBuilderImpl builder) {
    this.caseDefinitionKey = builder.getCaseDefinitionKey();
    this.caseDefinitionId = builder.getCaseDefinitionId();
    this.businessKey = builder.getBusinessKey();
    this.variables = builder.getVariables();
  }

  public CaseInstance execute(CommandContext commandContext) {

    DeploymentCache deploymentCache = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache();

      // Find the case definition
      CaseDefinitionEntity caseDefinition = null;

      if (caseDefinitionId!=null) {
        caseDefinition = deploymentCache.findDeployedCaseDefinitionById(caseDefinitionId);

        if (caseDefinition == null) {
          throw new ProcessEngineException("No case definition found for id = '" + caseDefinitionId + "'");
        }

      } else if(caseDefinitionKey != null){
        caseDefinition = deploymentCache.findDeployedLatestCaseDefinitionByKey(caseDefinitionKey);

        if (caseDefinition == null) {
          throw new ProcessEngineException("No case definition found for key '" + caseDefinitionKey +"'");
        }

      } else {
        throw new ProcessEngineException("caseDefinitionKey and caseDefinitionId are null");
      }

      // Start the case instance
      CaseExecutionEntity caseInstance = (CaseExecutionEntity) caseDefinition.createCaseInstance();
      caseInstance.create(businessKey, variables);
      return caseInstance;
  }

}
