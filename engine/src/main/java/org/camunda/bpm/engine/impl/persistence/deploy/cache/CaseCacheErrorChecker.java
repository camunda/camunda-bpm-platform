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

import org.camunda.bpm.engine.exception.cmmn.CaseDefinitionNotFoundException;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author: Johannes Heinemann
 */
public class CaseCacheErrorChecker implements CacheErrorChecker<CaseDefinitionEntity> {

  @Override
  public void checkInvalidDefinitionId(String caseDefinitionId) {
    ensureNotNull("Invalid case definition id", "caseDefinitionId", caseDefinitionId);
  }

  @Override
  public void checkDefinitionFound(String caseDefinitionId, CaseDefinitionEntity caseDefinition) {
    ensureNotNull(CaseDefinitionNotFoundException.class, "no deployed case definition found with id '" + caseDefinitionId + "'", "caseDefinition", caseDefinition);
  }

  @Override
  public void checkInvalidDefinitionByKey(String caseDefinitionKey, CaseDefinitionEntity caseDefinition) {
    ensureNotNull(CaseDefinitionNotFoundException.class, "no case definition deployed with key '" + caseDefinitionKey + "'", "caseDefinition", caseDefinition);
  }

  @Override
  public void checkInvalidDefinitionByKeyAndTenantId(String caseDefinitionKey, String tenantId, CaseDefinitionEntity caseDefinition) {
    ensureNotNull(CaseDefinitionNotFoundException.class, "no case definition deployed with key '" + caseDefinitionKey + "' and tenant-id '" + tenantId + "'", "caseDefinition", caseDefinition);
  }

  @Override
  public void checkInvalidDefinitionByKeyVersionAndTenantId(String caseDefinitionKey, Integer caseDefinitionVersion, String tenantId, CaseDefinitionEntity caseDefinition) {
    ensureNotNull(CaseDefinitionNotFoundException.class, "no case definition deployed with key = '" + caseDefinitionKey + "', version = '" + caseDefinitionVersion + "'"
        + " and tenant-id = '" + tenantId + "'", "caseDefinition", caseDefinition);
  }

  @Override
  public void checkInvalidDefinitionByDeploymentAndKey(String deploymentId, String caseDefinitionKey, CaseDefinitionEntity caseDefinition) {
    ensureNotNull(CaseDefinitionNotFoundException.class, "no case definition deployed with key = '" + caseDefinitionKey + "' in deployment = '" + deploymentId + "'", "caseDefinition", caseDefinition);
  }

  @Override
  public void checkInvalidDefinitionWasCached(String deploymentId, String caseDefinitionId, CaseDefinitionEntity cachedCaseDefinition) {
    ensureNotNull("deployment '" + deploymentId + "' didn't put case definition '" + caseDefinitionId + "' in the cache", "cachedCaseDefinition", cachedCaseDefinition);
  }
}
