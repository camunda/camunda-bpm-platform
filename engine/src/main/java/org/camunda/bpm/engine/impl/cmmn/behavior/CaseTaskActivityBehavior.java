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
package org.camunda.bpm.engine.impl.cmmn.behavior;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnCaseInstance;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;

/**
 * @author Roman Smirnov
 *
 */
public class CaseTaskActivityBehavior extends ProcessOrCaseTaskActivityBehavior {

  protected void triggerCallableElement(CmmnActivityExecution execution, Map<String, Object> variables, String businessKey) {
    String caseDefinitionKey = getDefinitionKey(execution);

    DeploymentCache deploymentCache = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache();

    CmmnCaseDefinition caseDefinition = null;
    if (isLatestBinding()) {
      caseDefinition = deploymentCache.findDeployedLatestCaseDefinitionByKey(caseDefinitionKey);

    } else if (isDeploymentBinding()) {
      String deploymentId = getDeploymentId(execution);
      caseDefinition = deploymentCache.findDeployedCaseDefinitionByDeploymentAndKey(deploymentId, caseDefinitionKey);

    } else if (isVersionBinding()) {
      Integer version = getVersion(execution);
      caseDefinition = deploymentCache.findDeployedCaseDefinitionByKeyAndVersion(caseDefinitionKey, version);
    }

    CmmnCaseInstance caseInstance = execution.createSubCaseInstance(caseDefinition);
    caseInstance.create(businessKey, variables);
  }

  protected void manualCompleting(CmmnActivityExecution execution) {
    CaseExecutionEntity subCaseInstance = getSubCaseInstance(execution);

    if (subCaseInstance != null) {
      throw new ProcessEngineException("It is not possible to complete a case task manually, because the called case instance is not closed.");
    }
  }

  protected void terminating(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("This method has not been implemented yet.");
  }

  protected void suspending(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("This method has not been implemented yet.");
  }

  protected void resuming(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("This method has not been implemented yet.");
  }

  protected CaseExecutionEntity getSubCaseInstance(CmmnActivityExecution execution) {
    String id = execution.getId();

    CaseExecutionEntity subCaseInstance = Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findSubCaseInstanceBySuperCaseExecutionId(id);

    return subCaseInstance;

  }

}
