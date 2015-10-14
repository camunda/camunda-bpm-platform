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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
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

  public DeploymentBuilder name(String name) {
    deployment.setName(name);
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

}
