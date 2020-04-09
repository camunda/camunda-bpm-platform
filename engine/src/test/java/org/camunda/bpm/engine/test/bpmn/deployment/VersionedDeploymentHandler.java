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
package org.camunda.bpm.engine.test.bpmn.deployment;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.repository.CandidateDeployment;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentHandler;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;

public class VersionedDeploymentHandler implements DeploymentHandler {

  protected ProcessEngine processEngine;
  protected RepositoryService repositoryService;
  protected String candidateVersionTag;
  protected String candidateProcessDefinitionKey;

  public VersionedDeploymentHandler(ProcessEngine processEngine) {
    this.processEngine = processEngine;
    this.repositoryService = processEngine.getRepositoryService();
  }

  @Override
  public boolean shouldDeployResource(Resource newResource, Resource existingResource) {

    if (isBpmnResource(newResource)) {

      Integer existingVersion = parseCamundaVersionTag(existingResource);
      Integer newVersion = parseCamundaVersionTag(newResource);
      if (this.candidateVersionTag == null) {
        this.candidateProcessDefinitionKey = parseProcessDefinitionKey(newResource);
        this.candidateVersionTag = String.valueOf(newVersion);
      }

      return newVersion > existingVersion;
    }

    return false;
  }

  @Override
  public String determineDuplicateDeployment(CandidateDeployment candidateDeployment) {

    String deploymentId = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(candidateProcessDefinitionKey)
        .versionTag(String.valueOf(candidateVersionTag))
        .orderByProcessDefinitionVersion()
        .desc()
        .singleResult()
        .getDeploymentId();

    return repositoryService.createDeploymentQuery()
        .deploymentId(deploymentId).singleResult().getId();
  }

  @Override
  public Set<String> determineDeploymentsToResumeByProcessDefinitionKey(
      String[] processDefinitionKeys) {

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKeysIn(processDefinitionKeys).list();

    Set<String> deploymentIds = new HashSet<>();
    for (ProcessDefinition processDefinition : processDefinitions) {
      // If all the resources are new, the candidateVersionTag
      // property will be null since there's nothing to compare it to.
      if (candidateVersionTag != null && candidateVersionTag.equals(processDefinition.getVersionTag())) {
        deploymentIds.add(processDefinition.getDeploymentId());
      }
    }

    return deploymentIds;
  }

  @Override
  public Set<String> determineDeploymentsToResumeByDeploymentName(CandidateDeployment candidateDeployment) {
    Set<String> deploymentIds = new HashSet<>();

    List<Deployment> previousDeployments = processEngine.getRepositoryService()
        .createDeploymentQuery().deploymentName(candidateDeployment.getName()).list();

    for (Deployment deployment : previousDeployments) {

      // find the Process Definitions included in this deployment
      List<ProcessDefinition> deploymentPDs = repositoryService.createProcessDefinitionQuery()
          .deploymentId(deployment.getId())
          .list();

      for (ProcessDefinition processDefinition : deploymentPDs) {
        // only deploy Deployments of the same name that contain a Process Definition with the
        // correct Camunda Version Tag. If all the resources are new, the candidateVersionTag
        // property will be null since there's nothing to compare it to.
        if (candidateVersionTag != null && candidateVersionTag.equals(processDefinition.getVersionTag())) {
          deploymentIds.add(deployment.getId());
          break;
        }
      }
    }

    return deploymentIds;
  }

  protected Integer parseCamundaVersionTag(Resource resource) {
    BpmnModelInstance model = Bpmn
        .readModelFromStream(new ByteArrayInputStream(resource.getBytes()));

    Process process = model.getDefinitions().getChildElementsByType(Process.class)
        .iterator().next();

    return process.getCamundaVersionTag() != null ?
        Integer.valueOf(process.getCamundaVersionTag()) :
        0;
  }

  protected String parseProcessDefinitionKey(Resource resource) {
    BpmnModelInstance model = Bpmn
        .readModelFromStream(new ByteArrayInputStream(resource.getBytes()));

    Process process = model.getDefinitions().getChildElementsByType(Process.class)
        .iterator().next();

    return process.getId();
  }

  protected boolean isBpmnResource(Resource resource) {
    return StringUtil.hasAnySuffix(resource.getName(), BpmnDeployer.BPMN_RESOURCE_SUFFIXES);
  }
}
