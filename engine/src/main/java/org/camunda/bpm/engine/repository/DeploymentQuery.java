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

import java.util.Date;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.query.Query;

/**
 * Allows programmatic querying of {@link Deployment}s.
 *
 * Note that it is impossible to retrieve the deployment resources through the
 * results of this operation, since that would cause a huge transfer of
 * (possibly) unneeded bytes over the wire.
 *
 * To retrieve the actual bytes of a deployment resource use the operations on the
 * {@link RepositoryService#getDeploymentResourceNames(String)}
 * and {@link RepositoryService#getResourceAsStream(String, String)}
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Ingo Richtsmeier
 */
public interface DeploymentQuery extends Query<DeploymentQuery, Deployment>{

  /** Only select deployments with the given deployment id. */
  DeploymentQuery deploymentId(String deploymentId);

  /** Only select deployments with the given name. */
  DeploymentQuery deploymentName(String name);

  /** Only select deployments with a name like the given string. */
  DeploymentQuery deploymentNameLike(String nameLike);

  /**
   * If the given <code>source</code> is <code>null</code>,
   * then deployments are returned where source is equal to null.
   * Otherwise only deployments with the given source are
   * selected.
   */
  DeploymentQuery deploymentSource(String source);

  /** Only select deployments deployed before the given date */
  DeploymentQuery deploymentBefore(Date before);

  /** Only select deployments deployed after the given date */
  DeploymentQuery deploymentAfter(Date after);

  /** Only select deployments with one of the given tenant ids. */
  DeploymentQuery tenantIdIn(String... tenantIds);

  /** Only select deployments which have no tenant id. */
  DeploymentQuery withoutTenantId();

  /**
   * Select deployments which have no tenant id. Can be used in
   * combination with {@link #tenantIdIn(String...)}.
   */
  DeploymentQuery includeDeploymentsWithoutTenantId();

  //sorting ////////////////////////////////////////////////////////

  /** Order by deployment id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  DeploymentQuery orderByDeploymentId();

  /** Order by deployment name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  DeploymentQuery orderByDeploymentName();

  /** Order by deployment time (needs to be followed by {@link #asc()} or {@link #desc()}).
   * @deprecated Use {@link #orderByDeploymentTime()} instead</p>*/
  @Deprecated
  DeploymentQuery orderByDeploymenTime();

  /** Order by deployment time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  DeploymentQuery orderByDeploymentTime();

  /** Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of process instances without tenant id is database-specific. */
  DeploymentQuery orderByTenantId();

}
