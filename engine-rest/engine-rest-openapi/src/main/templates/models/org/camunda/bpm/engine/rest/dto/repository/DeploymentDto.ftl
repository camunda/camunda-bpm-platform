{
  "type": "object",
  "properties": {

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the deployment." />

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The tenant id of the deployment." />

    <@lib.property
        name = "deploymentTime"
        type = "string"
        format = "date-time"
        desc = "The time when the deployment was created." />

    <@lib.property
        name = "source"
        type = "string"
        desc = "The source of the deployment." />

    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the deployment." />

    <@lib.property
        name = "links"
        type = "array"
        dto = "AtomLink"
        desc = "Link to the newly created deployment with method, href and rel." />

    <@lib.property
        name = "deployedProcessDefinitions"
        type = "object"
        additionalProperties = true
        dto = "ProcessDefinitionDto"
        desc = "A JSON Object containing a property for each of the process definitions,
                which are successfully deployed with that deployment.
                The key is the process definition id, the value is a JSON Object corresponding to the process definition." />

    <@lib.property
        name = "deployedDecisionDefinitions"
        type = "object"
        additionalProperties = true
        dto = "DecisionDefinitionDto"
        desc = "A JSON Object containing a property for each of the decision definitions,
                which are successfully deployed with that deployment.
                The key is the decision definition id, the value is a JSON Object corresponding to the decision definition." />

    <@lib.property
        name = "deployedDecisionRequirementsDefinitions"
        type = "object"
        additionalProperties = true
        dto = "DecisionRequirementsDefinitionDto"
        desc = "A JSON Object containing a property for each of the decision requirements definitions,
                which are successfully deployed with that deployment.
                The key is the decision requirements definition id, the value is a JSON Object corresponding to the decision requirements definition." />

    <@lib.property
        name = "deployedCaseDefinitions"
        type = "object"
        additionalProperties = true
        dto = "CaseDefinitionDto"
        last = true
        desc = "A JSON Object containing a property for each of the case definitions,
                which are successfully deployed with that deployment.
                The key is the case definition id, the value is a JSON Object corresponding to the case definition." />

  }
}