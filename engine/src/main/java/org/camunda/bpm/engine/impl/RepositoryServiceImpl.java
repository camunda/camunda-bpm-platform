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
import org.camunda.bpm.engine.impl.cmd.*;
import org.camunda.bpm.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.repository.ProcessApplicationDeploymentBuilderImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.DiagramLayout;
import org.camunda.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Joram Barrez
 */
public class RepositoryServiceImpl extends ServiceImpl implements RepositoryService {

  public DeploymentBuilder createDeployment() {
    return new DeploymentBuilderImpl(this);
  }

  public ProcessApplicationDeploymentBuilder createDeployment(ProcessApplicationReference processApplication) {
    return new ProcessApplicationDeploymentBuilderImpl(this, processApplication);
  }

  public Deployment deploy(DeploymentBuilderImpl deploymentBuilder) {
    return commandExecutor.execute(new DeployCmd<Deployment>(deploymentBuilder));
  }

  public void deleteDeployment(String deploymentId) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, false));
  }

  public void deleteDeploymentCascade(String deploymentId) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, true));
  }

  public void deleteDeployment(String deploymentId, boolean cascade) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, cascade));
  }

  public ProcessDefinitionQuery createProcessDefinitionQuery() {
    return new ProcessDefinitionQueryImpl(commandExecutor);
  }

  @SuppressWarnings("unchecked")
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return commandExecutor.execute(new GetDeploymentResourceNamesCmd(deploymentId));
  }

  public InputStream getResourceAsStream(String deploymentId, String resourceName) {
    return commandExecutor.execute(new GetDeploymentResourceCmd(deploymentId, resourceName));
  }

  public DeploymentQuery createDeploymentQuery() {
    return new DeploymentQueryImpl(commandExecutor);
  }

  public ProcessDefinition getProcessDefinition(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDefinitionCmd(processDefinitionId));
  }

  public ReadOnlyProcessDefinition getDeployedProcessDefinition(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDefinitionCmd(processDefinitionId));
  }

  public void suspendProcessDefinitionById(String processDefinitionId) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(processDefinitionId, null, false, null));
  }

  public void suspendProcessDefinitionById(String processDefinitionId, boolean suspendProcessInstances, Date suspensionDate) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(processDefinitionId, null, suspendProcessInstances, suspensionDate));
  }

  public void suspendProcessDefinitionByKey(String processDefinitionKey) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(null, processDefinitionKey, false, null));
  }

  public void suspendProcessDefinitionByKey(String processDefinitionKey, boolean suspendProcessInstances, Date suspensionDate) {
    commandExecutor.execute(new SuspendProcessDefinitionCmd(null, processDefinitionKey, suspendProcessInstances, suspensionDate));
  }

  public void activateProcessDefinitionById(String processDefinitionId) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(processDefinitionId, null, false, null));
  }

  public void activateProcessDefinitionById(String processDefinitionId, boolean activateProcessInstances, Date activationDate) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(processDefinitionId, null, activateProcessInstances, activationDate));
  }

  public void activateProcessDefinitionByKey(String processDefinitionKey) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(null, processDefinitionKey, false, null));
  }

  public void activateProcessDefinitionByKey(String processDefinitionKey, boolean activateProcessInstances, Date activationDate) {
    commandExecutor.execute(new ActivateProcessDefinitionCmd(null, processDefinitionKey, activateProcessInstances, activationDate));
  }

  public InputStream getProcessModel(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessModelCmd(processDefinitionId));
  }

  public InputStream getProcessDiagram(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDiagramCmd(processDefinitionId));
  }

  public DiagramLayout getProcessDiagramLayout(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDiagramLayoutCmd(processDefinitionId));
  }

  public BpmnModelInstance getBpmnModelInstance(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentBpmnModelInstanceCmd(processDefinitionId));
  }

  public void addCandidateStarterUser(String processDefinitionId, String userId) {
    commandExecutor.execute(new AddIdentityLinkForProcessDefinitionCmd(processDefinitionId, userId, null));
  }

  public void addCandidateStarterGroup(String processDefinitionId, String groupId) {
    commandExecutor.execute(new AddIdentityLinkForProcessDefinitionCmd(processDefinitionId, null, groupId));
  }

  public void deleteCandidateStarterGroup(String processDefinitionId, String groupId) {
    commandExecutor.execute(new DeleteIdentityLinkForProcessDefinitionCmd(processDefinitionId, null, groupId));
  }

  public void deleteCandidateStarterUser(String processDefinitionId, String userId) {
    commandExecutor.execute(new DeleteIdentityLinkForProcessDefinitionCmd(processDefinitionId, userId, null));
  }

  public List<IdentityLink> getIdentityLinksForProcessDefinition(String processDefinitionId) {
    return commandExecutor.execute(new GetIdentityLinksForProcessDefinitionCmd(processDefinitionId));
  }

}
