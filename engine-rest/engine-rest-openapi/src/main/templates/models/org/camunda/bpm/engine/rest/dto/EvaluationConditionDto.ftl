<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "variables"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "A map of variables which are used for evaluation of the conditions and are injected into the process instances which have been triggered.
                Each key is a variable name and each value a JSON variable value object with the following properties."/>

    <@lib.property
        name = "businessKey"
        type = "string"
        desc = "Used for the process instances that have been triggered after the evaluation."/>

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "Used to evaluate a condition for a tenant with the given id.
                Will only evaluate conditions of process definitions which belong to the tenant."/>

    <@lib.property
        name = "withoutTenantId"
        type = "boolean"
        desc = "A Boolean value that indicates whether the conditions should only be evaluated of process definitions which belong to no tenant or not.
                Value may only be true, as false is the default behavior."/>

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        last = true
        desc = "Used to evaluate conditions of the process definition with the given id."/>

</@lib.dto>

</#macro>