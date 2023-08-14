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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipInputStream;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.dmn.DmnModelInstance;

/**
 * <p>Builder for creating new deployments.</p>
 *
 * <p>A builder instance can be obtained through {@link org.camunda.bpm.engine.RepositoryService#createDeployment()}.</p>
 *
 * <p>Multiple resources can be added to one deployment before calling the {@link #deploy()}
 * operation.</p>
 *
 * <p>After deploying, no more changes can be made to the returned deployment
 * and the builder instance can be disposed.</p>
 *
 * <p>In order the resources to be processed as definitions, their names must have one of allowed suffixes (file extensions in case of file reference).</p>
 * <table>
 * <thead>
 *   <tr><th>Resource name suffix</th><th>Will be treated as</th></tr>
 * <thead>
 * <tbody>
 *    <tr>
 *      <td>.bpmn20.xml, .bpmn</td><td>BPMN process definition</td>
 *    </tr>
 *    <tr>
 *      <td>.cmmn11.xml, .cmmn10.xml, .cmmn</td><td>CMMN case definition</td>
 *    </tr>
 *    <tr>
 *      <td>.dmn11.xml, .dmn</td><td>DMN decision table</td>
 *    </tr>
 * </tbody>
 * </table>
 *
 * <p>Additionally resources with resource name suffixes .png, .jpg, .gif and .svg can be treated as diagram images. The deployment resource is considered
 * to represent the specific model diagram by file name, e.g. bpmnDiagram1.png will be considered to be a diagram image for bpmnDiagram1.bpmn20.xml.</p>
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface DeploymentBuilder {

  DeploymentBuilder addInputStream(String resourceName, InputStream inputStream);
  DeploymentBuilder addClasspathResource(String resource);
  DeploymentBuilder addString(String resourceName, String text);

  /**
   * Adds a BPMN model to the deployment.
   * @param resourceName resource name. See suffix requirements for resource names: {@see DeploymentBuilder}.
   * @param modelInstance model instance
   * @return
   */
  DeploymentBuilder addModelInstance(String resourceName, BpmnModelInstance modelInstance);

  /**
   * Adds a DMN model to the deployment.
   * @param resourceName resource name. See suffix requirements for resource names: {@see DeploymentBuilder}.
   * @param modelInstance model instance
   * @return
   */
  DeploymentBuilder addModelInstance(String resourceName, DmnModelInstance modelInstance);

  /**
   * Adds a CMMN model to the deployment.
   * @param resourceName resource name. See suffix requirements for resource names: {@see DeploymentBuilder}.
   * @param modelInstance model instance
   * @return
   */
  DeploymentBuilder addModelInstance(String resourceName, CmmnModelInstance modelInstance);

  DeploymentBuilder addZipInputStream(ZipInputStream zipInputStream);

  /**
   * All existing resources contained by the given deployment
   * will be added to the new deployment to re-deploy them.
   *
   * @throws NotValidException if deployment id is null.
   */
  DeploymentBuilder addDeploymentResources(String deploymentId);

  /**
   * A given resource specified by id and deployment id will be added
   * to the new deployment to re-deploy the given resource.
   *
   * @throws NotValidException if either deployment id or resource id is null.
   */
  DeploymentBuilder addDeploymentResourceById(String deploymentId, String resourceId);

  /**
   * All given resources specified by id and deployment id will be added
   * to the new deployment to re-deploy the given resource.
   *
   * @throws NotValidException if either deployment id or the list of resource ids is null.
   */
  DeploymentBuilder addDeploymentResourcesById(String deploymentId, List<String> resourceIds);

  /**
   * A given resource specified by name and deployment id will be added
   * to the new deployment to re-deploy the given resource.
   *
   * @throws NotValidException if either deployment id or resource name is null.
   */
  DeploymentBuilder addDeploymentResourceByName(String deploymentId, String resourceName);

  /**
   * All given resources specified by name and deployment id will be added
   * to the new deployment to re-deploy the given resource.
   *
   * @throws NotValidException if either deployment id or the list of resource names is null.
   */
  DeploymentBuilder addDeploymentResourcesByName(String deploymentId, List<String> resourceNames);

  /**
   * Gives the deployment the given name.
   *
   * @throws NotValidException
   *    if {@link #nameFromDeployment(String)} has been called before.
   */
  DeploymentBuilder name(String name);

  /**
   * Sets the deployment id to retrieve the deployment name from it.
   *
   * @throws NotValidException
   *    if {@link #name(String)} has been called before.
   */
  DeploymentBuilder nameFromDeployment(String deploymentId);

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
   * Check the resources for duplicates in the set of previous deployments with
   * same deployment source. If no resources have changed in this deployment,
   * its contained resources are not deployed at all. For further configuration,
   * use the parameter <code>deployChangedOnly</code>.
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
   * with the same given source are considered to perform the duplicate check.
   * </p>
   */
  DeploymentBuilder source(String source);

  /**
   * <p>Deploys all provided sources to the process engine and returns the created deployment.</p>
   *
   *
   * <p> The returned {@link Deployment} instance has no information about the definitions, which are deployed
   * with that deployment. To access this information you can use the {@link #deployWithResult()} method.
   * This method will return an instance of {@link DeploymentWithDefinitions}, which contains the information
   * about the successful deployed definitions.
   * </p>
   *
   * @throws NotFoundException thrown
   *  <ul>
   *    <li>if the deployment specified by {@link #nameFromDeployment(String)} does not exist or</li>
   *    <li>if at least one of given deployments provided by {@link #addDeploymentResources(String)} does not exist.</li>
   *  </ul>
   *
   * @throws NotValidException
   *    if there are duplicate resource names from different deployments to re-deploy.
   *
   * @throws ParseException
   *    In case of a BPMN Parsing exception due to null historyTimeToLive. To disable this behaviour, configure the
   *    feature flag `enforceHistoryTimeToLive` to `false`.
   *
   * @throws AuthorizationException
   *  thrown if the current user does not possess the following permissions:
   *   <ul>
   *     <li>{@link Permissions#CREATE} on {@link Resources#DEPLOYMENT}</li>
   *     <li>{@link Permissions#READ} on {@link Resources#DEPLOYMENT} (if resources from previous deployments are redeployed)</li>
   *   </ul>
   * @return the created deployment
   */
  Deployment deploy();

  /**
   * Deploys all provided sources to the process engine and returns the created deployment with the deployed definitions.
   *
   * @throws NotFoundException thrown
   *  <ul>
   *    <li>if the deployment specified by {@link #nameFromDeployment(String)} does not exist or</li>
   *    <li>if at least one of given deployments provided by {@link #addDeploymentResources(String)} does not exist.</li>
   *  </ul>
   *
   * @throws NotValidException
   *    if there are duplicate resource names from different deployments to re-deploy.
   *
   * @throws ParseException
   *    In case of a BPMN Parsing exception due to null historyTimeToLive. To disable this behaviour, configure the
   *    feature flag `enforceHistoryTimeToLive` to `false`.
   *
   * @throws AuthorizationException
   *  thrown if the current user does not possess the following permissions:
   *   <ul>
   *     <li>{@link Permissions#CREATE} on {@link Resources#DEPLOYMENT}</li>
   *     <li>{@link Permissions#READ} on {@link Resources#DEPLOYMENT} (if resources from previous deployments are redeployed)</li>
   *   </ul>
   * @return the created deployment, contains the deployed definitions
   */
  DeploymentWithDefinitions deployWithResult();

  /**
   *  @return the names of the resources which were added to this builder.
   */
  Collection<String> getResourceNames();

  /**
   * Sets the tenant id of a deployment.
   */
  DeploymentBuilder tenantId(String tenantId);

}
