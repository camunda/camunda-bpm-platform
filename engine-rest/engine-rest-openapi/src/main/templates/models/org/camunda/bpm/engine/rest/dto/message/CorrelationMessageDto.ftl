<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "messageName"
        type = "string"
        desc = "The name of the message to deliver."/>

    <@lib.property
        name = "businessKey"
        type = "string"
        desc = "Used for correlation of process instances that wait for incoming messages.
                Will only correlate to executions that belong to a process instance with the provided business key."/>

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "Used to correlate the message for a tenant with the given id.
                Will only correlate to executions and process definitions which belong to the tenant.
                Must not be supplied in conjunction with a `withoutTenantId`."/>

    <@lib.property
        name = "withoutTenantId"
        type = "boolean"
        defaultValue = 'false'
        desc = "A Boolean value that indicates whether the message should only be correlated to executions
                and process definitions which belong to no tenant or not. Value may only be `true`, as `false`
                is the default behavior.
                Must not be supplied in conjunction with a `tenantId`."/>

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "Used to correlate the message to the process instance with the given id."/>

    <@lib.property
        name = "correlationKeys"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "Used for correlation of process instances that wait for incoming messages.
                Has to be a JSON object containing key-value pairs that are matched against process instance variables
                during correlation. Each key is a variable name and each value a JSON variable value object with the
                following properties."/>

    <@lib.property
        name = "localCorrelationKeys"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "Local variables used for correlation of executions (process instances) that wait for incoming messages.
                Has to be a JSON object containing key-value pairs that are matched against local variables during correlation.
                Each key is a variable name and each value a JSON variable value object with the following properties."/>

    <@lib.property
        name = "processVariables"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "A map of variables that is injected into the triggered execution or process instance after the message
                has been delivered. Each key is a variable name and each value a JSON variable value object with
                the following properties."/>

    <@lib.property
        name = "processVariablesLocal"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "A map of local variables that is injected into the execution waiting on the message.
                Each key is a variable name and each value a JSON variable value object
                with the following properties."/>

    <@lib.property
        name = "processVariablesToTriggeredScope"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "A map of variables that is injected into the new scope triggered by message correlation.
                Each key is a variable name and each value a JSON variable value object
                with the following properties."/>

    <@lib.property
        name = "all"
        type = "boolean"
        defaultValue = 'false'
        desc = "A Boolean value that indicates whether the message should be correlated to exactly one entity or multiple entities.
                If the value is set to `false`, the message will be correlated to exactly one entity (execution or process definition).
                If the value is set to `true`, the message will be correlated to multiple executions and a process definition that
                can be instantiated by this message in one go."/>

    <@lib.property
        name = "resultEnabled"
        type = "boolean"
        defaultValue = 'false'
        desc = "A Boolean value that indicates whether the result of the correlation should be returned or not.
                If this property is set to `true`, there will be returned a list of message correlation result objects. Depending on the
                all property, there will be either one ore more returned results in the list.

                The default value is `false`, which means no result will be returned."/>

    <@lib.property
        name = "variablesInResultEnabled"
        type = "boolean"
        defaultValue = 'false'
        last = true
        desc = "A Boolean value that indicates whether the result of the correlation should contain process variables or not.
                The parameter resultEnabled should be set to `true` in order to use this it.

                The default value is `false`, which means the variables will not be returned."/>

</@lib.dto>
</#macro>