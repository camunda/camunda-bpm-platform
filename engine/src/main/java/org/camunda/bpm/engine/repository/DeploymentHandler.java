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

import java.util.Set;

/**
 * <p>The {@link DeploymentHandler} interface should be implemented when there is a need to
 * define a custom behavior for determining what Resources should be added to a new Deployment.</p>
 *
 * <p>The class that implements this interface must provide a Process Engine parameter in it's
 * constructor. Custom implementations of this interface should be coupled with an implementation
 * of the {@link DeploymentHandlerFactory} interface, which can then be wired to the Process Engine
 * through the
 * {@link org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl#setDeploymentHandlerFactory(DeploymentHandlerFactory)}
 * method.</p>
 *
 * <p>See {@link org.camunda.bpm.engine.impl.repository.DefaultDeploymentHandler} for the default
 * behavior of the deployment process.</p>
 */
public interface DeploymentHandler {

  /**
   * <p>This method is called in the first stage of the deployment process. It determines if there
   * is a difference between a {@link Resource} that is included in the set of resources provided
   * for deployment, and a Resource of the same name, already deployed to the Process Engine
   * with a previous Deployment. The method will be called for every (new, existing) Resource
   * pair. If a Resource of the same name doesn't exist in the Process Engine database, the new
   * Resource is deployed by default and this method is not called.</p>
   *
   * <p>An implementation should define the comparison criteria for the two provided
   * Resources. For the default comparison criteria, see
   * {@link org.camunda.bpm.engine.impl.repository.DefaultDeploymentHandler#shouldDeployResource(Resource, Resource)}.</p>
   *
   * <p>The output of this method (Boolean) will determine if <code>newResource</code> is
   * included in the list of Resources to be deployed (true), or not (false).</p>
   *
   * @param newResource      is a resource that is part of the new Deployment.
   * @param existingResource is the most recently deployed resource that has the same resource name.
   * @return <code>true</code>, if the new Resource satisfies the comparison criteria, or
   * <code>false</code> if not.
   */
  boolean shouldDeployResource(Resource newResource, Resource existingResource);

  /**
   * <p>This method is called in the second stage of the deployment process, when the previously
   * called {@link #shouldDeployResource(Resource, Resource)} method determines that none of the
   * provided resources for deployment satisfy the comparison criteria, i.e. there are no new
   * Resources to deploy. This method then determines a {@link Deployment} already present in the
   * Process Engine database that is a duplicate of the new Deployment.</p>
   *
   * <p>An implementation should determine what defines a duplicate Deployment.</p>
   *
   * <p>The returned Deployment ID (String) will be used to retrieve the corresponding
   * Deployment from the Process Engine database. This Deployment will then be registered
   * with the JobExecutor and the Process Application (if a Process Application Deployment is
   * performed). Furthermore, any Process Definitions that this Deployment contains will be
   * scheduled for activation, if a Process Definition Activation Date was set.</p>
   *
   * @param candidateDeployment a wrapper for the set of Resources provided for deployment and the
   *                            name under which they should have been deployed. At this stage,
   *                            it should be assumed that all of the Resources of this set didn't
   *                            satisfy the comparison criteria and will not be deployed.
   * @return the Deployment ID of the Deployment determined to be a duplicate of the data
   * provided by the <code>candidateDeployment</code>.
   */
  String determineDuplicateDeployment(CandidateDeployment candidateDeployment);

  /**
   * <p>This method is called in the last stage of the deployment process, if a Process Application
   * deployment is performed. An additional condition is that the
   * {@link ProcessApplicationDeploymentBuilder#resumePreviousVersions()} flag of the Process
   * Application Deployment Builder is set, and
   * {@link ProcessApplicationDeploymentBuilder#resumePreviousVersionsBy(String)} ()} is set to the
   * default, {@link ResumePreviousBy#RESUME_BY_PROCESS_DEFINITION_KEY} value.</p>
   *
   * <p>The implementation of this method determines a {@link Set} of Deployment IDs of Deployments
   * already present in the Process Engine database. These deployments will then be registered
   * with the Process Application, so that any Java classes that the Process Application provides
   * can be utilised by the Process Definitions of the Deployments.</p>
   *
   * @param processDefinitionKeys are the Process Definition Keys of a subset of Process Resources
   *                              provided for deployment. The subset includes Process Definition
   *                              Keys from:
   *                              <ul>
   *                              <li>Processes from Resources that will not be deployed to the
   *                                  Process Engine database due to the outcome of the
   *                                  {@link #shouldDeployResource(Resource, Resource)} method.</li>
   *                              <li>Processes from Resources that will be deployed to the Process
   *                                  Engine database, that update an existing Process Definition,
   *                                  i.e. there is a Process Definition with the same key present
   *                                  in the Process Engine database.</li>
   *                              </ul>
   *                              It should be noted that, if {@link #shouldDeployResource(Resource, Resource)}
   *                              determines that all of the provided resources should be deployed
   *                              to the database, and there are no Process Definitions with the
   *                              same key already present in the database (i.e. the Process
   *                              Resources are completely new), this method will not be called.
   *
   * @return a {@link Set} of deployment IDs of Deployments already present in the Process Engine
   * database, that should be resumed (registered with the
   * {@link org.camunda.bpm.engine.impl.jobexecutor.JobExecutor} and registered with the newly
   * deployed Process Application).
   */
  Set<String> determineDeploymentsToResumeByProcessDefinitionKey(String[] processDefinitionKeys);

  /**
   * <p>This method is called in the last stage of the deployment process, if a Process Application
   * deployment is performed. An additional condition is that the
   * {@link ProcessApplicationDeploymentBuilder#resumePreviousVersions()} flag of the Process
   * Application Deployment Builder is set, and
   * {@link ProcessApplicationDeploymentBuilder#resumePreviousVersionsBy(String)} ()} is set to the
   * {@link ResumePreviousBy#RESUME_BY_DEPLOYMENT_NAME} value.</p>
   *
   * <p>The implementation of this method should determine a set of Deployment IDs of Deployments
   * with the same name, that are already present in the Process Engine database.</p>
   *
   * @param candidateDeployment a wrapper for the set of resources that were provided for
   *                            deployment and the name that should be used for this deployment.
   *                            This information can be used to find any Deployments of the same
   *                            name already present in the Process Engine database. The set of
   *                            Resources can then be used, if needed, to filter out the resulting
   *                            Deployments according to a given criteria.
   * @return a {@link Set} of deployment IDs of Deployments already present in the Process Engine
   * database, that should be resumed (registered with the
   * {@link org.camunda.bpm.engine.impl.jobexecutor.JobExecutor} and registered with the newly
   * deployed Process Application).
   */
  Set<String> determineDeploymentsToResumeByDeploymentName(CandidateDeployment candidateDeployment);
}
