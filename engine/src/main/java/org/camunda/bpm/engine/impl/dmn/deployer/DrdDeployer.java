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

package org.camunda.bpm.engine.impl.dmn.deployer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransformer;
import org.camunda.bpm.engine.impl.AbstractDefinitionDeployer;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.core.model.Properties;
import org.camunda.bpm.engine.impl.dmn.DecisionLogger;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionManager;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;

/**
 * {@link Deployer} responsible to parse DMN 1.1 XML files and create the proper
 * {@link DecisionRequirementDefinitionEntity}s.
 */
public class DrdDeployer extends AbstractDefinitionDeployer<DecisionRequirementDefinitionEntity> {

  protected static final DecisionLogger LOG = ProcessEngineLogger.DECISION_LOGGER;

  protected DmnTransformer transformer;

  @Override
  protected String[] getResourcesSuffixes() {
    // since the DmnDeployer uses the result of this deployer, make sure that
    // it process the same DMN resources
    return DmnDeployer.DMN_RESOURCE_SUFFIXES;
  }

  @Override
  protected List<DecisionRequirementDefinitionEntity> transformDefinitions(DeploymentEntity deployment, ResourceEntity resource, Properties properties) {
    byte[] bytes = resource.getBytes();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

    try {
      DecisionRequirementDefinitionEntity drd = transformer
          .createTransform()
          .modelInstance(inputStream)
          .transformDecisionRequirementDiagram();

      return Collections.singletonList(drd);

    } catch (Exception e) {
      throw LOG.exceptionParseDmnResource(resource.getName(), e);
    }
  }

  @Override
  protected DecisionRequirementDefinitionEntity findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    return getDecisionDefinitionManager().findDecisionRequirementDefinitionByDeploymentAndKey(deploymentId, definitionKey);
  }

  @Override
  protected DecisionRequirementDefinitionEntity findLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId) {
    return getDecisionDefinitionManager().findLatestDecisionRequirementDefinitionByKeyAndTenantId(definitionKey, tenantId);
  }

  @Override
  protected void persistDefinition(DecisionRequirementDefinitionEntity definition) {
    if (isDecisionRequirementDefinitionPersistable(definition)) {
      getDecisionDefinitionManager().insertDecisionRequirementDefinition(definition);
    }
  }

  @Override
  protected void addDefinitionToDeploymentCache(DeploymentCache deploymentCache, DecisionRequirementDefinitionEntity definition) {
    if (isDecisionRequirementDefinitionPersistable(definition)) {
      deploymentCache.addDecisionRequirementDefinition(definition);
    }
  }

  @Override
  protected void ensureNoDuplicateDefinitionKeys(List<DecisionRequirementDefinitionEntity> definitions) {
    // ignore decision requirement definitions which will not be persistent
    ArrayList<DecisionRequirementDefinitionEntity> persistableDefinitions = new ArrayList<DecisionRequirementDefinitionEntity>();
    for (DecisionRequirementDefinitionEntity definition : definitions) {
      if (isDecisionRequirementDefinitionPersistable(definition)) {
        persistableDefinitions.add(definition);
      }
    }

    super.ensureNoDuplicateDefinitionKeys(persistableDefinitions);
  }

  public static boolean isDecisionRequirementDefinitionPersistable(DecisionRequirementDefinitionEntity definition) {
    // persist no decision requirement definition for a single decision
    return definition.getDecisions().size() > 1;
  }

  @Override
  protected void updateDefinitionByPersistedDefinition(DeploymentEntity deployment, DecisionRequirementDefinitionEntity definition,
      DecisionRequirementDefinitionEntity persistedDefinition) {
    // cannot update the definition if it is not persistent
    if (persistedDefinition != null) {
      super.updateDefinitionByPersistedDefinition(deployment, definition, persistedDefinition);
    }
  }

  //context ///////////////////////////////////////////////////////////////////////////////////////////

  protected DecisionDefinitionManager getDecisionDefinitionManager() {
    return getCommandContext().getDecisionDefinitionManager();
  }

  // getters/setters ///////////////////////////////////////////////////////////////////////////////////

  public DmnTransformer getTransformer() {
    return transformer;
  }

  public void setTransformer(DmnTransformer transformer) {
    this.transformer = transformer;
  }

}
