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
package org.camunda.bpm.engine.impl.persistence.deploy.cache;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author: Johannes Heinemann
 */
public class CacheDeployer {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected List<Deployer> deployers;

  public CacheDeployer() {
    this.deployers = Collections.emptyList();
  }

  public void setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
  }

  public void deploy(final DeploymentEntity deployment) {
    Context.getCommandContext().runWithoutAuthorization(new Callable<Void>() {
      public Void call() throws Exception {
        for (Deployer deployer : deployers) {
          deployer.deploy(deployment);
        }
        return null;
      }
    });
  }

  public void deployOnlyGivenResourcesOfDeployment(final DeploymentEntity deployment, String... resourceNames) {
    initDeployment(deployment, resourceNames);
    Context.getCommandContext().runWithoutAuthorization(new Callable<Void>() {
      public Void call() throws Exception {
        for (Deployer deployer : deployers) {
          deployer.deploy(deployment);
        }
        return null;
      }
    });
    deployment.setResources(null);
  }

  protected void initDeployment(final DeploymentEntity deployment, String... resourceNames) {
    deployment.clearResources();
    for (String resourceName : resourceNames) {
      if (resourceName != null) {
        // with the given resource we prevent the deployment of querying
        // the database which means using all resources that were utilized during the deployment
        ResourceEntity resource = Context.getCommandContext().getResourceManager().findResourceByDeploymentIdAndResourceName(deployment.getId(), resourceName);

        deployment.addResource(resource);
      }
    }
  }
}
