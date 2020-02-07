{
  "type": "object",
  "properties": {

    <@lib.property
        name = "id"
        type = "string"
        description = "The id of the decision requirements definition" />

    <@lib.property
        name = "key"
        type = "string"
        description = "The key of the decision requirements definition, i.e., the id of the DMN 1.0 XML decision definition." />

    <@lib.property
        name = "name"
        type = "string"
        description = "The name of the decision requirements definition." />

    <@lib.property
        name = "category"
        type = "string"
        description = "The category of the decision requirements definition." />

    <@lib.property
        name = "version"
        type = "integer"
        format = "int32"
        description = "The version of the decision requirements definition that the engine assigned to it." />

    <@lib.property
        name = "resource"
        type = "string"
        description = "The file name of the decision requirements definition." />

    <@lib.property
        name = "deploymentId"
        type = "string"
        description = "The deployment id of the decision requirements definition." />

    <@lib.property
        name = "tenantId"
        type = "string"
        last = true
        description = "The tenant id of the decisionrequirements definition." />

  }
}