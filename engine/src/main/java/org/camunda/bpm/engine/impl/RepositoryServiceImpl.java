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

package org.camunda.bpm.engine.impl;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.DeploymentResourceNotFoundException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.exception.cmmn.CaseDefinitionNotFoundException;
import org.camunda.bpm.engine.exception.cmmn.CmmnModelInstanceNotFoundException;
import org.camunda.bpm.engine.impl.cmd.ActivateProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.AddIdentityLinkForProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteDeploymentCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteIdentityLinkForProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.DeployCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentBpmnModelInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentCaseDiagramCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentCmmnModelInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentProcessDiagramCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentProcessDiagramLayoutCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentProcessModelCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourceCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourceForIdCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourceNamesCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourcesCmd;
import org.camunda.bpm.engine.impl.cmd.GetIdentityLinksForProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.GetDeploymentCaseDefinitionCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.GetDeploymentCaseModelCmd;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.repository.ProcessApplicationDeploymentBuilderImpl;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.DiagramLayout;
import org.camunda.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;

/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Joram Barrez
 */
public class RepositoryServiceImpl extends ServiceImpl implements RepositoryService {

  @Override
  public DeploymentBuilder createDeployment() {
    return new DeploymentBuilderImpl(this);
  }

  @Override
  public ProcessApplicationDeploymentBuilder createDeployment(final ProcessApplicationReference processApplication) {
    return new ProcessApplicationDeploymentBuilderImpl(this, processApplication);
  }

  public Deployment deploy(final DeploymentBuilderImpl deploymentBuilder) {
    return commandExecutor.execute(new DeployCmd<Deployment>(deploymentBuilder));
  }

  @Override
  public void deleteDeployment(final String deploymentId) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, false, false));
  }

  @Override
  public void deleteDeploymentCascade(final String deploymentId) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, true, false));
  }

  @Override
  public void deleteDeployment(final String deploymentId, final boolean cascade) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, cascade, false));
  }

  @Override
  public void deleteDeployment(final String deploymentId, final boolean cascade, final boolean skipCustomListeners) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, cascade, skipCustomListeners));
  }

  @Override
  public ProcessDefinitionQuery createProcessDefinitionQuery() {
    return new ProcessDefinitionQueryImpl(commandExecutor);
  }

  @Override
  public CaseDefinitionQuery createCaseDefinitionQuery() {
    return new CaseDefinitionQueryImpl(commandExecutor);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> getDeploymentResourceNames(final String deploymentId) {
    return commandExecutor.execute(new GetDeploymentResourceNamesCmd(deploymentId));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Resource> getDeploymentResources(final String deploymentId) {
    return commandExecutor.execute(new GetDeploymentResourcesCmd(deploymentId));
  }

  @Override
  public InputStream getResourceAsStream(final String deploymentId, final String resourceName) {
    return commandExecutor.execute(new GetDeploymentResourceCmd(deploymentId, resourceName));
  }

  @Override
  public InputStream getResourceAsStreamById(final String deploymentId, final String resourceId) {
    return commandExecutor.execute(new GetDeploymentResourceForIdCmd(deploymentId, resourceId));
  }

  @Override
  public DeploymentQuery createDeploymentQuery() {
    return new DeploymentQueryImpl(commandExecutor);
  }

  @Override
  public ProcessDefinition getProcessDefinition(final String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDefinitionCmd(processDefinitionId));
  }

  public ReadOnlyProcessDefinition getDeployedProcessDefinition(final String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDefinitionCmd(processDefinitionId));
  }

  @Override
  public void suspendProcessDefinitionById(final String processDefinitionId) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(processDefinitionId, null, false, null));
  }

  @Override
  public void suspendProcessDefinitionById(final String processDefinitionId, final boolean suspendProcessInstances, final Date suspensionDate) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(processDefinitionId, null, suspendProcessInstances, suspensionDate));
  }

  @Override
  public void suspendProcessDefinitionByKey(final String processDefinitionKey) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(null, processDefinitionKey, false, null));
  }

  @Override
  public void suspendProcessDefinitionByKey(final String processDefinitionKey, final boolean suspendProcessInstances, final Date suspensionDate) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(null, processDefinitionKey, suspendProcessInstances, suspensionDate));
  }

  @Override
  public void activateProcessDefinitionById(final String processDefinitionId) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(processDefinitionId, null, false, null));
  }

  @Override
  public void activateProcessDefinitionById(final String processDefinitionId, final boolean activateProcessInstances, final Date activationDate) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(processDefinitionId, null, activateProcessInstances, activationDate));
  }

  @Override
  public void activateProcessDefinitionByKey(final String processDefinitionKey) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(null, processDefinitionKey, false, null));
  }

  @Override
  public void activateProcessDefinitionByKey(final String processDefinitionKey, final boolean activateProcessInstances, final Date activationDate) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(null, processDefinitionKey, activateProcessInstances, activationDate));
  }

  @Override
  public InputStream getProcessModel(final String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessModelCmd(processDefinitionId));
  }

  @Override
  public InputStream getProcessDiagram(final String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDiagramCmd(processDefinitionId));
  }

  @Override
  public InputStream getCaseDiagram(final String caseDefinitionId) {
    return commandExecutor.execute(new GetDeploymentCaseDiagramCmd(caseDefinitionId));
  }

  @Override
  public DiagramLayout getProcessDiagramLayout(final String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDiagramLayoutCmd(processDefinitionId));
  }

  @Override
  public BpmnModelInstance getBpmnModelInstance(final String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentBpmnModelInstanceCmd(processDefinitionId));
  }

  @Override
  public CmmnModelInstance getCmmnModelInstance(final String caseDefinitionId) {
    try {
      return commandExecutor.execute(new GetDeploymentCmmnModelInstanceCmd(caseDefinitionId));

    } catch (final NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (final CmmnModelInstanceNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    } catch (final DeploymentResourceNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }
  }

  @Override
  public void addCandidateStarterUser(final String processDefinitionId, final String userId) {
    commandExecutor.execute(new AddIdentityLinkForProcessDefinitionCmd(processDefinitionId, userId, null));
  }

  @Override
  public void addCandidateStarterGroup(final String processDefinitionId, final String groupId) {
    commandExecutor.execute(new AddIdentityLinkForProcessDefinitionCmd(processDefinitionId, null, groupId));
  }

  @Override
  public void deleteCandidateStarterGroup(final String processDefinitionId, final String groupId) {
    commandExecutor.execute(new DeleteIdentityLinkForProcessDefinitionCmd(processDefinitionId, null, groupId));
  }

  @Override
  public void deleteCandidateStarterUser(final String processDefinitionId, final String userId) {
    commandExecutor.execute(new DeleteIdentityLinkForProcessDefinitionCmd(processDefinitionId, userId, null));
  }

  @Override
  public List<IdentityLink> getIdentityLinksForProcessDefinition(final String processDefinitionId) {
    return commandExecutor.execute(new GetIdentityLinksForProcessDefinitionCmd(processDefinitionId));
  }

  @Override
  public CaseDefinition getCaseDefinition(final String caseDefinitionId) {
    try {
      return commandExecutor.execute(new GetDeploymentCaseDefinitionCmd(caseDefinitionId));

    } catch (final NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (final CaseDefinitionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }
  }

  @Override
  public InputStream getCaseModel(final String caseDefinitionId) {
    try {
      return commandExecutor.execute(new GetDeploymentCaseModelCmd(caseDefinitionId));

    } catch (final NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (final CaseDefinitionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    } catch (final DeploymentResourceNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }

  }

}
