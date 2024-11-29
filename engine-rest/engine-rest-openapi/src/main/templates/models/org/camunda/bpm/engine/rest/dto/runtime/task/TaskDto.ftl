<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The task id." />

    <@lib.property
        name = "name"
        type = "string"
        desc = "The task name." />

    <@lib.property
        name = "assignee"
        type = "string"
        desc = "The assignee's id." />

    <@lib.property
        name = "owner"
        type = "string"
        desc = "The owner's id." />

    <@lib.property
        name = "created"
        type = "string"
        format = "date-time"
        desc = "The date the task was created on.
                [Default format](${docsUrl}/reference/rest/overview/date-format/)
                `yyyy-MM-dd'T'HH:mm:ss.SSSZ`." />

    <@lib.property
        name = "lastUpdated"
        type = "string"
        format = "date-time"
        desc = "The date the task was last updated. Every action that fires a [task update event](${docsUrl}/user-guide/process-engine/delegation-code/#task-listener-event-lifecycle) will update this property.
                [Default format](${docsUrl}/reference/rest/overview/date-format/)
                `yyyy-MM-dd'T'HH:mm:ss.SSSZ`." />

    <@lib.property
        name = "due"
        type = "string"
        format = "date-time"
        desc = "The task's due date.
                [Default format](${docsUrl}/reference/rest/overview/date-format/)
                `yyyy-MM-dd'T'HH:mm:ss.SSSZ`." />

    <@lib.property
        name = "followUp"
        type = "string"
        format = "date-time"
        desc = "The follow-up date for the task.
                [Default format](${docsUrl}/reference/rest/overview/date-format/)
                `yyyy-MM-dd'T'HH:mm:ss.SSSZ`." />

    <@lib.property
        name = "delegationState"
        type = "string"
        enumValues=['"PENDING"', '"RESOLVED"']
        desc = "The task's delegation state. Possible values are `PENDING` and `RESOLVED`." />

    <@lib.property
        name = "description"
        type = "string"
        desc = "The task's description." />

    <@lib.property
        name = "executionId"
        type = "string"
        desc = "The id of the execution the task belongs to." />

    <@lib.property
        name = "parentTaskId"
        type = "string"
        desc = "The id the parent task, if this task is a subtask." />

    <@lib.property
        name = "priority"
        type = "integer"
        format = "int32"
        desc = "The task's priority." />

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition the task belongs to." />

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The id of the process instance the task belongs to." />

    <@lib.property
        name = "caseExecutionId"
        type = "string"
        desc = "The id of the case execution the task belongs to." />

    <@lib.property
        name = "caseDefinitionId"
        type = "string"
        desc = "The id of the case definition the task belongs to." />

    <@lib.property
        name = "caseInstanceId"
        type = "string"
        desc = "The id of the case instance the task belongs to." />

    <@lib.property
        name = "taskDefinitionKey"
        type = "string"
        desc = "The task's key." />

    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "Whether the task belongs to a process instance that is suspended." />

    <@lib.property
        name = "formKey"
        type = "string"
        desc = "If not `null`, the form key for the task." />

    <@lib.property
        name = "camundaFormRef"
        type = "ref"
        dto = "CamundaFormRef"
        desc = "A reference to a specific version of a Camunda Form."/>

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "If not `null`, the tenant id of the task." />

    <@lib.property
        name = "taskState"
        type = "string"
        desc = "The task's state."
        last = true />

</@lib.dto>
</#macro>
