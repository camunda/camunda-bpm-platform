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
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import org.camunda.commons.utils.cache.Cache;

import java.util.concurrent.Callable;


/**
 * @author: Johannes Heinemann
 */
public abstract class ResourceDefinitionCache<T extends ResourceDefinition> {

  protected Cache<String, T> cache;
  protected CacheDeployer cacheDeployer;
  protected DefinitionManagerFactory<T> managerFactory;
  protected CacheErrorChecker<T> errorChecker;

  public ResourceDefinitionCache(CacheFactory factory, int cacheCapacity, CacheDeployer cacheDeployer) {
    this.cache = factory.createCache(cacheCapacity);
    this.cacheDeployer = cacheDeployer;
    this.managerFactory = createDefinitionManagerFactory();
    this.errorChecker = createErrorChecker();
  }

  protected abstract CacheErrorChecker<T> createErrorChecker();
  protected abstract DefinitionManagerFactory<T> createDefinitionManagerFactory();

  public T findDefinitionFromCache(String definitionId) {
    return cache.get(definitionId);
  }

  public T findDeployedDefinitionById(String definitionId) {
    errorChecker.checkInvalidDefinitionId(definitionId);
    T definition = managerFactory.getManager().getCachedResourceDefinitionEntity(definitionId);
    if (definition == null) {
      definition = managerFactory.getManager()
          .findLatestDefinitionById(definitionId);
    }

    errorChecker.checkDefinitionFound(definitionId, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  /**
   * @return the latest version of the definition with the given key (from any tenant)
   * @throws ProcessEngineException if more than one tenant has a definition with the given key
   */
  public T findDeployedLatestDefinitionByKey(String definitionKey) {
    T definition = managerFactory.getManager()
        .findLatestDefinitionByKey(definitionKey);
    errorChecker.checkInvalidDefinitionByKey(definitionKey, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  public T findDeployedLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId) {
    T definition = managerFactory.getManager()
        .findLatestDefinitionByKeyAndTenantId(definitionKey, tenantId);
    errorChecker.checkInvalidDefinitionByKeyAndTenantId(definitionKey, tenantId, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  @SuppressWarnings("ConstantConditions")
  public T findDeployedDefinitionByKeyVersionAndTenantId(final String definitionKey, final Integer definitionVersion, final String tenantId) {
    final CommandContext commandContext = Context.getCommandContext();
    T definition = commandContext.runWithoutAuthorization(new Callable<T>() {
      public T call() throws Exception {
        return managerFactory.getManager().
            findDefinitionByKeyVersionAndTenantId(definitionKey, definitionVersion, tenantId);
      }
    });
    errorChecker.checkInvalidDefinitionByKeyVersionAndTenantId(definitionKey, definitionVersion, tenantId, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  public T findDeployedDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    T definition = managerFactory.getManager().findDefinitionByDeploymentAndKey(deploymentId, definitionKey);
    errorChecker.checkInvalidDefinitionByDeploymentAndKey(deploymentId, definitionKey, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

  @SuppressWarnings("ConstantConditions")
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
          cacheDeployer.deployOnlyGivenResourceOfDeployment(deployment, definition.getResourceName());
          cachedDefinition = cache.get(definitionId);
        }
      }
      errorChecker.checkInvalidDefinitionWasCached(deploymentId, definitionId, cachedDefinition);
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


}