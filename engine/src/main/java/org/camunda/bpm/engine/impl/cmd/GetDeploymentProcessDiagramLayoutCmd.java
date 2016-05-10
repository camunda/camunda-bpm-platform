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
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.bpmn.diagram.ProcessDiagramLayoutFactory;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.DiagramLayout;


/**
 * Provides positions and dimensions of elements in a process diagram as
 * provided by {@link GetDeploymentProcessDiagramCmd}.
 *
 * This command requires a process model and a diagram image to be deployed.
 * @author Falko Menge
 */
public class GetDeploymentProcessDiagramLayoutCmd implements Command<DiagramLayout>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;

  public GetDeploymentProcessDiagramLayoutCmd(String processDefinitionId) {
    if (processDefinitionId == null || processDefinitionId.length() < 1) {
      throw new ProcessEngineException("The process definition id is mandatory, but '" + processDefinitionId + "' has been provided.");
    }
    this.processDefinitionId = processDefinitionId;
  }

  public DiagramLayout execute(final CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(processDefinitionId);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadProcessDefinition(processDefinition);
    }

    InputStream processModelStream = commandContext.runWithoutAuthorization(new Callable<InputStream>() {
      public InputStream call() throws Exception {
        return new GetDeploymentProcessModelCmd(processDefinitionId).execute(commandContext);
      }
    });

    InputStream processDiagramStream = commandContext.runWithoutAuthorization(new Callable<InputStream>() {
      public InputStream call() throws Exception {
        return new GetDeploymentProcessDiagramCmd(processDefinitionId).execute(commandContext);
      }
    });

    return new ProcessDiagramLayoutFactory().getProcessDiagramLayout(processModelStream, processDiagramStream);
  }

}
