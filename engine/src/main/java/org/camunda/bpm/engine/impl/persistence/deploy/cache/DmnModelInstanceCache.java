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

import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionQueryImpl;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;

import java.io.InputStream;
import java.util.List;

/**
 * @author: Johannes Heinemann
 */
public class DmnModelInstanceCache extends ModelInstanceCache<DmnModelInstance, DecisionDefinitionEntity> {

  public DmnModelInstanceCache(CacheFactory factory, int cacheCapacity, ResourceDefinitionCache<DecisionDefinitionEntity> definitionCache) {
    super(factory, cacheCapacity, definitionCache);
  }

  @Override
  protected void throwLoadModelException(String definitionId, Exception e) {
    throw LOG.loadModelException("DMN", "decision", definitionId, e);
  }

  @Override
  protected DmnModelInstance readModelFromStream(InputStream cmmnResourceInputStream) {
    return Dmn.readModelFromStream(cmmnResourceInputStream);
  }

  @Override
  protected void logRemoveEntryFromDeploymentCacheFailure(String definitionId, Exception e) {
    LOG.removeEntryFromDeploymentCacheFailure("decision", definitionId, e);
  }

  @Override
  protected List<DecisionDefinition> getAllDefinitionsForDeployment(String deploymentId) {
    return new DecisionDefinitionQueryImpl()
        .deploymentId(deploymentId)
        .list();
  }
}
