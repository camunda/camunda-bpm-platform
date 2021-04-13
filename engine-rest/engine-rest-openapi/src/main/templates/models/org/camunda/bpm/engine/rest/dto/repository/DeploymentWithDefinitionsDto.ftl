<#macro dto_macro docsUrl="">
<@lib.dto
    extends = "DeploymentDto" >

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

</@lib.dto>
</#macro>