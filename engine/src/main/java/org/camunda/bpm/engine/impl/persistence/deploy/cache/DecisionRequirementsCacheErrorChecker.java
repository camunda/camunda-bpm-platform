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

import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author: Johannes Heinemann
 */
public class DecisionRequirementsCacheErrorChecker implements CacheErrorChecker<DecisionRequirementsDefinitionEntity> {
  @Override
  public void checkInvalidDefinitionId(String decisionRequirementsDefinitionId) {
    ensureNotNull("Invalid decision requirements definition id", "decisionRequirementsDefinitionId", decisionRequirementsDefinitionId);
  }

  @Override
  public void checkDefinitionFound(String decisionRequirementsDefinitionId, DecisionRequirementsDefinitionEntity decisionRequirementsDefinition) {
    ensureNotNull("no deployed decision requirements definition found with id '" + decisionRequirementsDefinitionId + "'",
        "decisionRequirementsDefinition", decisionRequirementsDefinition);
  }

  @Override
  public void checkInvalidDefinitionByKey(String definitionKey, DecisionRequirementsDefinitionEntity definition) {

  }

  @Override
  public void checkInvalidDefinitionByKeyAndTenantId(String definitionKey, String tenantId, DecisionRequirementsDefinitionEntity definition) {

  }

  @Override
  public void checkInvalidDefinitionByKeyVersionAndTenantId(String definitionKey, Integer definitionVersion, String tenantId, DecisionRequirementsDefinitionEntity definition) {

  }

  @Override
  public void checkInvalidDefinitionByDeploymentAndKey(String deploymentId, String definitionKey, DecisionRequirementsDefinitionEntity definition) {

  }

  @Override
  public void checkInvalidDefinitionWasCached(String deploymentId, String decisionRequirementsDefinitionId, DecisionRequirementsDefinitionEntity cachedDecisionRequirementsDefinition) {
    ensureNotNull("deployment '" + deploymentId + "' didn't put decision requirements definition '" + decisionRequirementsDefinitionId + "' in the cache", "cachedDecisionRequirementsDefinition", cachedDecisionRequirementsDefinition);
  }
}
