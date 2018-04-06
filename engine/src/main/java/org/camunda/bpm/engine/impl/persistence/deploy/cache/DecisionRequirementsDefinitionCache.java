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

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.AbstractResourceDefinitionManager;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author: Johannes Heinemann
 */
public class DecisionRequirementsDefinitionCache extends ResourceDefinitionCache<DecisionRequirementsDefinitionEntity> {

  public DecisionRequirementsDefinitionCache(CacheFactory factory, int cacheCapacity, CacheDeployer cacheDeployer) {
    super(factory, cacheCapacity, cacheDeployer);
  }

  @Override
  protected AbstractResourceDefinitionManager<DecisionRequirementsDefinitionEntity> getManager() {
    return Context.getCommandContext().getDecisionRequirementsDefinitionManager();
  }

  @Override
  protected void checkInvalidDefinitionId(String definitionId) {
    ensureNotNull("Invalid decision requirements definition id", "decisionRequirementsDefinitionId", definitionId);
  }

  @Override
  protected void checkDefinitionFound(String definitionId, DecisionRequirementsDefinitionEntity definition) {
    ensureNotNull("no deployed decision requirements definition found with id '" + definitionId + "'",
        "decisionRequirementsDefinition", definition);
  }

  @Override
  protected void checkInvalidDefinitionByKey(String definitionKey, DecisionRequirementsDefinitionEntity definition) {
    // not needed
  }

  @Override
  protected void checkInvalidDefinitionByKeyAndTenantId(String definitionKey, String tenantId, DecisionRequirementsDefinitionEntity definition) {
    // not needed
  }

  @Override
  protected void checkInvalidDefinitionByKeyVersionAndTenantId(String definitionKey, Integer definitionVersion, String tenantId, DecisionRequirementsDefinitionEntity definition) {
    // not needed
  }

  @Override
  protected void checkInvalidDefinitionByKeyVersionTagAndTenantId(String definitionKey, String definitionVersionTag, String tenantId, DecisionRequirementsDefinitionEntity definition) {
    // not needed
  }

  @Override
  protected void checkInvalidDefinitionByDeploymentAndKey(String deploymentId, String definitionKey, DecisionRequirementsDefinitionEntity definition) {
    // not needed
  }

  @Override
  protected void checkInvalidDefinitionWasCached(String deploymentId, String definitionId, DecisionRequirementsDefinitionEntity definition) {
    ensureNotNull("deployment '" + deploymentId + "' didn't put decision requirements definition '" + definitionId + "' in the cache", "cachedDecisionRequirementsDefinition", definition);
  }
}
