<#import "/lib/macros.ftl" as lib>
{
  "openapi": "3.0.2",
  "info": {
    "title": "[Test] Camunda REST API",
    "description": "Swagger OpenApi Spec for some Camunda REST API.",
    "version": "7.13.0-alpha1",
    "contact": {
      "email": "test@example.com"
    }
  },
  "servers": [
    {
      "url": "http://localhost:8080/engine-rest"
    }
  ],
  "tags": [
    {"name": "Process instance"},
    {"name": "Deployment"}
  ],
  "paths": {

    <#include "/paths/process-instance/all.ftl">,

    <#include "/paths/deployment/all.ftl">
    <#-- TODO -->
  },
  "components": {
    "schemas": {
      "HistoricProcessInstanceQueryDto": <#include "/models/org/camunda/bpm/engine/rest/dto/history/HistoricProcessInstanceQueryDto.ftl">,

      "DeploymentDto": <#include "/models/org/camunda/bpm/engine/rest/dto/repository/DeploymentDto.ftl">,
      "CaseDefinitionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/repository/CaseDefinitionDto.ftl">,
      "DecisionDefinitionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/repository/DecisionDefinitionDto.ftl">,
      "DecisionRequirementsDefinitionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/repository/DecisionRequirementsDefinitionDto.ftl">,
      "ProcessDefinitionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/repository/ProcessDefinitionDto.ftl">,

      "BatchDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/batch/BatchDto.ftl">,
      "DeleteProcessInstancesDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/batch/DeleteProcessInstancesDto.ftl">,

      "ActivityInstanceDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/ActivityInstanceDto.ftl">,
      "ActivityInstanceIncidentDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/ActivityInstanceIncidentDto.ftl">,
      "ProcessInstanceDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/ProcessInstanceDto.ftl">,
      "ProcessInstanceQueryDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/ProcessInstanceQueryDto.ftl">,
      "SetJobRetriesByProcessDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/SetJobRetriesByProcessDto.ftl">,
      "TransitionInstanceDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/TransitionInstanceDto.ftl">,
      "TriggerVariableValueDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/TriggerVariableValueDto.ftl">,

      "ProcessInstanceModificationDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/modification/ProcessInstanceModificationDto.ftl">,
      "ProcessInstanceModificationInstructionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/modification/ProcessInstanceModificationInstructionDto.ftl">,

      "AtomLink": <#include "/models/org/camunda/bpm/engine/rest/dto/AtomLink.ftl">,
      "CountResultDto": <#include "/models/org/camunda/bpm/engine/rest/dto/CountResultDto.ftl">,
      "ExceptionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/ExceptionDto.ftl">,
      "ParseExceptionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/ParseExceptionDto.ftl">,
      "PatchVariablesDto": <#include "/models/org/camunda/bpm/engine/rest/dto/PatchVariablesDto.ftl">,
      "ProblemDto": <#include "/models/org/camunda/bpm/engine/rest/dto/ProblemDto.ftl">,
      "ResourceReportDto": <#include "/models/org/camunda/bpm/engine/rest/dto/ResourceReportDto.ftl">,
      "VariableQueryParameterDto": <#include "/models/org/camunda/bpm/engine/rest/dto/VariableQueryParameterDto.ftl">,
      "VariableValueDto": <#include "/models/org/camunda/bpm/engine/rest/dto/VariableValueDto.ftl">
    }
  }
}
