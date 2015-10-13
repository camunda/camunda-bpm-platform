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

package org.camunda.bpm.engine;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.DiagramLayout;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
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


/** Service providing access to the repository of process definitions and deployments.
 *
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public interface RepositoryService {

  /**
   * Starts creating a new deployment
   */
  DeploymentBuilder createDeployment();

  /**
   * Starts creating a new {@link ProcessApplicationDeployment}.
   *
   * @see ProcessApplicationDeploymentBuilder
   */
  ProcessApplicationDeploymentBuilder createDeployment(ProcessApplicationReference processApplication);

  /**
   * Starts re-deployment of a deployment.
   */
  RedeploymentBuilder createRedeployment(String deploymentId);

  /**
   * Starts re-deployment of {@link ProcessApplicationDeployment}.
   *
   * @see ProcessApplicationRedeploymentBuilder
   */
  ProcessApplicationRedeploymentBuilder createRedeployment(String deploymentId, ProcessApplicationReference processApplicationReference);

  /**
   * Deletes the given deployment.
   *
   * @param deploymentId id of the deployment, cannot be null.
   *
   * @throws RuntimeException
   *          If there are still runtime or history process instances or jobs.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#DEPLOYMENT}.
   */
  void deleteDeployment(String deploymentId);

  /**
   * Deletes the given deployment and cascade deletion to process instances,
   * history process instances and jobs.
   *
   * @param deploymentId id of the deployment, cannot be null.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#DEPLOYMENT}.
   *
   * @deprecated use {@link #deleteDeployment(String, boolean)}. This methods may be deleted from 5.3.
   */
  void deleteDeploymentCascade(String deploymentId);

  /**
   * Deletes the given deployment and cascade deletion to process instances,
   * history process instances and jobs.
   *
   * @param deploymentId id of the deployment, cannot be null.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#DEPLOYMENT}.
   */
  void deleteDeployment(String deploymentId, boolean cascade);

  /**
   * Deletes the given deployment and cascade deletion to process instances,
   * history process instances and jobs.
   *
   * @param deploymentId id of the deployment, cannot be null.
   * @param cascade if set to true, all process instances (including) history are deleted
   * @param skipCustomListeners if true, only the built-in {@link ExecutionListener}s
   * are notified with the {@link ExecutionListener#EVENTNAME_END} event.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#DEPLOYMENT}.
   */
  void deleteDeployment(String deploymentId, boolean cascade, boolean skipCustomListeners);

  /**
   * Retrieves a list of deployment resource names for the given deployment,
   * ordered alphabetically.
   *
   * @param deploymentId id of the deployment, cannot be null.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#DEPLOYMENT}.
   */
  List<String> getDeploymentResourceNames(String deploymentId);

  /**
   * Retrieves a list of deployment resources for the given deployment,
   * ordered alphabetically by name.
   *
   * @param deploymentId id of the deployment, cannot be null.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#DEPLOYMENT}.
   */
  List<Resource> getDeploymentResources(String deploymentId);

  /**
   * Gives access to a deployment resource through a stream of bytes.
   *
   * @param deploymentId id of the deployment, cannot be null.
   * @param resourceName name of the resource, cannot be null.
   *
   * @throws ProcessEngineException
   *          When the resource doesn't exist in the given deployment or when no deployment exists
   *          for the given deploymentId.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#DEPLOYMENT}.
   */
  InputStream getResourceAsStream(String deploymentId, String resourceName);

  /**
   * Gives access to a deployment resource through a stream of bytes.
   *
   * @param deploymentId id of the deployment, cannot be null.
   * @param resourceId id of the resource, cannot be null.
   *
   * @throws ProcessEngineException
   *          When the resource doesn't exist in the given deployment or when no deployment exists
   *          for the given deploymentId.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#DEPLOYMENT}.
   */
  InputStream getResourceAsStreamById(String deploymentId, String resourceId);

  /**
   * Query process definitions.
   */
  ProcessDefinitionQuery createProcessDefinitionQuery();

  /**
   * Query case definitions.
   */
  CaseDefinitionQuery createCaseDefinitionQuery();

  /**
   * Query decision definitions.
   */
  DecisionDefinitionQuery createDecisionDefinitionQuery();

  /**
   * Query process definitions.
   */
  DeploymentQuery createDeploymentQuery();

  /**
   * Suspends the process definition with the given id.
   *
   * If a process definition is in state suspended, it will not be possible to start new process instances
   * based on the process definition.
   *
   * <strong>Note: all the process instances of the process definition will still be active
   * (ie. not suspended)!</strong>
   *
   * @throws ProcessEngineException
   *          If no such processDefinition can be found.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void suspendProcessDefinitionById(String processDefinitionId);

  /**
   * Suspends the process definition with the given id.
   *
   * If a process definition is in state suspended, it will not be possible to start new process instances
   * based on the process definition.
   *
   * @param suspendProcessInstances If true, all the process instances of the provided process definition
   *                                will be suspended too.
   * @param suspensionDate The date on which the process definition will be suspended. If null, the
   *                       process definition is suspended immediately.
   *                       Note: The job executor needs to be active to use this!
   *
   * @throws ProcessEngineException
   *          If no such processDefinition can be found.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}
   *          and if <code>suspendProcessInstances</code> is set to <code>true</code> and the user have no
   *          {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE} or no
   *          {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @see RuntimeService#suspendProcessInstanceById(String)
   */
  void suspendProcessDefinitionById(String processDefinitionId, boolean suspendProcessInstances, Date suspensionDate);

  /**
   * Suspends the <strong>all</strong> process definitions with the given key (= id in the bpmn20.xml file).
   *
   * If a process definition is in state suspended, it will not be possible to start new process instances
   * based on the process definition.
   *
   * <strong>Note: all the process instances of the process definition will still be active
   * (ie. not suspended)!</strong>
   *
   * @throws ProcessEngineException
   *          If no such processDefinition can be found.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void suspendProcessDefinitionByKey(String processDefinitionKey);

  /**
   * Suspends the <strong>all</strong> process definitions with the given key (= id in the bpmn20.xml file).
   *
   * If a process definition is in state suspended, it will not be possible to start new process instances
   * based on the process definition.
   *
   * @param suspendProcessInstances If true, all the process instances of the provided process definition
   *                                will be suspended too.
   * @param suspensionDate The date on which the process definition will be suspended. If null, the
   *                       process definition is suspended immediately.
   *                       Note: The job executor needs to be active to use this!
   *
   * @throws ProcessEngineException
   *          If no such processDefinition can be found.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}
   *          and if <code>suspendProcessInstances</code> is set to <code>true</code> and the user have no
   *          {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE} or no
   *          {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @see RuntimeService#suspendProcessInstanceById(String)
   */
  void suspendProcessDefinitionByKey(String processDefinitionKey, boolean suspendProcessInstances, Date suspensionDate);

  /**
   * Activates the process definition with the given id.
   *
   * @throws ProcessEngineException
   *          If no such processDefinition can be found or if the process definition is already in state active.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void activateProcessDefinitionById(String processDefinitionId);

  /**
   * Activates the process definition with the given id.
   *
   * @param suspendProcessInstances If true, all the process instances of the provided process definition
   *                                will be activated too.
   * @param activationDate The date on which the process definition will be activated. If null, the
   *                       process definition is suspended immediately.
   *                       Note: The job executor needs to be active to use this!
   *
   * @throws ProcessEngineException
   *          If no such processDefinition can be found.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}
   *          and if <code>activateProcessInstances</code> is set to <code>true</code> and the user have no
   *          {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE} or no
   *          {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @see RuntimeService#activateProcessInstanceById(String)
   */
  void activateProcessDefinitionById(String processDefinitionId, boolean activateProcessInstances, Date activationDate);

  /**
   * Activates the process definition with the given key (=id in the bpmn20.xml file).
   *
   * @throws ProcessEngineException
   *          If no such processDefinition can be found.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void activateProcessDefinitionByKey(String processDefinitionKey);

  /**
   * Activates the process definition with the given key (=id in the bpmn20.xml file).
   *
   * @param suspendProcessInstances If true, all the process instances of the provided process definition
   *                                will be activated too.
   * @param activationDate The date on which the process definition will be activated. If null, the
   *                       process definition is suspended immediately.
   *                       Note: The job executor needs to be active to use this!
   *
   * @throws ProcessEngineException
   *          If no such processDefinition can be found.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}
   *          and if <code>activateProcessInstances</code> is set to <code>true</code> and the user have no
   *          {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE} or no
   *          {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @see RuntimeService#activateProcessInstanceById(String)
   */
  void activateProcessDefinitionByKey(String processDefinitionKey, boolean activateProcessInstances,  Date activationDate);

  /**
   * Gives access to a deployed process model, e.g., a BPMN 2.0 XML file,
   * through a stream of bytes.
   *
   * @param processDefinitionId
   *          id of a {@link ProcessDefinition}, cannot be null.
   *
   * @throws ProcessEngineException
   *           when the process model doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  InputStream getProcessModel(String processDefinitionId);

  /**
   * Gives access to a deployed process diagram, e.g., a PNG image, through a
   * stream of bytes.
   *
   * @param processDefinitionId
   *          id of a {@link ProcessDefinition}, cannot be null.
   * @return null when the diagram resource name of a {@link ProcessDefinition} is null.
   *
   * @throws ProcessEngineException
   *           when the process diagram doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  InputStream getProcessDiagram(String processDefinitionId);

  /**
   * Returns the {@link ProcessDefinition} including all BPMN information like additional
   * Properties (e.g. documentation).
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessDefinition getProcessDefinition(String processDefinitionId);

  /**
   * Provides positions and dimensions of elements in a process diagram as
   * provided by {@link RepositoryService#getProcessDiagram(String)}.
   *
   * This method requires a process model and a diagram image to be deployed.
   *
   * @param processDefinitionId id of a {@link ProcessDefinition}, cannot be null.
   * @return Map with process element ids as keys and positions and dimensions as values.
   *
   * @return null when the input stream of a process diagram is null.
   *
   * @throws ProcessEngineException
   *          When the process model or diagram doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  DiagramLayout getProcessDiagramLayout(String processDefinitionId);

  /**
   * Returns the {@link BpmnModelInstance} for the given processDefinitionId.
   *
   * @param processDefinitionId the id of the Process Definition for which the {@link BpmnModelInstance}
   *  should be retrieved.
   *
   * @return the {@link BpmnModelInstance}
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  BpmnModelInstance getBpmnModelInstance(String processDefinitionId);

  /**
   * Returns the {@link CmmnModelInstance} for the given caseDefinitionId.
   *
   * @param caseDefinitionId the id of the Case Definition for which the {@link CmmnModelInstance}
   *  should be retrieved.
   *
   * @return the {@link CmmnModelInstance}
   *
   * @throws NotValidException when the given case definition id or deployment id or resource name is null
   * @throws NotFoundException when no CMMN model instance or deployment resource is found for the given
   *     case definition id
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  CmmnModelInstance getCmmnModelInstance(String caseDefinitionId);

  /**
   * Returns the {@link DmnModelInstance} for the given decisionDefinitionId.
   *
   * @param decisionDefinitionId the id of the Decision Definition for which the {@link DmnModelInstance}
   *  should be retrieved.
   *
   * @return the {@link DmnModelInstance}
   *
   * @throws NotValidException when the given decision definition id or deployment id or resource name is null
   * @throws NotFoundException when no DMN model instance or deployment resource is found for the given
   *     decision definition id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command.
   */
  DmnModelInstance getDmnModelInstance(String decisionDefinitionId);

  /**
   * Authorizes a candidate user for a process definition.
   *
   * @param processDefinitionId id of the process definition, cannot be null.
   * @param userId id of the user involve, cannot be null.
   *
   * @throws ProcessEngineException
   *          When the process definition or user doesn't exist.
   *
   * @deprecated Use authorization mechanism instead.
   *
   */
  void addCandidateStarterUser(String processDefinitionId, String userId);

  /**
   * Authorizes a candidate group for a process definition.
   *
   * @param processDefinitionId id of the process definition, cannot be null.
   * @param groupId id of the group involve, cannot be null.
   *
   * @throws ProcessEngineException
   *          When the process definition or group doesn't exist.
   *
   * @deprecated Use authorization mechanism instead.
   *
   */
  void addCandidateStarterGroup(String processDefinitionId, String groupId);

  /**
   * Removes the authorization of a candidate user for a process definition.
   *
   * @param processDefinitionId id of the process definition, cannot be null.
   * @param userId id of the user involve, cannot be null.
   *
   * @throws ProcessEngineException
   *          When the process definition or user doesn't exist.
   *
   * @deprecated Use authorization mechanism instead.
   *
   */
  void deleteCandidateStarterUser(String processDefinitionId, String userId);

  /**
   * Removes the authorization of a candidate group for a process definition.
   *
   * @param processDefinitionId id of the process definition, cannot be null.
   * @param groupId id of the group involve, cannot be null.
   *
   * @throws ProcessEngineException
   *          When the process definition or group doesn't exist.
   *
   * @deprecated Use authorization mechanism instead.
   *
   */
  void deleteCandidateStarterGroup(String processDefinitionId, String groupId);

  /**
   * Retrieves the {@link IdentityLink}s associated with the given process definition.
   * Such an {@link IdentityLink} informs how a certain identity (eg. group or user)
   * is authorized for a certain process definition
   *
   * @deprecated Use authorization mechanism instead.
   *
   */
  List<IdentityLink> getIdentityLinksForProcessDefinition(String processDefinitionId);

  /**
   * Returns the {@link CaseDefinition}.
   *
   * @throws NotValidException when the given case definition id is null
   * @throws NotFoundException when no case definition is found for the given case definition id
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  CaseDefinition getCaseDefinition(String caseDefinitionId);

  /**
   * Gives access to a deployed case model, e.g., a CMMN 1.0 XML file,
   * through a stream of bytes.
   *
   * @param caseDefinitionId
   *          id of a {@link CaseDefinition}, cannot be null.
   *
   * @throws NotValidException when the given case definition id or deployment id or resource name is null
   * @throws NotFoundException when no case definition or deployment resource is found for the given case definition id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  InputStream getCaseModel(String caseDefinitionId);

  /**
   * Gives access to a deployed case diagram, e.g., a PNG image, through a
   * stream of bytes.
   *
   * @param caseDefinitionId id of a {@link CaseDefinition}, cannot be null.
   * @return null when the diagram resource name of a {@link CaseDefinition} is null.
   * @throws ProcessEngineException when the process diagram doesn't exist.
   */
  InputStream getCaseDiagram(String caseDefinitionId);

  /**
   * Returns the {@link DecisionDefinition}.
   *
   * @throws NotValidException when the given decision definition id is null
   * @throws NotFoundException when no decision definition is found for the given decision definition id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command.
   */
  DecisionDefinition getDecisionDefinition(String decisionDefinitionId);

  /**
   * Gives access to a deployed decision model, e.g., a DMN 1.0 XML file,
   * through a stream of bytes.
   *
   * @param decisionDefinitionId
   *          id of a {@link DecisionDefinition}, cannot be null.
   *
   * @throws NotValidException when the given decision definition id or deployment id or resource name is null
   * @throws NotFoundException when no decision definition or deployment resource is found for the given decision definition id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  InputStream getDecisionModel(String decisionDefinitionId);

  /**
   * Gives access to a deployed decision diagram, e.g., a PNG image, through a
   * stream of bytes.
   *
   * @param decisionDefinitionId id of a {@link DecisionDefinition}, cannot be null.
   * @return null when the diagram resource name of a {@link DecisionDefinition} is null.
   * @throws ProcessEngineException when the process diagram doesn't exist.
   */
  InputStream getDecisionDiagram(String decisionDefinitionId);

}

