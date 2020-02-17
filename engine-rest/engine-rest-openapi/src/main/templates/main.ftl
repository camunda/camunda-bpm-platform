<#import "/lib/macros.ftl" as lib>
{
  "openapi": "3.0.2",
  "info": {
    "title": "Camunda Rest API",
    "description": "OpenApi Spec for Camunda REST API.",
    "version": "${cambpmVersion}",
    "license": {
      "name": "Apache 2.0",
      "url": "http://www.apache.org/licenses/LICENSE-2.0.html"
    }
  },
  "externalDocs": {
    "description": "Find out more about Camunda Rest API",
    "url": "https://docs.camunda.org/manual/${docsVersion}/reference/rest/overview/"
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
      "DeleteProcessInstanceHPIQDto":              <#include "/models/org/camunda/bpm/engine/rest/dto/history/DeleteProcessInstanceHPIQDto.ftl">,
      "HistoricProcessInstanceQueryDto":           <#include "/models/org/camunda/bpm/engine/rest/dto/history/HistoricProcessInstanceQueryDto.ftl">,
      "SetJobRetriesByProcessHPIQDto":             <#include "/models/org/camunda/bpm/engine/rest/dto/history/SetJobRetriesByProcessHPIQDto.ftl">,

      "DeploymentDto":                             <#include "/models/org/camunda/bpm/engine/rest/dto/repository/DeploymentDto.ftl">,
      "CaseDefinitionDto":                         <#include "/models/org/camunda/bpm/engine/rest/dto/repository/CaseDefinitionDto.ftl">,
      "DecisionDefinitionDto":                     <#include "/models/org/camunda/bpm/engine/rest/dto/repository/DecisionDefinitionDto.ftl">,
      "DecisionRequirementsDefinitionDto":         <#include "/models/org/camunda/bpm/engine/rest/dto/repository/DecisionRequirementsDefinitionDto.ftl">,
      "ProcessDefinitionDto":                      <#include "/models/org/camunda/bpm/engine/rest/dto/repository/ProcessDefinitionDto.ftl">,

      "BatchDto":                                  <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/batch/BatchDto.ftl">,
      "DeleteProcessInstancesDto":                 <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/batch/DeleteProcessInstancesDto.ftl">,

      "ActivityInstanceDto":                       <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/ActivityInstanceDto.ftl">,
      "ActivityInstanceIncidentDto":               <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/ActivityInstanceIncidentDto.ftl">,
      "DeleteProcessInstancePIQDto":               <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/DeleteProcessInstancePIQDto.ftl">,
      "ProcessInstanceSuspensionStateDto":         <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/ProcessInstanceSuspensionStateDto.ftl">,
      "ProcessInstanceSuspensionStateQueriesDto":  <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/ProcessInstanceSuspensionStateQueriesDto.ftl">,
      "ProcessInstanceDto":                        <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/ProcessInstanceDto.ftl">,
      "ProcessInstanceQueryDto":                   <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/ProcessInstanceQueryDto.ftl">,
      "SetJobRetriesByProcessDto":                 <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/SetJobRetriesByProcessDto.ftl">,
      "SetJobRetriesByProcessPIQDto":              <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/SetJobRetriesByProcessPIQDto.ftl">,
      "SingleProcessInstanceSuspensionStateDto":   <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/SingleProcessInstanceSuspensionStateDto.ftl">,
      "TransitionInstanceDto":                     <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/TransitionInstanceDto.ftl">,
      "TriggerVariableValueDto":                   <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/TriggerVariableValueDto.ftl">,

      "ProcessInstanceModificationDto":            <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/modification/ProcessInstanceModificationDto.ftl">,
      "ProcessInstanceModificationInstructionDto": <#include "/models/org/camunda/bpm/engine/rest/dto/runtime/modification/ProcessInstanceModificationInstructionDto.ftl">,

      "AtomLink":                                  <#include "/models/org/camunda/bpm/engine/rest/dto/AtomLink.ftl">,
      "CountResultDto":                            <#include "/models/org/camunda/bpm/engine/rest/dto/CountResultDto.ftl">,
      "ExceptionDto":                              <#include "/models/org/camunda/bpm/engine/rest/dto/ExceptionDto.ftl">,
      "ParseExceptionDto":                         <#include "/models/org/camunda/bpm/engine/rest/dto/ParseExceptionDto.ftl">,
      "PatchVariablesDto":                         <#include "/models/org/camunda/bpm/engine/rest/dto/PatchVariablesDto.ftl">,
      "ProblemDto":                                <#include "/models/org/camunda/bpm/engine/rest/dto/ProblemDto.ftl">,
      "ResourceReportDto":                         <#include "/models/org/camunda/bpm/engine/rest/dto/ResourceReportDto.ftl">,
      "VariableQueryParameterDto":                 <#include "/models/org/camunda/bpm/engine/rest/dto/VariableQueryParameterDto.ftl">,
      "VariableValueDto":                          <#include "/models/org/camunda/bpm/engine/rest/dto/VariableValueDto.ftl">
    }
  }
}
