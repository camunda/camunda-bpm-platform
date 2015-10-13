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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureAtLeastOneNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentManager;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceManager;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.repository.RedeploymentBuilderImpl;
import org.camunda.bpm.engine.repository.Deployment;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Roman Smirnov
 *
 */
public class RedeployCmd implements Command<Deployment>, Serializable {

  private static final long serialVersionUID = 1L;

  protected RedeploymentBuilderImpl redeploymentBuilder;

  public RedeployCmd(RedeploymentBuilderImpl redeploymentBuilder) {
    this.redeploymentBuilder = redeploymentBuilder;
  }

  public Deployment execute(final CommandContext commandContext) {
    final String deploymentId = redeploymentBuilder.getDeploymentId();
    ensureNotNull(NotValidException.class, "deploymentId", deploymentId);

    // check authorization (be aware that the DeployCmd do an
    // authorization check whether the given user is allowed to
    // create a deployment)
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkReadDeployment(deploymentId);

    // get deployment to re-deploy
    DeploymentManager deploymentManager = commandContext.getDeploymentManager();
    final DeploymentEntity deployment = deploymentManager.findDeploymentById(deploymentId);
    ensureNotNull(NotFoundException.class, "No deployment found with id '" + deploymentId + "'", "deployment", deployment);

    final DeploymentBuilderImpl deploymentBuilder = redeploymentBuilder.getDeploymentBuilder();

    // set the name for the deployment
    String name = deployment.getName();
    deploymentBuilder.name(name);

    // add resources to re-deploy
    commandContext.runWithoutAuthorization(new Callable<Void>() {

      public Void call() throws Exception {

        Set<String> resourceIds = redeploymentBuilder.getResourceIds();
        Set<String> resourceNames = redeploymentBuilder.getResourceNames();

        List<ResourceEntity> resourcesToDeploy = null;

        if (resourceIds.isEmpty() && resourceNames.isEmpty()) {
          // all deployment resources have to be re-deployed
          Map<String, ResourceEntity> resources = deployment.getResources();
          resourcesToDeploy = new ArrayList<ResourceEntity>(resources.values());
        }
        else {
          // only given resources have to be re-deployed

          ResourceManager resourceManager = commandContext.getResourceManager();
          resourcesToDeploy = new ArrayList<ResourceEntity>();

          if (!resourceIds.isEmpty()) {
            String[] resourceIdArray = resourceIds.toArray(new String[resourceIds.size()]);
            List<ResourceEntity> resources = resourceManager.findResourceByDeploymentIdAndResourceIds(deploymentId, resourceIdArray);
            checkResourcesToDeployById(deploymentId, resourceIds, resources);
            resourcesToDeploy.addAll(resources);
          }

          if (!resourceNames.isEmpty()) {
            String[] resourceNameArray = resourceNames.toArray(new String[resourceNames.size()]);
            List<ResourceEntity> resources = resourceManager.findResourceByDeploymentIdAndResourceNames(deploymentId, resourceNameArray);
            checkResourcesToDeployByName(deploymentId, resourceNames, resources);
            resourcesToDeploy.addAll(resources);
          }
        }

        // add resources to deployment builder
        addResources(resourcesToDeploy, deploymentBuilder);

        return null;
      }
    });

    // are there any resources to re-deploy
    Collection<String> resources = deploymentBuilder.getResourceNames();
    String[] resourceArray = resources.toArray(new String[resources.size()]);
    ensureAtLeastOneNotEmpty(NotValidException.class, "Cannot re-deploy deployment with id '" + deploymentId + "' because there are no resources to redeploy.", resourceArray);

    // deploy
    return new DeployCmd<Deployment>(deploymentBuilder).execute(commandContext);
  }

  protected void addResources(List<ResourceEntity> resources, DeploymentBuilderImpl deploymentBuilder) {
    for (ResourceEntity resource : resources) {
      String resourceName = resource.getName();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(resource.getBytes());
      deploymentBuilder.addInputStream(resourceName, inputStream);
    }
  }

  protected void checkResourcesToDeployById(String deploymentId, Set<String> expected, List<ResourceEntity> actual) {
    Map<String, ResourceEntity> resources = new HashMap<String, ResourceEntity>();
    for (ResourceEntity resource : actual) {
      resources.put(resource.getId(), resource);
    }
    checkResourcesToDeploy(deploymentId, expected, resources, "id");
  }

  protected void checkResourcesToDeployByName(String deploymentId, Set<String> expected, List<ResourceEntity> actual) {
    Map<String, ResourceEntity> resources = new HashMap<String, ResourceEntity>();
    for (ResourceEntity resource : actual) {
      resources.put(resource.getName(), resource);
    }
    checkResourcesToDeploy(deploymentId, expected, resources, "name");
  }

  protected void checkResourcesToDeploy(String deploymentId, Set<String> expected, Map<String, ResourceEntity> actual, String valueProperty) {
    List<String> missingResources = new ArrayList<String>();
    for (String value : expected) {
      if (actual.get(value) == null) {
        missingResources.add(value);
      }
    }

    if (!missingResources.isEmpty()) {
      StringBuilder builder = new StringBuilder();

      builder.append("The deployment with id '");
      builder.append(deploymentId);
      builder.append("' does not contain one of the following resources with ");
      builder.append(valueProperty);
      builder.append(":");

      boolean first = true;
      for(String missingResource: missingResources) {
        if (!first) {
          builder.append(", ");
        } else {
          first = false;
        }
        builder.append(missingResource);
      }

      throw new NotFoundException(builder.toString());
    }
  }

}
