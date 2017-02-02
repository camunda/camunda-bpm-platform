package org.camunda.bpm.engine.rest.dto.repository;

import org.camunda.bpm.engine.repository.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class DeploymentWithDefinitionsDto extends DeploymentDto {

  protected Map<String, ProcessDefinitionDto> deployedProcessDefinitions;
  protected Map<String, CaseDefinitionDto> deployedCaseDefinitions;
  protected Map<String, DecisionDefinitionDto> deployedDecisionDefinitions;
  protected Map<String, DecisionRequirementsDefinitionDto> deployedDecisionRequirementsDefinitions;

  public Map<String, ProcessDefinitionDto> getDeployedProcessDefinitions() {
    return deployedProcessDefinitions;
  }

  public Map<String, CaseDefinitionDto> getDeployedCaseDefinitions() {
    return deployedCaseDefinitions;
  }

  public Map<String, DecisionDefinitionDto> getDeployedDecisionDefinitions() {
    return deployedDecisionDefinitions;
  }

  public Map<String, DecisionRequirementsDefinitionDto> getDeployedDecisionRequirementsDefinitions() {
    return deployedDecisionRequirementsDefinitions;
  }

  public static DeploymentWithDefinitionsDto fromDeployment(DeploymentWithDefinitions deployment) {
    DeploymentWithDefinitionsDto dto = new DeploymentWithDefinitionsDto();
    dto.id = deployment.getId();
    dto.name = deployment.getName();
    dto.source = deployment.getSource();
    dto.deploymentTime = deployment.getDeploymentTime();
    dto.tenantId = deployment.getTenantId();

    initDeployedResourceLists(deployment, dto);

    return dto;
  }

  private static void initDeployedResourceLists(DeploymentWithDefinitions deployment, DeploymentWithDefinitionsDto dto) {
    List<ProcessDefinition> deployedProcessDefinitions = deployment.getDeployedProcessDefinitions();
    if (deployedProcessDefinitions != null) {
      dto.deployedProcessDefinitions = new HashMap<String, ProcessDefinitionDto>();
      for (ProcessDefinition processDefinition : deployedProcessDefinitions) {
        dto.deployedProcessDefinitions
          .put(processDefinition.getId(), ProcessDefinitionDto.fromProcessDefinition(processDefinition));
      }
    }

    List<CaseDefinition> deployedCaseDefinitions = deployment.getDeployedCaseDefinitions();
    if (deployedCaseDefinitions != null) {
      dto.deployedCaseDefinitions = new HashMap<String, CaseDefinitionDto>();
      for (CaseDefinition caseDefinition : deployedCaseDefinitions) {
        dto.deployedCaseDefinitions
          .put(caseDefinition.getId(), CaseDefinitionDto.fromCaseDefinition(caseDefinition));
      }
    }

    List<DecisionDefinition> deployedDecisionDefinitions = deployment.getDeployedDecisionDefinitions();
    if (deployedDecisionDefinitions != null) {
      dto.deployedDecisionDefinitions = new HashMap<String, DecisionDefinitionDto>();
      for (DecisionDefinition decisionDefinition : deployedDecisionDefinitions) {
        dto.deployedDecisionDefinitions
          .put(decisionDefinition.getId(), DecisionDefinitionDto.fromDecisionDefinition(decisionDefinition));
      }
    }

    List<DecisionRequirementsDefinition> deployedDecisionRequirementsDefinitions = deployment.getDeployedDecisionRequirementsDefinitions();
    if (deployedDecisionRequirementsDefinitions != null) {
      dto.deployedDecisionRequirementsDefinitions = new HashMap<String, DecisionRequirementsDefinitionDto>();
      for (DecisionRequirementsDefinition drd : deployedDecisionRequirementsDefinitions) {
        dto.deployedDecisionRequirementsDefinitions
          .put(drd.getId(), DecisionRequirementsDefinitionDto.fromDecisionRequirementsDefinition(drd));
      }
    }
  }
}
