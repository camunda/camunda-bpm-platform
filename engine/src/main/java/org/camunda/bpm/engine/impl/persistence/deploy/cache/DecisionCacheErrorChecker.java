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
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author: Johannes Heinemann
 */
public class DecisionCacheErrorChecker implements CacheErrorChecker<DecisionDefinitionEntity> {

  @Override
  public void checkInvalidDefinitionId(String decisionDefinitionId) {
    ensureNotNull("Invalid decision definition id", "decisionDefinitionId", decisionDefinitionId);
  }

  @Override
  public void checkDefinitionFound(String decisionDefinitionId, DecisionDefinitionEntity decisionDefinition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no deployed decision definition found with id '" + decisionDefinitionId + "'", "decisionDefinition", decisionDefinition);
  }

  @Override
  public void checkInvalidDefinitionByKey(String decisionDefinitionKey, DecisionDefinitionEntity decisionDefinition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no decision definition deployed with key '" + decisionDefinitionKey + "'", "decisionDefinition", decisionDefinition);
  }

  @Override
  public void checkInvalidDefinitionByKeyAndTenantId(String decisionDefinitionKey, String tenantId, DecisionDefinitionEntity decisionDefinition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no decision definition deployed with key '" + decisionDefinitionKey + "' and tenant-id '" + tenantId + "'", "decisionDefinition", decisionDefinition);
  }

  public void checkInvalidDefinitionByKeyAndVersion(String decisionDefinitionKey, Integer decisionDefinitionVersion, DecisionDefinitionEntity decisionDefinition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no decision definition deployed with key = '" + decisionDefinitionKey + "' and version = '" + decisionDefinitionVersion + "'", "decisionDefinition", decisionDefinition);
  }

  @Override
  public void checkInvalidDefinitionByKeyVersionAndTenantId(String decisionDefinitionKey, Integer decisionDefinitionVersion, String tenantId, DecisionDefinitionEntity decisionDefinition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no decision definition deployed with key = '" + decisionDefinitionKey + "', version = '" + decisionDefinitionVersion + "' and tenant-id '" + tenantId + "'", "decisionDefinition", decisionDefinition);
  }

  @Override
  public void checkInvalidDefinitionByDeploymentAndKey(String deploymentId, String decisionDefinitionKey, DecisionDefinitionEntity decisionDefinition) {
    ensureNotNull(DecisionDefinitionNotFoundException.class, "no decision definition deployed with key = '" + decisionDefinitionKey + "' in deployment = '" + deploymentId + "'", "decisionDefinition", decisionDefinition);

  }

  @Override
  public void checkInvalidDefinitionWasCached(String deploymentId, String decisionDefinitionId, DecisionDefinitionEntity cachedDecisionDefinition) {
    ensureNotNull("deployment '" + deploymentId + "' didn't put decision definition '" + decisionDefinitionId + "' in the cache", "cachedDecisionDefinition", cachedDecisionDefinition);
  }
}
