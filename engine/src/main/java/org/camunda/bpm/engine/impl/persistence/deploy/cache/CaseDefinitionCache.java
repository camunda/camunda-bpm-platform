/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.AbstractResourceDefinitionManager;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author: Johannes Heinemann
 */
public class CaseDefinitionCache extends ResourceDefinitionCache<CaseDefinitionEntity> {

  public CaseDefinitionCache(CacheFactory factory, int cacheCapacity, CacheDeployer cacheDeployer) {
    super(factory, cacheCapacity, cacheDeployer);
  }

  public CaseDefinitionEntity getCaseDefinitionById(String caseDefinitionId) {
    checkInvalidDefinitionId(caseDefinitionId);
    CaseDefinitionEntity caseDefinition = getDefinition(caseDefinitionId);
    if (caseDefinition == null) {
      caseDefinition = findDeployedDefinitionById(caseDefinitionId);

    }
    return caseDefinition;
  }

  @Override
  protected AbstractResourceDefinitionManager<CaseDefinitionEntity> getManager() {
    return Context.getCommandContext().getCaseDefinitionManager();
  }

  @Override
  protected void checkInvalidDefinitionId(String definitionId) {
    ensureNotNull("Invalid case definition id", "caseDefinitionId", definitionId);
  }

  @Override
  protected void checkDefinitionFound(String definitionId, CaseDefinitionEntity definition) {
    ensureNotNull(CaseDefinitionNotFoundException.class, "no deployed case definition found with id '" + definitionId + "'", "caseDefinition", definition);
  }

  @Override
  protected void checkInvalidDefinitionByKey(String definitionKey, CaseDefinitionEntity definition) {
    ensureNotNull(CaseDefinitionNotFoundException.class, "no case definition deployed with key '" + definitionKey + "'", "caseDefinition", definition);
  }

  @Override
  protected void checkInvalidDefinitionByKeyAndTenantId(String definitionKey, String tenantId, CaseDefinitionEntity definition) {
    ensureNotNull(CaseDefinitionNotFoundException.class, "no case definition deployed with key '" + definitionKey + "' and tenant-id '" + tenantId + "'", "caseDefinition", definition);
  }

  @Override
  protected void checkInvalidDefinitionByKeyVersionAndTenantId(String definitionKey, Integer definitionVersion, String tenantId, CaseDefinitionEntity definition) {
    ensureNotNull(CaseDefinitionNotFoundException.class, "no case definition deployed with key = '" + definitionKey + "', version = '" + definitionVersion + "'"
        + " and tenant-id = '" + tenantId + "'", "caseDefinition", definition);
  }

  @Override
  protected void checkInvalidDefinitionByKeyVersionTagAndTenantId(String definitionKey, String definitionVersionTag, String tenantId, CaseDefinitionEntity definition) {
    throw new UnsupportedOperationException("Version tag is not implemented in case definition.");  }

  @Override
  protected void checkInvalidDefinitionByDeploymentAndKey(String deploymentId, String definitionKey, CaseDefinitionEntity definition) {
    ensureNotNull(CaseDefinitionNotFoundException.class, "no case definition deployed with key = '" + definitionKey + "' in deployment = '" + deploymentId + "'", "caseDefinition", definition);
  }

  @Override
  protected void checkInvalidDefinitionWasCached(String deploymentId, String definitionId, CaseDefinitionEntity definition) {
    ensureNotNull("deployment '" + deploymentId + "' didn't put case definition '" + definitionId + "' in the cache", "cachedCaseDefinition", definition);
  }
}
