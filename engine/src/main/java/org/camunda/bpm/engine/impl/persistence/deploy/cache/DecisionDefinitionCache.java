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
package org.camunda.bpm.engine.impl.persistence.deploy.cache;

import org.camunda.bpm.engine.exception.dmn.DecisionDefinitionNotFoundException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.AbstractResourceDefinitionManager;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author: Johannes Heinemann
 */
public class DecisionDefinitionCache extends ResourceDefinitionCache<DecisionDefinitionEntity> {


  public DecisionDefinitionCache(CacheFactory factory, int cacheCapacity, CacheDeployer cacheDeployer) {
    super(factory, cacheCapacity, cacheDeployer);
  }

  public DecisionDefinitionEntity findDeployedDefinitionByKeyAndVersion(String definitionKey, Integer definitionVersion) {
    DecisionDefinitionEntity definition = ((DecisionDefinitionManager) getManager())
        .findDecisionDefinitionByKeyAndVersion(definitionKey, definitionVersion);

    checkInvalidDefinitionByKeyAndVersion(definitionKey, definitionVersion, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  @Override
  protected AbstractResourceDefinitionManager<DecisionDefinitionEntity> getManager() {
    return Context.getCommandContext().getDecisionDefinitionManager();
  }

  @Override
  protected void checkInvalidDefinitionId(String definitionId) {
    ensureNotNull("Invalid decision definition id", "decisionDefinitionId", definitionId);
  }

  @Override
  protected void checkDefinitionFound(String definitionId, DecisionDefinitionEntity definition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no deployed decision definition found with id '" + definitionId + "'", "decisionDefinition", definition);
  }

  @Override
  protected void checkInvalidDefinitionByKey(String definitionKey, DecisionDefinitionEntity definition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no decision definition deployed with key '" + definitionKey + "'", "decisionDefinition", definition);
  }

  @Override
  protected void checkInvalidDefinitionByKeyAndTenantId(String definitionKey, String tenantId, DecisionDefinitionEntity definition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no decision definition deployed with key '" + definitionKey + "' and tenant-id '" + tenantId + "'", "decisionDefinition", definition);
  }

  protected void checkInvalidDefinitionByKeyAndVersion(String decisionDefinitionKey, Integer decisionDefinitionVersion, DecisionDefinitionEntity decisionDefinition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no decision definition deployed with key = '" + decisionDefinitionKey + "' and version = '" + decisionDefinitionVersion + "'", "decisionDefinition", decisionDefinition);
  }

  @Override
  protected void checkInvalidDefinitionByKeyVersionAndTenantId(String definitionKey, Integer definitionVersion, String tenantId, DecisionDefinitionEntity definition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no decision definition deployed with key = '" + definitionKey + "', version = '" + definitionVersion + "' and tenant-id '" + tenantId + "'", "decisionDefinition", definition);
  }

  @Override
  protected void checkInvalidDefinitionByKeyVersionTagAndTenantId(String definitionKey, String definitionVersionTag, String tenantId, DecisionDefinitionEntity definition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no decision definition deployed with key = '" + definitionKey + "', versionTag = '" + definitionVersionTag + "' and tenant-id '" + tenantId + "'", "decisionDefinition", definition);
  }

  @Override
  protected void checkInvalidDefinitionByDeploymentAndKey(String deploymentId, String definitionKey, DecisionDefinitionEntity definition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no decision definition deployed with key = '" + definitionKey + "' in deployment = '" + deploymentId + "'", "decisionDefinition", definition);
  }

  @Override
  protected void checkInvalidDefinitionWasCached(String deploymentId, String definitionId, DecisionDefinitionEntity definition) {
    ensureNotNull("deployment '" + deploymentId + "' didn't put decision definition '" + definitionId + "' in the cache", "cachedDecisionDefinition", definition);
  }
}
