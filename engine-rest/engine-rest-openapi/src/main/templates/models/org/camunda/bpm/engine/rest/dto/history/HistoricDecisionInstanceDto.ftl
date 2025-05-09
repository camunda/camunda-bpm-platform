<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the decision instance."
    />
    
    <@lib.property
        name = "decisionDefinitionId"
        type = "string"
        desc = "The id of the decision definition that this decision instance belongs to."
    />
    
    <@lib.property
        name = "decisionDefinitionKey"
        type = "string"
        desc = "The key of the decision definition that this decision instance belongs to."
    />
    
    <@lib.property
        name = "decisionDefinitionName"
        type = "string"
        desc = "The name of the decision definition that this decision instance belongs to."
    />
    
    <@lib.property
        name = "evaluationTime"
        type = "string"
        format = "date-time"
        desc = "The time the instance was evaluated. 
                [Default format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />
    
    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The time after which the instance should be removed by the History Cleanup job.
                [Default format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />
    
    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition that this decision instance belongs to."
    />
    
    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The key of the process definition that this decision instance belongs to."
    />
    
    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The id of the process instance that this decision instance belongs to."
    />
    
    <@lib.property
        name = "caseDefinitionId"
        type = "string"
        desc = "The id of the case definition that this decision instance belongs to."
    />
    
    <@lib.property
        name = "caseDefinitionKey"
        type = "string"
        desc = "The key of the case definition that this decision instance belongs to."
    />
    
    <@lib.property
        name = "caseInstanceId"
        type = "string"
        desc = "The id of the case instance that this decision instance belongs to."
    />
    
    <@lib.property
        name = "activityId"
        type = "string"
        desc = "The id of the activity that this decision instance belongs to."
    />
    
    <@lib.property
        name = "activityInstanceId"
        type = "string"
        desc = "The id of the activity instance that this decision instance belongs to."
    />
    
    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The tenant id of the historic decision instance."
    />
    
    <@lib.property
        name = "userId"
        type = "string"
        desc = "The id of the authenticated user that has evaluated this decision instance without
                    a process or case instance."
    />
    
    <@lib.property
        name = "inputs"
        type = "array"
        dto = "HistoricDecisionInputInstanceDto"
        desc = "The list of decision input values. **Only exists** if `includeInputs`
                was set to `true` in the query."
    />
    
    <@lib.property
        name = "outputs"
        type = "array"
        dto = "HistoricDecisionOutputInstanceDto"
        desc = "The list of decision output values. **Only exists** if `includeOutputs`
                was set to `true` in the query."
    />
    
    <@lib.property
        name = "collectResultValue"
        type = "number"
        format = "double"
        desc = "The result of the collect aggregation of the decision result if used. `null` if no
                aggregation was used."
    />
    
    <@lib.property
        name = "rootDecisionInstanceId"
        type = "string"
        desc = "The decision instance id of the evaluated root decision. Can be `null` if this
                instance is the root decision instance of the evaluation."
    />
    
    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        desc = "The process instance id of the root process instance that initiated the evaluation
                of this decision. Can be `null` if this decision instance is not
                evaluated as part of a BPMN process."
    />
    
    <@lib.property
        name = "decisionRequirementsDefinitionId"
        type = "string"
        desc = "The id of the decision requirements definition that this decision instance belongs
                to."
    />
    
    <@lib.property
        name = "decisionRequirementsDefinitionKey"
        type = "string"
        desc = "The key of the decision requirements definition that this decision instance belongs
                to."
        last = true
    />

</@lib.dto>
</#macro>
