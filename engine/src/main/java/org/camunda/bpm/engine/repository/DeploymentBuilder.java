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
package org.camunda.bpm.engine.repository;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.zip.ZipInputStream;

/**
 * Builder for creating new deployments.
 *
 * A builder instance can be obtained through {@link org.camunda.bpm.engine.RepositoryService#createDeployment()}.
 *
 * Multiple resources can be added to one deployment before calling the {@link #deploy()}
 * operation.
 *
 * After deploying, no more changes can be made to the returned deployment
 * and the builder instance can be disposed.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface DeploymentBuilder {

  DeploymentBuilder addInputStream(String resourceName, InputStream inputStream);
  DeploymentBuilder addClasspathResource(String resource);
  DeploymentBuilder addString(String resourceName, String text);
  DeploymentBuilder addModelInstance(String resourceName, BpmnModelInstance modelInstance);

  DeploymentBuilder addZipInputStream(ZipInputStream zipInputStream);

  /**
   * Gives the deployment the given name.
   */
  DeploymentBuilder name(String name);

  /**
   * If set, this deployment will be compared to any previous deployment.
   * This means that every (non-generated) resource will be compared with the
   * provided resources of this deployment.
   */
  DeploymentBuilder enableDuplicateFiltering();

  /**
   * Sets the date on which the process definitions contained in this deployment
   * will be activated. This means that all process definitions will be deployed
   * as usual, but they will be suspended from the start until the given activation date.
   */
  DeploymentBuilder activateProcessDefinitionsOn(Date date);

  /**
   * Deploys all provided sources to the process engine.
   */
  Deployment deploy();

  /**
   *  @return the names of the resources which were added to this builder.
   */
  Collection<String> getResourceNames();

}
