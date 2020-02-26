{
  "properties": {

    <@lib.property
        name = "suspended"
        type = "boolean"
        description = "A Boolean value which indicates whether to activate or suspend a given process instance.
When the value is set to true, the given process instance will be suspended and when the value is set to false,
the given process instance will be activated." />

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        description = "The process definition id of the process instances to activate or suspend.

**Note**: This parameter can be used only with combination of `suspended`." />

    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        description = "The process definition key of the process instances to activate or suspend.

**Note**: This parameter can be used only with combination of `suspended`, `processDefinitionTenantId`, and `processDefinitionWithoutTenantId`." />

    <@lib.property
        name = "processDefinitionTenantId"
        type = "string"
        description = "Only activate or suspend process instances of a process definition which belongs to a tenant with the given id.

**Note**: This parameter can be used only with combination of `suspended`, `processDefinitionKey`, and `processDefinitionWithoutTenantId`." />

    <@lib.property
        name = "processDefinitionWithoutTenantId"
        type = "boolean"
        description = "Only activate or suspend process instances of a process definition which belongs to no tenant.
Value may only be true, as false is the default behavior.

**Note**: This parameter can be used only with combination of `suspended`, `processDefinitionKey`, and `processDefinitionTenantId`." />

    <@lib.property
        name = "processInstanceIds"
        type = "array"
        itemType = "string"
        description = "A list of process instance ids which defines a group of process instances
which will be activated or suspended by the operation.

**Note**: This parameter can be used only with combination of `suspended`, `processInstanceQuery`, and `historicProcessInstanceQuery`." />

    "processInstanceQuery": {
      "$ref": "#/components/schemas/ProcessInstanceQueryDto"
    },
    "historicProcessInstanceQuery": {
      "$ref": "#/components/schemas/HistoricProcessInstanceQueryDto"
    }
  }
}