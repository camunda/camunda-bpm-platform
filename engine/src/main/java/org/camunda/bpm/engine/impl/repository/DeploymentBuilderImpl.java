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
package org.camunda.bpm.engine.impl.repository;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DeploymentBuilderImpl implements DeploymentBuilder, Serializable {

  private static final long serialVersionUID = 1L;

  protected transient RepositoryServiceImpl repositoryService;
  protected DeploymentEntity deployment = new DeploymentEntity();
  protected boolean isDuplicateFilterEnabled = false;
  protected boolean deployChangedOnly = false;
  protected Date processDefinitionsActivationDate;

  protected String nameFromDeployment;
  protected Set<String> deployments = new HashSet<String>();
  protected Map<String, Set<String>> deploymentResourcesById = new HashMap<String, Set<String>>();
  protected Map<String, Set<String>> deploymentResourcesByName = new HashMap<String, Set<String>>();

  public DeploymentBuilderImpl(RepositoryServiceImpl repositoryService) {
    this.repositoryService = repositoryService;
  }

  public DeploymentBuilder addInputStream(String resourceName, InputStream inputStream) {
    ensureNotNull("inputStream for resource '" + resourceName + "' is null", "inputStream", inputStream);
    byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
    ResourceEntity resource = new ResourceEntity();
    resource.setName(resourceName);
    resource.setBytes(bytes);
    deployment.addResource(resource);
    return this;
  }

  public DeploymentBuilder addClasspathResource(String resource) {
    InputStream inputStream = ReflectUtil.getResourceAsStream(resource);
    ensureNotNull("resource '" + resource + "' not found", "inputStream", inputStream);
    return addInputStream(resource, inputStream);
  }

  public DeploymentBuilder addString(String resourceName, String text) {
    ensureNotNull("text", text);
    ResourceEntity resource = new ResourceEntity();
    resource.setName(resourceName);
    resource.setBytes(text.getBytes());
    deployment.addResource(resource);
    return this;
  }

  public DeploymentBuilder addModelInstance(String resourceName, BpmnModelInstance modelInstance) {
    ensureNotNull("modelInstance", modelInstance);
    String processText = Bpmn.convertToString(modelInstance);
    return addString(resourceName, processText);
  }

  public DeploymentBuilder addZipInputStream(ZipInputStream zipInputStream) {
    try {
      ZipEntry entry = zipInputStream.getNextEntry();
      while (entry != null) {
        if (!entry.isDirectory()) {
          String entryName = entry.getName();
          byte[] bytes = IoUtil.readInputStream(zipInputStream, entryName);
          ResourceEntity resource = new ResourceEntity();
          resource.setName(entryName);
          resource.setBytes(bytes);
          deployment.addResource(resource);
        }
        entry = zipInputStream.getNextEntry();
      }
    } catch (Exception e) {
      throw new ProcessEngineException("problem reading zip input stream", e);
    }
    return this;
  }

  public DeploymentBuilder addDeploymentResources(String deploymentId) {
    ensureNotNull(NotValidException.class, "deploymentId", deploymentId);
    deployments.add(deploymentId);
    return this;
  }

  public DeploymentBuilder addDeploymentResourceById(String deploymentId, String resourceId) {
    ensureNotNull(NotValidException.class, "deploymentId", deploymentId);
    ensureNotNull(NotValidException.class, "resourceId", resourceId);

    CollectionUtil.addToMapOfSets(deploymentResourcesById, deploymentId, resourceId);

    return this;
  }

  public DeploymentBuilder addDeploymentResourcesById(String deploymentId, List<String> resourceIds) {
    ensureNotNull(NotValidException.class, "deploymentId", deploymentId);

    ensureNotNull(NotValidException.class, "resourceIds", resourceIds);
    ensureNotEmpty(NotValidException.class, "resourceIds", resourceIds);
    ensureNotContainsNull(NotValidException.class, "resourceIds", resourceIds);

    CollectionUtil.addCollectionToMapOfSets(deploymentResourcesById, deploymentId, resourceIds);

    return this;
  }

  public DeploymentBuilder addDeploymentResourceByName(String deploymentId, String resourceName) {
    ensureNotNull(NotValidException.class, "deploymentId", deploymentId);
    ensureNotNull(NotValidException.class, "resourceName", resourceName);

    CollectionUtil.addToMapOfSets(deploymentResourcesByName, deploymentId, resourceName);

    return this;
  }

  public DeploymentBuilder addDeploymentResourcesByName(String deploymentId, List<String> resourceNames) {
    ensureNotNull(NotValidException.class, "deploymentId", deploymentId);

    ensureNotNull(NotValidException.class, "resourceNames", resourceNames);
    ensureNotEmpty(NotValidException.class, "resourceNames", resourceNames);
    ensureNotContainsNull(NotValidException.class, "resourceNames", resourceNames);

    CollectionUtil.addCollectionToMapOfSets(deploymentResourcesByName, deploymentId, resourceNames);

    return this;
  }

  public DeploymentBuilder name(String name) {
    if (nameFromDeployment != null && !nameFromDeployment.isEmpty()) {
      String message = String.format("Cannot set the deployment name to '%s', because the property 'nameForDeployment' has been already set to '%s'.", name, nameFromDeployment);
      throw new NotValidException(message);
    }
    deployment.setName(name);
    return this;
  }

  public DeploymentBuilder nameFromDeployment(String deploymentId) {
    String name = deployment.getName();
    if (name != null && !name.isEmpty()) {
      String message = String.format("Cannot set the given deployment id '%s' to get the name from it, because the deployment name has been already set to '%s'.", deploymentId, name);
      throw new NotValidException(message);
    }
    nameFromDeployment = deploymentId;
    return this;
  }

  public DeploymentBuilder enableDuplicateFiltering() {
    return enableDuplicateFiltering(false);
  }

  public DeploymentBuilder enableDuplicateFiltering(boolean deployChangedOnly) {
    this.isDuplicateFilterEnabled = true;
    this.deployChangedOnly = deployChangedOnly;
    return this;
  }

  public DeploymentBuilder activateProcessDefinitionsOn(Date date) {
    this.processDefinitionsActivationDate = date;
    return this;
  }

  public DeploymentBuilder source(String source) {
    deployment.setSource(source);
    return this;
  }

  public Deployment deploy() {
    return repositoryService.deploy(this);
  }

  public Collection<String> getResourceNames() {
    if(deployment.getResources() == null) {
      return Collections.<String>emptySet();
    } else {
      return deployment.getResources().keySet();
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  public DeploymentEntity getDeployment() {
    return deployment;
  }

  public boolean isDuplicateFilterEnabled() {
    return isDuplicateFilterEnabled;
  }

  public boolean isDeployChangedOnly() {
    return deployChangedOnly;
  }

  public Date getProcessDefinitionsActivationDate() {
    return processDefinitionsActivationDate;
  }

  public String getNameFromDeployment() {
    return nameFromDeployment;
  }

  public Set<String> getDeployments() {
    return deployments;
  }

  public Map<String, Set<String>> getDeploymentResourcesById() {
    return deploymentResourcesById;
  }

  public Map<String, Set<String>> getDeploymentResourcesByName() {
    return deploymentResourcesByName;
  }

}
