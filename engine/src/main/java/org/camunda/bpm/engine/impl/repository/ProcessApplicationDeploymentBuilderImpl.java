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
package org.camunda.bpm.engine.impl.repository;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.camunda.bpm.engine.repository.ResumePreviousBy;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationDeploymentBuilderImpl extends DeploymentBuilderImpl implements ProcessApplicationDeploymentBuilder {

  private static final long serialVersionUID = 1L;

  protected final ProcessApplicationReference processApplicationReference;
  protected boolean isResumePreviousVersions = false;
  protected String resumePreviousVersionsBy = ResumePreviousBy.RESUME_BY_PROCESS_DEFINITION_KEY;

  public ProcessApplicationDeploymentBuilderImpl(RepositoryServiceImpl repositoryService, ProcessApplicationReference reference) {
    super(repositoryService);
    this.processApplicationReference = reference;
    source(ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE);
  }

  public ProcessApplicationDeploymentBuilder resumePreviousVersions() {
    this.isResumePreviousVersions = true;
    return this;
  }

  @Override
  public ProcessApplicationDeploymentBuilder resumePreviousVersionsBy(String resumePreviousVersionsBy) {
    this.resumePreviousVersionsBy = resumePreviousVersionsBy;
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
  public ProcessApplicationDeploymentBuilderImpl tenantId(String tenantId) {
    return (ProcessApplicationDeploymentBuilderImpl) super.tenantId(tenantId);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl nameFromDeployment(String deploymentId) {
    return (ProcessApplicationDeploymentBuilderImpl) super.nameFromDeployment(deploymentId);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl source(String source) {
    return (ProcessApplicationDeploymentBuilderImpl) super.source(source);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl enableDuplicateFiltering() {
    return (ProcessApplicationDeploymentBuilderImpl) super.enableDuplicateFiltering();
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl enableDuplicateFiltering(boolean deployChangedOnly) {
    return (ProcessApplicationDeploymentBuilderImpl) super.enableDuplicateFiltering(deployChangedOnly);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl addDeploymentResources(String deploymentId) {
    return (ProcessApplicationDeploymentBuilderImpl) super.addDeploymentResources(deploymentId);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl addDeploymentResourceById(String deploymentId, String resourceId) {
    return (ProcessApplicationDeploymentBuilderImpl) super.addDeploymentResourceById(deploymentId, resourceId);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl addDeploymentResourcesById(String deploymentId, List<String> resourceIds) {
    return (ProcessApplicationDeploymentBuilderImpl) super.addDeploymentResourcesById(deploymentId, resourceIds);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl addDeploymentResourceByName(String deploymentId, String resourceName) {
    return (ProcessApplicationDeploymentBuilderImpl) super.addDeploymentResourceByName(deploymentId, resourceName);
  }

  @Override
  public ProcessApplicationDeploymentBuilderImpl addDeploymentResourcesByName(String deploymentId, List<String> resourceNames) {
    return (ProcessApplicationDeploymentBuilderImpl) super.addDeploymentResourcesByName(deploymentId, resourceNames);
  }

  // getters / setters ///////////////////////////////////////////////

  public boolean isResumePreviousVersions() {
    return isResumePreviousVersions;
  }

  public ProcessApplicationReference getProcessApplicationReference() {
    return processApplicationReference;
  }

  public String getResumePreviousVersionsBy() {
    return resumePreviousVersionsBy;
  }

}
