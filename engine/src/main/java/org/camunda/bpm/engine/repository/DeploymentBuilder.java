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

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.zip.ZipInputStream;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

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
   * <p>If set, this deployment will be compared to any previous deployment.
   * This means that every (non-generated) resource will be compared with the
   * provided resources of this deployment. If any resource of this deployment
   * is different to the existing resources, <i>all</i> resources are re-deployed.
   * </p>
   *
   * <p><b>Deprecated</b>: use {@link #enableDuplicateFiltering(boolean)}</p>
   */
  @Deprecated
  DeploymentBuilder enableDuplicateFiltering();

  /**
   * Check the resources for duplicates in the set of previous deployments.
   * If no resources have changed in this deployment, its contained resources
   * are not deployed at all. For further configuration, use the parameter
   * <code>deployChangedOnly</code>.
   *
   * @param deployChangedOnly determines whether only those resources should be
   * deployed that have changed from the previous versions of the deployment.
   * If false, all of the resources are re-deployed if any resource differs.
   */
  DeploymentBuilder enableDuplicateFiltering(boolean deployChangedOnly);

  /**
   * Sets the date on which the process definitions contained in this deployment
   * will be activated. This means that all process definitions will be deployed
   * as usual, but they will be suspended from the start until the given activation date.
   */
  DeploymentBuilder activateProcessDefinitionsOn(Date date);

  /**
   * <p>Sets the source of a deployment.</p>
   *
   * <p>
   * Furthermore if duplicate check of deployment resources is enabled (by calling
   * {@link #enableDuplicateFiltering(boolean)}) then only previous deployments
   * with the same given source (or where the source is equal to <code>null</code>) are
   * considered to perform the duplicate check.
   * </p>
   */
  DeploymentBuilder source(String source);

  /**
   * Deploys all provided sources to the process engine.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#DEPLOYMENT}.
   */
  Deployment deploy();

  /**
   *  @return the names of the resources which were added to this builder.
   */
  Collection<String> getResourceNames();

}
