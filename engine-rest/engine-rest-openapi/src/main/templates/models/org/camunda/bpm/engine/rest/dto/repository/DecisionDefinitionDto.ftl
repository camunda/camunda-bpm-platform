{
  "type": "object",
  "properties": {

    <@lib.property
        name = "id"
        type = "string"
        description = "The id of the decision definition" />

    <@lib.property
        name = "key"
        type = "string"
        description = "The key of the decision definition, i.e., the id of the DMN 1.0 XML decision definition." />

    <@lib.property
        name = "category"
        type = "string"
        description = "The category of the decision definition." />

    <@lib.property
        name = "name"
        type = "string"
        description = "The name of the decision definition." />

    <@lib.property
        name = "version"
        type = "integer"
        format = "int32"
        description = "The version of the decision definition that the engine assigned to it." />

    <@lib.property
        name = "resource"
        type = "string"
        description = "The file name of the decision definition." />

    <@lib.property
        name = "deploymentId"
        type = "string"
        description = "The deployment id of the decision definition." />

    <@lib.property
        name = "tenantId"
        type = "string"
        description = "The tenant id of the decision definition." />

    <@lib.property
        name = "decisionRequirementsDefinitionId"
        type = "string"
        description = "The id of the decision requirements definition this decision definition belongs to." />

    <@lib.property
        name = "decisionRequirementsDefinitionKey"
        type = "string"
        description = "The key of the decision requirements definition this decision definition belongs to." />

    <@lib.property
        name = "historyTimeToLive"
        type = "integer"
        format = "int32"
        minimum = 0
        description = "History time to live value of the decision definition.
Is used within [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup)." />

    <@lib.property
        name = "versionTag"
        type = "string"
        last = true
        description = "The version tag of the decision definition." />

  }
}