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
package org.camunda.bpm.engine.repository;

import org.camunda.bpm.dmn.engine.impl.spi.type.DmnTypeDefinition;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;

import java.util.Date;
import java.util.List;

/**
 * Represents a deployment that is already present in the process repository.
 *
 * A deployment is a container for resources such as process definitions, images, forms, etc.
 *
 * When a deployment is 'deployed' through the {@link org.camunda.bpm.engine.RepositoryService},
 * the engine will recognize certain of such resource types and act upon
 * them (e.g. process definitions will be parsed to an executable Java artifact).
 *
 * To create a Deployment, use the {@link org.camunda.bpm.engine.repository.DeploymentBuilder}.
 * A Deployment on itself is a <b>read-only</b> object and its content cannot be
 * changed after deployment (hence the builder that needs to be used).
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Christopher Zell
 */
public interface Deployment {

  String getId();

  String getName();

  Date getDeploymentTime();

  String getSource();

  /**
   * Returns the process definitions, which are deployed with that deployment.
   *
   * @return the process definitions which are deployed
   */
  List<ProcessDefinition> getDeployedProcessDefinitions();

  /**
   * Returns the case definitions, which are deployed with that deployment.
   *
   * @return the case definitions, which are deployed
   */
  List<CaseDefinition> getDeployedCaseDefinitions();

  /**
   * Returns the decision definitions, which are deployed with that deployment
   *
   * @return the decision definitions, which are deployed
   */
  List<DecisionDefinition> getDeployedDecisionDefinitions();

  /**
   * Returns the decision requirements definitions, which are deployed with that deployment
   *
   * @return the decision definitions, which are deployed
   */
  List<DecisionRequirementsDefinition> getDeployedDecisionRequirementsDefinitions();

  /**
   * Returns the id of the tenant this deployment belongs to. Can be <code>null</code>
   * if the deployment belongs to no single tenant.
   */
  String getTenantId();

}
