{
  "type": "object",
  "properties": {

    <@lib.property
        name = "id"
        type = "string"
        description = "The id of the case definition" />

    <@lib.property
        name = "key"
        type = "string"
        description = "The key of the case definition, i.e., the id of the CMMN 2.0 XML case definition." />

    <@lib.property
        name = "category"
        type = "string"
        description = "The category of the case definition." />

    <@lib.property
        name = "name"
        type = "string"
        description = "The name of the case definition." />

    <@lib.property
        name = "version"
        type = "integer"
        format = "int32"
        description = "The version of the case definition that the engine assigned to it." />

    <@lib.property
        name = "resource"
        type = "string"
        description = "The file name of the case definition." />

    <@lib.property
        name = "deploymentId"
        type = "string"
        description = "The deployment id of the case definition." />

    <@lib.property
        name = "tenantId"
        type = "string"
        description = "The tenant id of the case definition." />

    <@lib.property
        name = "historyTimeToLive"
        type = "integer"
        format = "int32"
        minimum = 0
        last = true
        description = "History time to live value of the case definition. Is used within History cleanup (https://docs.camunda.org/manual/${docsVersion}/user-guide/process-engine/history/#history-cleanup)." />

  }
 
}