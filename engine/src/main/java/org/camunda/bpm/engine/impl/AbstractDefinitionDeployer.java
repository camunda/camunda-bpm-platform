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
package org.camunda.bpm.engine.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.Properties;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;

/**
 * {@link Deployer} responsible to parse resource files and create the proper entities.
 * This class is extended by specific resource deployers.
 *
 * Note: Implementations must be thread-safe. In particular they should not keep deployment-specific state.
 */
public abstract class AbstractDefinitionDeployer<DefinitionEntity extends ResourceDefinitionEntity> implements Deployer {

  public static final String[] DIAGRAM_SUFFIXES = new String[] { "png", "jpg", "gif", "svg" };

  private final CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected IdGenerator idGenerator;

  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public void deploy(DeploymentEntity deployment) {
    LOG.debugProcessingDeployment(deployment.getName());
    Properties properties = new Properties();
    List<DefinitionEntity> definitions = parseDefinitionResources(deployment, properties);
    ensureNoDuplicateDefinitionKeys(definitions);
    postProcessDefinitions(deployment, definitions, properties);
  }

  protected List<DefinitionEntity> parseDefinitionResources(DeploymentEntity deployment, Properties properties) {
    List<DefinitionEntity> definitions = new ArrayList<DefinitionEntity>();
    for (ResourceEntity resource : deployment.getResources().values()) {
      LOG.debugProcessingResource(resource.getName());
      if (isResourceHandled(resource)) {
        definitions.addAll(transformResource(deployment, resource, properties));
      }
    }
    return definitions;
  }

  protected boolean isResourceHandled(ResourceEntity resource) {
    String resourceName = resource.getName();

    for (String suffix : getResourcesSuffixes()) {
      if (resourceName.endsWith(suffix)) {
        return true;
      }
    }

    return false;
  }

  /**
   * @return the list of resource suffixes for this cacheDeployer
   */
  protected abstract String[] getResourcesSuffixes();

  protected Collection<DefinitionEntity> transformResource(DeploymentEntity deployment, ResourceEntity resource, Properties properties) {
    String resourceName = resource.getName();
    List<DefinitionEntity> definitions = transformDefinitions(deployment, resource, properties);

    for (DefinitionEntity definition : definitions) {
      definition.setResourceName(resourceName);

      String diagramResourceName = getDiagramResourceForDefinition(deployment, resourceName, definition, deployment.getResources());
      if (diagramResourceName != null) {
        definition.setDiagramResourceName(diagramResourceName);
      }
    }

    return definitions;
  }


  /**
   * Transform the resource entity into definition entities.
   *
   * @param deployment the deployment the resources belongs to
   * @param resource the resource to transform
   * @return a list of transformed definition entities
   */
  protected abstract List<DefinitionEntity> transformDefinitions(DeploymentEntity deployment, ResourceEntity resource, Properties properties);

  /**
   * Returns the default name of the image resource for a certain definition.
   *
   * It will first look for an image resource which matches the definition
   * specifically, before resorting to an image resource which matches the file
   * containing the definition.
   *
   * Example: if the deployment contains a BPMN 2.0 xml resource called
   * 'abc.bpmn20.xml' containing only one process with key 'myProcess', then
   * this method will look for an image resources called 'abc.myProcess.png'
   * (or .jpg, or .gif, etc.) or 'abc.png' if the previous one wasn't found.
   *
   * Example 2: if the deployment contains a BPMN 2.0 xml resource called
   * 'abc.bpmn20.xml' containing three processes (with keys a, b and c),
   * then this method will first look for an image resource called 'abc.a.png'
   * before looking for 'abc.png' (likewise for b and c).
   * Note that if abc.a.png, abc.b.png and abc.c.png don't exist, all
   * processes will have the same image: abc.png.
   *
   * @return null if no matching image resource is found.
   */
  protected String getDiagramResourceForDefinition(DeploymentEntity deployment, String resourceName, DefinitionEntity definition, Map<String, ResourceEntity> resources) {
    for (String diagramSuffix: getDiagramSuffixes()) {
      String definitionDiagramResource = getDefinitionDiagramResourceName(resourceName, definition, diagramSuffix);
      String diagramForFileResource = getGeneralDiagramResourceName(resourceName, definition, diagramSuffix);
      if (resources.containsKey(definitionDiagramResource)) {
        return definitionDiagramResource;
      } else if (resources.containsKey(diagramForFileResource)) {
        return diagramForFileResource;
      }
    }
    // no matching diagram found
    return null;
  }

  protected String getDefinitionDiagramResourceName(String resourceName, DefinitionEntity definition, String diagramSuffix) {
    String fileResourceBase = stripDefinitionFileSuffix(resourceName);
    String definitionKey = definition.getKey();

    return fileResourceBase + definitionKey + "." + diagramSuffix;
  }

  protected String getGeneralDiagramResourceName(String resourceName, DefinitionEntity definition, String diagramSuffix) {
    String fileResourceBase = stripDefinitionFileSuffix(resourceName);

    return fileResourceBase + diagramSuffix;
  }

  protected String stripDefinitionFileSuffix(String resourceName) {
    for (String suffix : getResourcesSuffixes()) {
      if(resourceName.endsWith(suffix)) {
        return resourceName.substring(0, resourceName.length() - suffix.length());
      }
    }
    return resourceName;
  }

  protected String[] getDiagramSuffixes() {
    return DIAGRAM_SUFFIXES;
  }

  protected void ensureNoDuplicateDefinitionKeys(List<DefinitionEntity> definitions) {
    Set<String> keys = new HashSet<String>();

    for (DefinitionEntity definition : definitions) {

      String key = definition.getKey();

      if (keys.contains(key)) {
        throw new ProcessEngineException("The deployment contains definitions with the same key '" + key + "' (id attribute), this is not allowed");
      }

      keys.add(key);
    }
  }

  protected void postProcessDefinitions(DeploymentEntity deployment, List<DefinitionEntity> definitions, Properties properties) {
    if (deployment.isNew()) {
      // if the deployment is new persist the new definitions
      persistDefinitions(deployment, definitions, properties);
    } else {
      // if the current deployment is not a new one,
      // then load the already existing definitions
      loadDefinitions(deployment, definitions, properties);
    }
  }

  protected void persistDefinitions(DeploymentEntity deployment, List<DefinitionEntity> definitions, Properties properties) {
    for (DefinitionEntity definition : definitions) {
      String definitionKey = definition.getKey();
      String tenantId = deployment.getTenantId();

      DefinitionEntity latestDefinition = findLatestDefinitionByKeyAndTenantId(definitionKey, tenantId);

      updateDefinitionByLatestDefinition(deployment, definition, latestDefinition);

      persistDefinition(definition);
      registerDefinition(deployment, definition, properties);
    }
  }

  protected void updateDefinitionByLatestDefinition(DeploymentEntity deployment, DefinitionEntity definition, DefinitionEntity latestDefinition) {
    definition.setVersion(getNextVersion(deployment, definition, latestDefinition));
    definition.setId(generateDefinitionId(deployment, definition, latestDefinition));
    definition.setDeploymentId(deployment.getId());
    definition.setTenantId(deployment.getTenantId());
  }

  protected void loadDefinitions(DeploymentEntity deployment, List<DefinitionEntity> definitions, Properties properties) {
    for (DefinitionEntity definition : definitions) {
      String deploymentId = deployment.getId();
      String definitionKey = definition.getKey();

      DefinitionEntity persistedDefinition = findDefinitionByDeploymentAndKey(deploymentId, definitionKey);
      handlePersistedDefinition(definition, persistedDefinition, deployment, properties);
    }
  }

  protected void handlePersistedDefinition(DefinitionEntity definition,
            DefinitionEntity persistedDefinition, DeploymentEntity deployment, Properties properties) {
    persistedDefinitionLoaded(deployment, definition, persistedDefinition);
    updateDefinitionByPersistedDefinition(deployment, definition, persistedDefinition);
    registerDefinition(deployment, definition, properties);
  }

  protected void updateDefinitionByPersistedDefinition(DeploymentEntity deployment, DefinitionEntity definition, DefinitionEntity persistedDefinition) {
    definition.setVersion(persistedDefinition.getVersion());
    definition.setId(persistedDefinition.getId());
    definition.setDeploymentId(deployment.getId());
    definition.setTenantId(persistedDefinition.getTenantId());
  }

  /**
   * Called when a previous version of a definition was loaded from the persistent store.
   *
   * @param deployment the deployment of the definition
   * @param definition the definition entity
   * @param persistedDefinition the loaded definition entity
   */
  protected void persistedDefinitionLoaded(DeploymentEntity deployment, DefinitionEntity definition, DefinitionEntity persistedDefinition) {
    // do nothing;
  }

  /**
   * Find a definition entity by deployment id and definition key.
   * @param deploymentId the deployment id
   * @param definitionKey the definition key
   * @return the corresponding definition entity or null if non is found
   */
  protected abstract DefinitionEntity findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey);

  /**
   * Find the last deployed definition entity by definition key and tenant id.
   *
   * @return the corresponding definition entity or null if non is found
   */
  protected abstract DefinitionEntity findLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId);

  /**
   * Persist definition entity into the database.
   * @param definition the definition entity
   */
  protected abstract void persistDefinition(DefinitionEntity definition);

  protected void registerDefinition(DeploymentEntity deployment, DefinitionEntity definition, Properties properties) {
    DeploymentCache deploymentCache = getDeploymentCache();

    // Add to cache
    addDefinitionToDeploymentCache(deploymentCache, definition);

    definitionAddedToDeploymentCache(deployment, definition, properties);

    // Add to deployment for further usage
    deployment.addDeployedArtifact(definition);
  }

  /**
   * Add a definition to the deployment cache
   *
   * @param deploymentCache the deployment cache
   * @param definition the definition to add
   */
  protected abstract void addDefinitionToDeploymentCache(DeploymentCache deploymentCache, DefinitionEntity definition);

  /**
   * Called after a definition was added to the deployment cache.
   *
   * @param deployment the deployment of the definition
   * @param definition the definition entity
   */
  protected void definitionAddedToDeploymentCache(DeploymentEntity deployment, DefinitionEntity definition, Properties properties) {
    // do nothing
  }

  /**
   * per default we increment the latest definition version by one - but you
   * might want to hook in some own logic here, e.g. to align definition
   * versions with deployment / build versions.
   */
  protected int getNextVersion(DeploymentEntity deployment, DefinitionEntity newDefinition, DefinitionEntity latestDefinition) {
    int result = 1;
    if (latestDefinition != null) {
      int latestVersion = latestDefinition.getVersion();
      result = latestVersion + 1;
    }
    return result;
  }

  /**
   * create an id for the definition. The default is to ask the {@link IdGenerator}
   * and add the definition key and version if that does not exceed 64 characters.
   * You might want to hook in your own implementation here.
   */
  protected String generateDefinitionId(DeploymentEntity deployment, DefinitionEntity newDefinition, DefinitionEntity latestDefinition) {
    String nextId = idGenerator.getNextId();

    String definitionKey = newDefinition.getKey();
    int definitionVersion = newDefinition.getVersion();

    String definitionId = definitionKey
      + ":" + definitionVersion
      + ":" + nextId;

    // ACT-115: maximum id length is 64 characters
    if (definitionId.length() > 64) {
      definitionId = nextId;
    }
    return definitionId;
  }

  protected ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return Context.getProcessEngineConfiguration();
  }

  protected CommandContext getCommandContext() {
    return Context.getCommandContext();
  }

  protected DeploymentCache getDeploymentCache() {
    return getProcessEngineConfiguration().getDeploymentCache();
  }

}
