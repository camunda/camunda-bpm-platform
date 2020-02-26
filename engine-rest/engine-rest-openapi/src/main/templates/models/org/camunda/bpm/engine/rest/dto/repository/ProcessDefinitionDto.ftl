{
  "type": "object",
  "properties": {

    <@lib.property
        name = "id"
        type = "string"
        description = "The id of the process definition" />

    <@lib.property
        name = "key"
        type = "string"
        description = "The key of the process definition, i.e., the id of the BPMN 2.0 XML process definition." />

    <@lib.property
        name = "category"
        type = "string"
        description = "The category of the process definition." />

    <@lib.property
        name = "description"
        type = "string"
        description = "The description of the process definition." />

    <@lib.property
        name = "name"
        type = "string"
        description = "The name of the process definition." />

    <@lib.property
        name = "version"
        type = "integer"
        format = "int32"
        description = "The version of the process definition that the engine assigned to it." />

    <@lib.property
        name = "resource"
        type = "string"
        description = "The file name of the process definition." />

    <@lib.property
        name = "deploymentId"
        type = "string"
        description = "The deployment id of the process definition." />

    <@lib.property
        name = "diagram"
        type = "string"
        description = "The file name of the process definition diagram, if it exists." />

    <@lib.property
        name = "suspended"
        type = "boolean"
        description = "A flag indicating whether the definition is suspended or not." />

    <@lib.property
        name = "tenantId"
        type = "string"
        description = "The tenant id of the process definition." />

    <@lib.property
        name = "versionTag"
        type = "string"
        description = "The version tag of the process definition." />

    <@lib.property
        name = "historyTimeToLive"
        type = "integer"
        format = "int32"
        minimum = 0
        description = "History time to live value of the process definition.
Is used within [History cleanup](https://docs.camunda.org/manual/${docsVersion}/user-guide/process-engine/history/#history-cleanup)." />

    <@lib.property
        name = "startableInTasklist"
        type = "boolean"
        last = true
        description = "A flag indicating whether the process definition is startable in Tasklist or not." />

  }
}