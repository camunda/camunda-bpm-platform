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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourceCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.commons.utils.cache.Cache;

import java.io.InputStream;
import java.util.List;

/**
 * @author: Johannes Heinemann
 */
public abstract class ModelInstanceCache<InstanceType extends ModelInstance, DefinitionType extends ResourceDefinitionEntity> {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected Cache<String, InstanceType> instanceCache;
  protected ResourceDefinitionCache<DefinitionType> definitionCache;

  public ModelInstanceCache(CacheFactory factory, int cacheCapacity, ResourceDefinitionCache<DefinitionType> definitionCache) {
    this.instanceCache = factory.createCache(cacheCapacity);
    this.definitionCache = definitionCache;
  }

  public InstanceType findBpmnModelInstanceForDefinition(DefinitionType definitionEntity) {
    InstanceType bpmnModelInstance = instanceCache.get(definitionEntity.getId());
    if (bpmnModelInstance == null) {
      bpmnModelInstance = loadAndCacheBpmnModelInstance(definitionEntity);
    }
    return bpmnModelInstance;
  }

  public InstanceType findBpmnModelInstanceForDefinition(String definitionId) {
    InstanceType bpmnModelInstance = instanceCache.get(definitionId);
    if (bpmnModelInstance == null) {
      DefinitionType definition = definitionCache.findDeployedDefinitionById(definitionId);
      bpmnModelInstance = loadAndCacheBpmnModelInstance(definition);
    }
    return bpmnModelInstance;
  }

  protected InstanceType loadAndCacheBpmnModelInstance(final DefinitionType definitionEntity) {
    final CommandContext commandContext = Context.getCommandContext();
    InputStream bpmnResourceInputStream = commandContext.runWithoutAuthorization(
        new GetDeploymentResourceCmd(definitionEntity.getDeploymentId(), definitionEntity.getResourceName()));

    try {
      InstanceType bpmnModelInstance = readModelFromStream(bpmnResourceInputStream);
      instanceCache.put(definitionEntity.getId(), bpmnModelInstance);
      return bpmnModelInstance;
    } catch (Exception e) {
      throwLoadModelException(definitionEntity.getId(), e);
    }
    return null;
  }

  public void removeAllDefinitionsByDeploymentId(final String deploymentId) {
    // remove all definitions for a specific deployment
    List<? extends ResourceDefinition> allDefinitionsForDeployment = getAllDefinitionsForDeployment(deploymentId);
    for (ResourceDefinition definition : allDefinitionsForDeployment) {
      try {
        instanceCache.remove(definition.getId());
        definitionCache.removeDefinitionFromCache(definition.getId());

      } catch (Exception e) {
        logRemoveEntryFromDeploymentCacheFailure(definition.getId(), e);
      }
    }
  }

  public void remove(String definitionId) {
    instanceCache.remove(definitionId);
  }

  public void clear() {
    instanceCache.clear();
  }

  public Cache<String, InstanceType> getCache() {
    return instanceCache;
  }

  protected abstract void throwLoadModelException(String definitionId, Exception e);

  protected abstract void logRemoveEntryFromDeploymentCacheFailure(String definitionId, Exception e);

  protected abstract InstanceType readModelFromStream(InputStream stream);

  protected abstract List<? extends ResourceDefinition> getAllDefinitionsForDeployment(String deploymentId);
}
