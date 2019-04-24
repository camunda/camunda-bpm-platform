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
package org.camunda.bpm.engine.repository;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * <p>Builder for a {@link ProcessApplication} deployment</p>
 *
 * <p>A process application deployment is different from a regular deployment.
 * Besides deploying a set of process definitions to the database,
 * this deployment has the additional side effect that the process application
 * is registered for the deployment. This means that the process engine will exeute
 * all process definitions contained in the deployment in the context of the process
 * application (by calling the process application's
 * {@link ProcessApplicationInterface#execute(java.util.concurrent.Callable)} method.<p>
 *
 * @author Daniel Meyer
 *
 */
public interface ProcessApplicationDeploymentBuilder extends DeploymentBuilder {

  /**
   * <p>If this method is called, additional registrations will be created for
   * previous versions of the deployment.</p>
   */
  ProcessApplicationDeploymentBuilder resumePreviousVersions();

  /**
   * This method defines on what additional registrations will be based.
   * The value will only be recognized if {@link #resumePreviousVersions()} is set.
   * <p>
   * @see ResumePreviousBy
   * @see #resumePreviousVersions()
   * @param resumeByProcessDefinitionKey one of the constants from {@link ResumePreviousBy}
   */
  ProcessApplicationDeploymentBuilder resumePreviousVersionsBy(String resumePreviousVersionsBy);

  /* {@inheritDoc} */
  ProcessApplicationDeployment deploy();

  // overridden methods //////////////////////////////

  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder addInputStream(String resourceName, InputStream inputStream);
  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder addClasspathResource(String resource);
  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder addString(String resourceName, String text);
  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder addModelInstance(String resourceName, BpmnModelInstance modelInstance);
  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder addZipInputStream(ZipInputStream zipInputStream);
  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder name(String name);
  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder nameFromDeployment(String deploymentId);
  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder source(String source);
  /* {@inheritDoc} */
  @Deprecated
  ProcessApplicationDeploymentBuilder enableDuplicateFiltering();
  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder enableDuplicateFiltering(boolean deployChangedOnly);
  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder activateProcessDefinitionsOn(Date date);

  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder addDeploymentResources(String deploymentId);

  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder addDeploymentResourceById(String deploymentId, String resourceId);
  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder addDeploymentResourcesById(String deploymentId, List<String> resourceIds);

  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder addDeploymentResourceByName(String deploymentId, String resourceName);
  /* {@inheritDoc} */
  ProcessApplicationDeploymentBuilder addDeploymentResourcesByName(String deploymentId, List<String> resourceNames);

}
