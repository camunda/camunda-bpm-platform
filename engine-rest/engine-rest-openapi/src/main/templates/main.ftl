<#import "/lib/macros.ftl" as lib>
{
  "openapi": "3.0.1",
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

      "DeploymentDto": <#include "/models/org/camunda/bpm/engine/rest/dto/repository/DeploymentDto.ftl">,
      "CaseDefinitionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/repository/CaseDefinitionDto.ftl">,
      "DecisionDefinitionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/repository/DecisionDefinitionDto.ftl">,
      "DecisionRequirementsDefinitionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/repository/DecisionRequirementsDefinitionDto.ftl">,
      "ProcessDefinitionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/repository/ProcessDefinitionDto.ftl">,

      <#-- TODO resolve by traverse of models dir? -->
      "BatchDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/batch/BatchDto.ftl">,
      "DeleteProcessInstancesDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/batch/DeleteProcessInstancesDto.ftl">,

      "ProcessInstanceQueryDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/ProcessInstanceQueryDto.ftl">,
      "SortingDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/SortingDto.ftl">,

      "AtomLink": <#include "/models/org/camunda/bpm/engine/rest/dto/AtomLink.ftl">,
      "CountResultDto": <#include "/models/org/camunda/bpm/engine/rest/dto/CountResultDto.ftl">,
      "ExceptionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/ExceptionDto.ftl">
    }
  }
}
