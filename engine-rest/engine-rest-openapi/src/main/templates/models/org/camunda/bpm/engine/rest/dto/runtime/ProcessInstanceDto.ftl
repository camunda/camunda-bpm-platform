{
  "type": "object",
  "properties": {

    <@lib.property
        name = "id"
        type = "string"
        description = "The id of the process instance." />

    <@lib.property
        name = "definitionId"
        type = "string"
        description = "The id of the process definition that this process instance belongs to." />

    <@lib.property
        name = "businessKey"
        type = "string"
        description = "The business key of the process instance." />

    <@lib.property
        name = "caseInstanceId"
        type = "string"
        description = "The id of the case instance associated with the process instance." />

    <@lib.property
        name = "ended"
        type = "boolean"
        deprecated = true
        description = "A flag indicating whether the process instance has ended or not. Deprecated: will always be false!" />

    <@lib.property
        name = "suspended"
        type = "boolean"
        description = "A flag indicating whether the process instance is suspended or not." />

    <@lib.property
        name = "tenantId"
        type = "string"
        description = "The tenant id of the process instance." />

    <@lib.property
        name = "links"
        type = "array"
        dto = "AtomLink"
        last = true
        description = "The links associated to the process instance." />

  }
}