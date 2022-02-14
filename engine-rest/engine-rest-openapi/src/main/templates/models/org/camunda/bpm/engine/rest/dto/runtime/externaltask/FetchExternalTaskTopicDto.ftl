<#macro dto_macro docsUrl="">
<@lib.dto
    required = [ "topicName", "lockDuration" ] >

    <@lib.property
        name = "topicName"
        type = "string"
        nullable = false
        desc = "**Mandatory.** The topic's name." />

    <@lib.property
        name = "lockDuration"
        type = "integer"
        format = "int64"
        desc = "**Mandatory.** The duration to lock the external tasks for in milliseconds." />

    <@lib.property
        name = "variables"
        type = "array"
        itemType = "string"
        desc = "A JSON array of `String` values that represent variable names. For each result task belonging to this
                topic, the given variables are returned as well if they are accessible from the external task's
                execution. If not provided - all variables will be fetched." />

    <@lib.property
        name = "localVariables"
        type = "boolean"
        defaultValue = 'false'
        desc = "If `true` only local variables will be fetched." />

    <@lib.property
        name = "businessKey"
        type = "string"
        desc = "A `String` value which enables the filtering of tasks based on process instance business key." />

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "Filter tasks based on process definition id." />

    <@lib.property
        name = "processDefinitionIdIn"
        type = "array"
        itemType = "string"
        desc = "Filter tasks based on process definition ids." />

    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "Filter tasks based on process definition key." />

    <@lib.property
        name = "processDefinitionKeyIn"
        type = "array"
        itemType = "string"
        desc = "Filter tasks based on process definition keys." />

    <@lib.property
        name = "processDefinitionVersionTag"
        type = "string"
        desc = "Filter tasks based on process definition version tag." />

    <@lib.property
        name = "withoutTenantId"
        type = "boolean"
        defaultValue = 'false'
        desc = "Filter tasks without tenant id." />

    <@lib.property
        name = "tenantIdIn"
        type = "array"
        itemType = "string"
        desc = "Filter tasks based on tenant ids." />

    <@lib.property
        name = "processVariables"
        type = "object"
        addProperty = "\"additionalProperties\": true"
        desc = "A `JSON` object used for filtering tasks based on process instance variable values. A property name of
                the object represents a process variable name, while the property value represents the process variable
                value to filter tasks by." />

    <@lib.property
        name = "deserializeValues"
        type = "boolean"
        defaultValue = 'false'
        desc = "Determines whether serializable variable values (typically variables that store custom Java objects)
                should be deserialized on server side (default `false`).

                If set to `true`, a serializable variable will be deserialized on server side and transformed to JSON
                using [Jackson's](https://github.com/FasterXML/jackson) POJO/bean property introspection feature. Note
                that this requires the Java classes of the variable value to be on the REST API's classpath.

                If set to `false`, a serializable variable will be returned in its serialized format. For example, a
                variable that is serialized as XML will be returned as a JSON string containing XML." />

    <@lib.property
        name = "includeExtensionProperties"
        type = "boolean"
        defaultValue = 'false'
        last = true
        desc = "Determines whether custom extension properties defined in the BPMN activity of the external task (e.g.
                via the Extensions tab in the Camunda modeler) should be included in the response. Default: false" />


</@lib.dto>

</#macro>