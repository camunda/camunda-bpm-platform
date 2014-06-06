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

import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipInputStream;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationDeploymentBuilderImpl extends DeploymentBuilderImpl implements ProcessApplicationDeploymentBuilder {

  private static final long serialVersionUID = 1L;

  protected final ProcessApplicationReference processApplicationReference;
  protected boolean isResumePreviousVersions = false;

  public ProcessApplicationDeploymentBuilderImpl(RepositoryServiceImpl repositoryService, ProcessApplicationReference reference) {
    super(repositoryService);
    this.processApplicationReference = reference;
  }

  public ProcessApplicationDeploymentBuilder resumePreviousVersions() {
    this.isResumePreviousVersions = true;
    return this;
  }

  // overrides from parent ////////////////////////////////////////////////

  @Override
  public ProcessApplicationDeployment deploy() {
    return (ProcessApplicationDeployment) super.deploy();
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl activateProcessDefinitionsOn(Date date) {
    return (ProcessApplicationDeploymentBuilderImpl) super.activateProcessDefinitionsOn(date);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl addInputStream(String resourceName, InputStream inputStream) {
    return (ProcessApplicationDeploymentBuilderImpl) super.addInputStream(resourceName, inputStream);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl addClasspathResource(String resource) {
    return (ProcessApplicationDeploymentBuilderImpl) super.addClasspathResource(resource);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl addString(String resourceName, String text) {
    return (ProcessApplicationDeploymentBuilderImpl) super.addString(resourceName, text);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl addModelInstance(String resourceName, BpmnModelInstance modelInstance) {
    return (ProcessApplicationDeploymentBuilderImpl) super.addModelInstance(resourceName, modelInstance);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl addZipInputStream(ZipInputStream zipInputStream) {
    return (ProcessApplicationDeploymentBuilderImpl) super.addZipInputStream(zipInputStream);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl name(String name) {
    return (ProcessApplicationDeploymentBuilderImpl) super.name(name);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl enableDuplicateFiltering() {
    return (ProcessApplicationDeploymentBuilderImpl) super.enableDuplicateFiltering();
  }

  // getters / setters ///////////////////////////////////////////////

  public boolean isResumePreviousVersions() {
    return isResumePreviousVersions;
  }

  public ProcessApplicationReference getProcessApplicationReference() {
    return processApplicationReference;
  }

}
