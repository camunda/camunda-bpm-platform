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

import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionQueryImpl;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;

import java.io.InputStream;
import java.util.List;

/**
 * @author: Johannes Heinemann
 */
public class CmmnModelInstanceCache extends ModelInstanceCache<CmmnModelInstance, CaseDefinitionEntity> {

  public CmmnModelInstanceCache(CacheFactory factory, int cacheCapacity, ResourceDefinitionCache<CaseDefinitionEntity> definitionCache) {
    super(factory, cacheCapacity, definitionCache);
  }

  @Override
  protected void throwLoadModelException(String definitionId, Exception e) {
    throw LOG.loadModelException("CMMN", "case", definitionId, e);
  }

  @Override
  protected CmmnModelInstance readModelFromStream(InputStream cmmnResourceInputStream) {
    return Cmmn.readModelFromStream(cmmnResourceInputStream);
  }

  @Override
  protected void logRemoveEntryFromDeploymentCacheFailure(String definitionId, Exception e) {
    LOG.removeEntryFromDeploymentCacheFailure("case", definitionId, e);
  }

  @Override
  protected List<CaseDefinition> getAllDefinitionsForDeployment(String deploymentId) {
    return new CaseDefinitionQueryImpl()
        .deploymentId(deploymentId)
        .list();
  }
}
