<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the decision output value."
    />
    
    <@lib.property
        name = "decisionInstanceId"
        type = "string"
        desc = "The id of the decision instance the output value belongs to."
    />
    
    <@lib.property
        name = "clauseId"
        type = "string"
        desc = "The id of the clause the output value belongs to."
    />
    
    <@lib.property
        name = "clauseName"
        type = "string"
        desc = "The name of the clause the output value belongs to."
    />
    
    <@lib.property
        name = "ruleId"
        type = "string"
        desc = "The id of the rule the output value belongs to."
    />
    
    <@lib.property
        name = "ruleOrder"
        type = "integer"
        format = "int32"
        desc = "The order of the rule the output value belongs to."
    />
    
    <@lib.property
        name = "errorMessage"
        type = "string"
        desc = "An error message in case a Java Serialized Object could not be de-serialized."
    />
    
    <@lib.property
        name = "variableName"
        type = "string"
        desc = "The name of the output variable."
    />
    
    <@lib.property
        name = "type"
        type = "string"
        desc = "The value type of the variable."
    />
    
    <@lib.property
        name = "createTime"
        type = "string"
        format = "date-time"
        desc = "The time the variable was inserted. 
                [Default format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />
    
    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The time after which the entry should be removed by the History Cleanup job.
                [Default format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />
    
    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        desc = "The process instance id of the root process instance that initiated the process
                containing this entry."
    />
    
    <@lib.property
        name = "value"
        type = "object"
        desc = "The variable's value. Value differs depending on the variable's type
                and on the `disableCustomObjectDeserialization` parameter."
    />
    
    <@lib.property
        name = "valueInfo"
        type = "object"
        addProperty = "\"additionalProperties\": true"
        desc = "A JSON object containing additional, value-type-dependent
                properties.

                For variables of type `Object`, the following properties are
                returned:

                * `objectTypeName`: A string representation of the object's type
                name.

                * `serializationDataFormat`: The serialization format used to store
                the variable."
        last = true
    />

</@lib.dto>
</#macro>
