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

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.DeploymentResourceNotFoundException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.exception.cmmn.CaseDefinitionNotFoundException;
import org.camunda.bpm.engine.exception.cmmn.CmmnModelInstanceNotFoundException;
import org.camunda.bpm.engine.exception.dmn.DecisionDefinitionNotFoundException;
import org.camunda.bpm.engine.exception.dmn.DmnModelInstanceNotFoundException;
import org.camunda.bpm.engine.impl.cmd.ActivateProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.AddIdentityLinkForProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteDeploymentCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteIdentityLinkForProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.DeployCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentBpmnModelInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentProcessDiagramCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentProcessDiagramLayoutCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentProcessModelCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourceCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourceForIdCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourceNamesCmd;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourcesCmd;
import org.camunda.bpm.engine.impl.cmd.GetIdentityLinksForProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.RedeployCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.GetDeploymentCaseDefinitionCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.GetDeploymentCaseDiagramCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.GetDeploymentCaseModelCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.GetDeploymentCmmnModelInstanceCmd;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.dmn.cmd.GetDeploymentDecisionDefinitionCmd;
import org.camunda.bpm.engine.impl.dmn.cmd.GetDeploymentDecisionDiagramCmd;
import org.camunda.bpm.engine.impl.dmn.cmd.GetDeploymentDecisionModelCmd;
import org.camunda.bpm.engine.impl.dmn.cmd.GetDeploymentDmnModelInstanceCmd;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.repository.ProcessApplicationDeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.repository.ProcessApplicationRedeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.repository.RedeploymentBuilderImpl;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.DiagramLayout;
import org.camunda.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessApplicationRedeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.repository.RedeploymentBuilder;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.dmn.DmnModelInstance;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

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

  public RedeploymentBuilder createRedeployment(String deploymentId) {
    return new RedeploymentBuilderImpl(this, deploymentId);
  }

  public ProcessApplicationRedeploymentBuilder createRedeployment(String deploymentId, ProcessApplicationReference processApplicationReference) {
    return new ProcessApplicationRedeploymentBuilderImpl(this, deploymentId, processApplicationReference);
  }

  public Deployment deploy(DeploymentBuilderImpl deploymentBuilder) {
    return commandExecutor.execute(new DeployCmd<Deployment>(deploymentBuilder));
  }

  public Deployment redeploy(RedeploymentBuilderImpl redeploymentBuilder) {
    return commandExecutor.execute(new RedeployCmd(redeploymentBuilder));
  }

  public void deleteDeployment(String deploymentId) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, false, false));
  }

  public void deleteDeploymentCascade(String deploymentId) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, true, false));
  }

  public void deleteDeployment(String deploymentId, boolean cascade) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, cascade, false));
  }

  public void deleteDeployment(String deploymentId, boolean cascade, boolean skipCustomListeners) {
    commandExecutor.execute(new DeleteDeploymentCmd(deploymentId, cascade, skipCustomListeners));
  }

  public ProcessDefinitionQuery createProcessDefinitionQuery() {
    return new ProcessDefinitionQueryImpl(commandExecutor);
  }

  public CaseDefinitionQuery createCaseDefinitionQuery() {
    return new CaseDefinitionQueryImpl(commandExecutor);
  }

  public DecisionDefinitionQuery createDecisionDefinitionQuery() {
    return new DecisionDefinitionQueryImpl(commandExecutor);
  }

  @SuppressWarnings("unchecked")
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return commandExecutor.execute(new GetDeploymentResourceNamesCmd(deploymentId));
  }

  @SuppressWarnings("unchecked")
  public List<Resource> getDeploymentResources(String deploymentId) {
    return commandExecutor.execute(new GetDeploymentResourcesCmd(deploymentId));
  }

  public InputStream getResourceAsStream(String deploymentId, String resourceName) {
    return commandExecutor.execute(new GetDeploymentResourceCmd(deploymentId, resourceName));
  }

  public InputStream getResourceAsStreamById(String deploymentId, String resourceId) {
    return commandExecutor.execute(new GetDeploymentResourceForIdCmd(deploymentId, resourceId));
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

  public InputStream getCaseDiagram(String caseDefinitionId) {
    return commandExecutor.execute(new GetDeploymentCaseDiagramCmd(caseDefinitionId));
  }

  public DiagramLayout getProcessDiagramLayout(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentProcessDiagramLayoutCmd(processDefinitionId));
  }

  public BpmnModelInstance getBpmnModelInstance(String processDefinitionId) {
    return commandExecutor.execute(new GetDeploymentBpmnModelInstanceCmd(processDefinitionId));
  }

  public CmmnModelInstance getCmmnModelInstance(String caseDefinitionId) {
    try {
      return commandExecutor.execute(new GetDeploymentCmmnModelInstanceCmd(caseDefinitionId));

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (CmmnModelInstanceNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    } catch (DeploymentResourceNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }
  }

  public DmnModelInstance getDmnModelInstance(String decisionDefinitionId) {
    try {
      return commandExecutor.execute(new GetDeploymentDmnModelInstanceCmd(decisionDefinitionId));

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (DmnModelInstanceNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    } catch (DeploymentResourceNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }
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

  public CaseDefinition getCaseDefinition(String caseDefinitionId) {
    try {
      return commandExecutor.execute(new GetDeploymentCaseDefinitionCmd(caseDefinitionId));

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (CaseDefinitionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }
  }

  public InputStream getCaseModel(String caseDefinitionId) {
    try {
      return commandExecutor.execute(new GetDeploymentCaseModelCmd(caseDefinitionId));

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (CaseDefinitionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    } catch (DeploymentResourceNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }

  }

  public DecisionDefinition getDecisionDefinition(String decisionDefinitionId) {
    try {
      return commandExecutor.execute(new GetDeploymentDecisionDefinitionCmd(decisionDefinitionId));
    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);
    } catch (DecisionDefinitionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    }
  }

  public InputStream getDecisionModel(String decisionDefinitionId) {
    try {
      return commandExecutor.execute(new GetDeploymentDecisionModelCmd(decisionDefinitionId));
    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);
    } catch (DecisionDefinitionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    } catch (DeploymentResourceNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    }
  }

  public InputStream getDecisionDiagram(String decisionDefinitionId) {
    return commandExecutor.execute(new GetDeploymentDecisionDiagramCmd(decisionDefinitionId));
  }

}
