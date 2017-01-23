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
package org.camunda.bpm.engine.rest.dto.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.repository.*;
import org.camunda.bpm.engine.rest.dto.LinkableDto;

public class DeploymentDto extends LinkableDto {

  protected String id;
  protected String name;
  protected String source;
  protected Date deploymentTime;
  protected String tenantId;
  protected List<ProcessDefinitionDto> deployedProcessDefinitions;
  protected List<CaseDefinitionDto> deployedCaseDefinitions;
  protected List<DecisionDefinitionDto> deployedDecisionDefinitions;
  protected List<DecisionRequirementsDefinitionDto> deployedDecisionRequirementsDefinitions;

  public DeploymentDto() {
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSource() {
    return source;
  }

  public Date getDeploymentTime() {
    return deploymentTime;
  }

  public String getTenantId() {
    return tenantId;
  }

  public List<ProcessDefinitionDto> getDeployedProcessDefinitions() {
    return deployedProcessDefinitions;
  }

  public List<CaseDefinitionDto> getDeployedCaseDefinitions() {
    return deployedCaseDefinitions;
  }

  public List<DecisionDefinitionDto> getDeployedDecisionDefinitions() {
    return deployedDecisionDefinitions;
  }

  public List<DecisionRequirementsDefinitionDto> getDeployedDecisionRequirementsDefinitions() {
    return deployedDecisionRequirementsDefinitions;
  }

  public static DeploymentDto fromDeployment(Deployment deployment) {
    DeploymentDto dto = new DeploymentDto();
    dto.id = deployment.getId();
    dto.name = deployment.getName();
    dto.source = deployment.getSource();
    dto.deploymentTime = deployment.getDeploymentTime();
    dto.tenantId = deployment.getTenantId();

    initDeployedResourceLists(deployment, dto);

    return dto;
  }

  private static void initDeployedResourceLists(Deployment deployment, DeploymentDto dto) {
    List<ProcessDefinition> deployedProcessDefinitions = deployment.getDeployedProcessDefinitions();
    if (deployedProcessDefinitions != null) {
      dto.deployedProcessDefinitions = new ArrayList<ProcessDefinitionDto>();
      for (ProcessDefinition processDefinition : deployedProcessDefinitions) {
        dto.deployedProcessDefinitions.add(ProcessDefinitionDto.fromProcessDefinition(processDefinition));
      }
    }

    List<CaseDefinition> deployedCaseDefinitions = deployment.getDeployedCaseDefinitions();
    if (deployedCaseDefinitions != null) {
      dto.deployedCaseDefinitions = new ArrayList<CaseDefinitionDto>();
      for (CaseDefinition caseDefinition : deployedCaseDefinitions) {
        dto.deployedCaseDefinitions.add(CaseDefinitionDto.fromCaseDefinition(caseDefinition));
      }
    }

    List<DecisionDefinition> deployedDecisionDefinitions = deployment.getDeployedDecisionDefinitions();
    if (deployedCaseDefinitions != null) {
      dto.deployedDecisionDefinitions = new ArrayList<DecisionDefinitionDto>();
      for (DecisionDefinition decisionDefinition : deployedDecisionDefinitions) {
        dto.deployedDecisionDefinitions.add(DecisionDefinitionDto.fromDecisionDefinition(decisionDefinition));
      }
    }

    List<DecisionRequirementsDefinition> deployedDecisionRequirementsDefinitions = deployment.getDeployedDecisionRequirementsDefinitions();
    if (deployedDecisionRequirementsDefinitions != null) {
      dto.deployedDecisionRequirementsDefinitions = new ArrayList<DecisionRequirementsDefinitionDto>();
      for (DecisionRequirementsDefinition drd : deployedDecisionRequirementsDefinitions) {
        dto.deployedDecisionRequirementsDefinitions.add(DecisionRequirementsDefinitionDto.fromDecisionRequirementsDefinition(drd));
      }
    }
  }

}
