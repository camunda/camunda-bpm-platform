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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractResourceDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.commons.utils.cache.Cache;

import java.util.concurrent.Callable;


/**
 * @author: Johannes Heinemann
 */
public abstract class ResourceDefinitionCache<T extends ResourceDefinitionEntity> {

  protected Cache<String, T> cache;
  protected CacheDeployer cacheDeployer;

  public ResourceDefinitionCache(CacheFactory factory, int cacheCapacity, CacheDeployer cacheDeployer) {
    this.cache = factory.createCache(cacheCapacity);
    this.cacheDeployer = cacheDeployer;
  }

  public T findDefinitionFromCache(String definitionId) {
    return cache.get(definitionId);
  }

  public T findDeployedDefinitionById(String definitionId) {
    checkInvalidDefinitionId(definitionId);
    T definition = getManager().getCachedResourceDefinitionEntity(definitionId);
    if (definition == null) {
      definition = getManager()
          .findLatestDefinitionById(definitionId);
    }

    checkDefinitionFound(definitionId, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  /**
   * @return the latest version of the definition with the given key (from any tenant)
   * @throws ProcessEngineException if more than one tenant has a definition with the given key
   */
  public T findDeployedLatestDefinitionByKey(String definitionKey) {
    T definition = getManager()
        .findLatestDefinitionByKey(definitionKey);
    checkInvalidDefinitionByKey(definitionKey, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  public T findDeployedLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId) {
    T definition = getManager()
        .findLatestDefinitionByKeyAndTenantId(definitionKey, tenantId);
    checkInvalidDefinitionByKeyAndTenantId(definitionKey, tenantId, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  public T findDeployedDefinitionByKeyVersionAndTenantId(final String definitionKey, final Integer definitionVersion, final String tenantId) {
    final CommandContext commandContext = Context.getCommandContext();
    T definition = commandContext.runWithoutAuthorization(new Callable<T>() {
      public T call() throws Exception {
        return getManager().findDefinitionByKeyVersionAndTenantId(definitionKey, definitionVersion, tenantId);
      }
    });
    checkInvalidDefinitionByKeyVersionAndTenantId(definitionKey, definitionVersion, tenantId, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  public T findDeployedDefinitionByKeyVersionTagAndTenantId(final String definitionKey, final String definitionVersionTag, final String tenantId) {
    final CommandContext commandContext = Context.getCommandContext();
    T definition = commandContext.runWithoutAuthorization(new Callable<T>() {
      public T call() throws Exception {
        return getManager().findDefinitionByKeyVersionTagAndTenantId(definitionKey, definitionVersionTag, tenantId);
      }
    });
    checkInvalidDefinitionByKeyVersionTagAndTenantId(definitionKey, definitionVersionTag, tenantId, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  public T findDeployedDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    T definition = getManager().findDefinitionByDeploymentAndKey(deploymentId, definitionKey);
    checkInvalidDefinitionByDeploymentAndKey(deploymentId, definitionKey, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  public T resolveDefinition(T definition) {
    String definitionId = definition.getId();
    String deploymentId = definition.getDeploymentId();
    T cachedDefinition = cache.get(definitionId);
    if (cachedDefinition == null) {
      synchronized (this) {
        cachedDefinition = cache.get(definitionId);
        if (cachedDefinition == null) {
          DeploymentEntity deployment = Context
              .getCommandContext()
              .getDeploymentManager()
              .findDeploymentById(deploymentId);
          deployment.setNew(false);
          cacheDeployer.deployOnlyGivenResourcesOfDeployment(deployment, definition.getResourceName(), definition.getDiagramResourceName());
          cachedDefinition = cache.get(definitionId);
        }
      }
      checkInvalidDefinitionWasCached(deploymentId, definitionId, cachedDefinition);
    }
    if (cachedDefinition != null) {
      cachedDefinition.updateModifiableFieldsFromEntity(definition);
    }
    return cachedDefinition;
  }

  public void addDefinition(T definition) {
    cache.put(definition.getId(), definition);
  }

  public T getDefinition(String id) {
    return cache.get(id);
  }

  public void removeDefinitionFromCache(String id) {
    cache.remove(id);
  }

  public void clear() {
    cache.clear();
  }

  public Cache<String, T> getCache() {
    return cache;
  }

  protected abstract AbstractResourceDefinitionManager<T> getManager();

  protected abstract void checkInvalidDefinitionId(String definitionId);

  protected abstract void checkDefinitionFound(String definitionId, T definition);

  protected abstract void checkInvalidDefinitionByKey(String definitionKey, T definition);

  protected abstract void checkInvalidDefinitionByKeyAndTenantId(String definitionKey, String tenantId, T definition);

  protected abstract void checkInvalidDefinitionByKeyVersionAndTenantId(String definitionKey, Integer definitionVersion, String tenantId, T definition);

  protected abstract void checkInvalidDefinitionByKeyVersionTagAndTenantId(String definitionKey, String definitionVersionTag, String tenantId, T definition);

  protected abstract void checkInvalidDefinitionByDeploymentAndKey(String deploymentId, String definitionKey, T definition);

  protected abstract void checkInvalidDefinitionWasCached(String deploymentId, String definitionId, T definition);

}