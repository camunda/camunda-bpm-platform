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

import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.RedeploymentBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Roman Smirnov
 *
 */
public class RedeploymentBuilderImpl implements RedeploymentBuilder {

  protected RepositoryServiceImpl repositoryService;

  protected String deploymentId;
  protected Set<String> resourceIds = new HashSet<String>();
  protected Set<String> resourceNames = new HashSet<String>();
  protected DeploymentBuilderImpl deploymentBuilder;

  protected RedeploymentBuilderImpl(RepositoryServiceImpl repositoryService, String deploymentId, DeploymentBuilderImpl deploymentBuilder) {
    this.repositoryService = repositoryService;
    this.deploymentId = deploymentId;
    this.deploymentBuilder = deploymentBuilder;
  }

  public RedeploymentBuilderImpl(RepositoryServiceImpl repositoryService, String deploymentId) {
    this(repositoryService, deploymentId, new DeploymentBuilderImpl(repositoryService));
  }

  public RedeploymentBuilder source(String source) {
    getDeploymentBuilder().source(source);
    return this;
  }

  public RedeploymentBuilder addResourceId(String resourceId) {
    resourceIds.add(resourceId);
    return this;
  }

  public RedeploymentBuilder addResourceIds(List<String> resourceIds) {
    this.resourceIds.addAll(resourceIds);
    return this;
  }

  public RedeploymentBuilder addResourceName(String resourceName) {
    resourceNames.add(resourceName);
    return this;
  }

  public RedeploymentBuilder addResourceNames(List<String> resourceNames) {
    this.resourceNames.addAll(resourceNames);
    return this;
  }

  public Deployment redeploy() {
    return repositoryService.redeploy(this);
  }

  // getter /////////////////////////////////////////////

  public String getDeploymentId() {
    return deploymentId;
  }

  public Set<String> getResourceIds() {
    return resourceIds;
  }

  public Set<String> getResourceNames() {
    return resourceNames;
  }

  public DeploymentBuilderImpl getDeploymentBuilder() {
    return deploymentBuilder;
  }

}
