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
package org.camunda.bpm.engine.impl.repository;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.repository.CandidateDeployment;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentHandler;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.Resource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultDeploymentHandler implements DeploymentHandler {

  protected ProcessEngine processEngine;
  protected RepositoryService repositoryService;

  public DefaultDeploymentHandler(ProcessEngine processEngine) {
    this.processEngine = processEngine;
    this.repositoryService = processEngine.getRepositoryService();
  }

  @Override
  public boolean shouldDeployResource(Resource newResource, Resource existingResource) {
    return resourcesDiffer(newResource, existingResource);
  }

  @Override
  public String determineDuplicateDeployment(CandidateDeployment candidateDeployment) {
    return Context.getCommandContext()
        .getDeploymentManager()
        .findLatestDeploymentByName(candidateDeployment.getName())
        .getId();
  }

  @Override
  public Set<String> determineDeploymentsToResumeByProcessDefinitionKey(
      String[] processDefinitionKeys) {

    Set<String> deploymentIds = new HashSet<>();
    List<ProcessDefinition> processDefinitions = Context.getCommandContext().getProcessDefinitionManager()
        .findProcessDefinitionsByKeyIn(processDefinitionKeys);
    for (ProcessDefinition processDefinition : processDefinitions) {
      deploymentIds.add(processDefinition.getDeploymentId());
    }

    return deploymentIds;
  }

  @Override
  public Set<String> determineDeploymentsToResumeByDeploymentName(CandidateDeployment candidateDeployment) {

    List<Deployment> previousDeployments = processEngine.getRepositoryService()
        .createDeploymentQuery()
        .deploymentName(candidateDeployment.getName())
        .list();

    Set<String> deploymentIds = new HashSet<>();
    for (Deployment deployment : previousDeployments) {
      deploymentIds.add(deployment.getId());
    }

    return deploymentIds;
  }

  protected boolean resourcesDiffer(Resource resource, Resource existing) {
    byte[] bytes = resource.getBytes();
    byte[] savedBytes = existing.getBytes();
    return !Arrays.equals(bytes, savedBytes);
  }
}
