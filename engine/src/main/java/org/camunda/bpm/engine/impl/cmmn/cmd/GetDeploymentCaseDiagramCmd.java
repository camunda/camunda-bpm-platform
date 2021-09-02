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
package org.camunda.bpm.engine.impl.cmmn.cmd;

import java.io.InputStream;
import java.io.Serializable;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourceCmd;
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

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadCaseDefinition(caseDefinition);
    }

    final String deploymentId = caseDefinition.getDeploymentId();
    final String resourceName = caseDefinition.getDiagramResourceName();

    InputStream caseDiagramStream = null;

    if (resourceName != null) {

      caseDiagramStream = commandContext.runWithoutAuthorization(new GetDeploymentResourceCmd(deploymentId, resourceName));
    }

    return caseDiagramStream;
  }

}
