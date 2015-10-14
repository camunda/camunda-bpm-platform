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

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;

import java.util.List;

/**
 * <p>
 * Builder for executing a re-deploy of a given deployment.
 * </p>
 *
 * <p>
 * A builder instance can be obtained through {@link org.camunda.bpm.engine.RepositoryService#createRedeployment()}.
 * </p>
 *
 * <p>
 * Deployment resources to re-deploy can be added by:
 * <ul>
 *   <li>{@link #addResourceId(String)}</li>
 *   <li>{@link #addResourceIds(String)}</li>
 *   <li>{@link #addResourceName(String)}</li>
 *   <li>{@link #addResourceNames(String)}</li>
 * </ul>
 * before calling the {@link #redeploy()} operation. The added deployment resources
 * have to be part of the given deployment to re-deploy.
 * </p>
 *
 * <p>
 * If specific deployment resources to re-deploy are not added, then all existing
 * deployment resources of the given deployment are re-deployed.
 * </p>
 *
 * <p>
 * After re-deploying, no more changes can be made to the returned deployment
 * and the builder instance can be disposed.
 * </p>
 *
 * @author Roman Smirnov
 */
public interface RedeploymentBuilder {

  /**
   * Sets the source of a deployment.
   */
  RedeploymentBuilder source(String source);

  /**
   * Adds the id of the deployment resource to re-deploy.
   */
  RedeploymentBuilder addResourceId(String resourceId);

  /**
   * Adds a list of ids of deployment resources to re-deploy.
   */
  RedeploymentBuilder addResourceIds(List<String> resourceIds);

  /**
   * Adds the name of the deployment resource to re-deploy.
   */
  RedeploymentBuilder addResourceName(String resourceName);

  /**
   * Adds a list of names of the deployment resource to re-deploy.
   */
  RedeploymentBuilder addResourceNames(List<String> resourceNames);

  /**
   * Redeploys all provided resources to the process engine.
   *
   * @throws NotFoundException
   *   if no deployment with the given id exists or one of the deployment
   *   resource to re-deploy does not exist.
   *
   * @throws NotValidException
   *    if the given deployment id is null or there are no deployment resources
   *    to re-deploy.
   *
   * @throws AuthorizationException thrown if the current user does not possess the following permissions:
   *   <ul>
   *     <li>{@link Permissions#READ} on {@link Resources#DEPLOYMENT}</li>
   *     <li>{@link Permissions#CREATE} on {@link Resources#DEPLOYMENT}</li>
   *   </ul>
   */
  Deployment redeploy();

}
