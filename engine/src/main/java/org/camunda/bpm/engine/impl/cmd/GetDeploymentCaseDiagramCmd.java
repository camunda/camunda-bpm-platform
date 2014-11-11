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

package org.camunda.bpm.engine.impl.cmd;

import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * Gives access to a deployed case diagram, e.g., a PNG image, through a stream
 * of bytes.
 * 
 * @author Simon Zambrovski
 */
public class GetDeploymentCaseDiagramCmd implements Command<InputStream>, Serializable {

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(GetDeploymentCaseDiagramCmd.class.getName());

  protected String caseDefinitionId;

  public GetDeploymentCaseDiagramCmd(String caseDefinitionId) {
    if (caseDefinitionId == null || caseDefinitionId.length() < 1) {
      throw new ProcessEngineException("The case definition id is mandatory, but '" + caseDefinitionId + "' has been provided.");
    }
    this.caseDefinitionId = caseDefinitionId;
  }

  @Override
  public InputStream execute(final CommandContext commandContext) {
    CaseDefinitionEntity caseDefinition = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedCaseDefinitionById(caseDefinitionId);
    String deploymentId = caseDefinition.getDeploymentId();
    String resourceName = caseDefinition.getDiagramResourceName();
    if (resourceName == null) {
      log.info("Resource name is null! No case diagram stream exists.");
      return null;
    } else {
      InputStream caseDiagramStream = new GetDeploymentResourceCmd(deploymentId, resourceName).execute(commandContext);
      return caseDiagramStream;
    }
  }

}
