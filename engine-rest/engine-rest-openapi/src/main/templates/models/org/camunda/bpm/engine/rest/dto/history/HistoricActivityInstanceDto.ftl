<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the activity instance."/>

    <@lib.property
        name = "parentActivityInstanceId"
        type = "string"
        desc = "The id of the parent activity instance, for example a sub process instance."/>

    <@lib.property
        name = "activityId"
        type = "string"
        desc = "The id of the activity that this object is an instance of."/>

    <@lib.property
        name = "activityName"
        type = "string"
        desc = "The name of the activity that this object is an instance of."/>

    <@lib.property
        name = "activityType"
        type = "string"
        desc = "The type of the activity that this object is an instance of."/>

    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        desc = "The key of the process definition that this activity instance belongs to."/>

    <@lib.property
        name = "processDefinitionId"
        type = "string"
        desc = "The id of the process definition that this activity instance belongs to."/>

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The id of the process instance that this activity instance belongs to."/>

    <@lib.property
        name = "executionId"
        type = "string"
        desc = "The id of the execution that executed this activity instance."/>

    <@lib.property
        name = "taskId"
        type = "string"
        desc = "The id of the task that is associated to this activity instance. Is only set if the activity is a user task."/>

    <@lib.property
        name = "assignee"
        type = "string"
        desc = "The assignee of the task that is associated to this activity instance. Is only set if the activity is a user task."/>

    <@lib.property
        name = "calledProcessInstanceId"
        type = "string"
        desc = "The id of the called process instance. Is only set if the activity is a call activity and the called instance a process instance."/>

    <@lib.property
        name = "calledCaseInstanceId"
        type = "string"
        desc = "The id of the called case instance. Is only set if the activity is a call activity and the called instance a case instance."/>

    <@lib.property
        name = "startTime"
        type = "string"
        format = "date-time"
        desc = "The time the instance was started. Default format* `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."/>

    <@lib.property
        name = "endTime"
        type = "string"
        format = "date-time"
        desc = "The time the instance ended. Default format* `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."/>

    <@lib.property
        name = "durationInMillis"
        type = "integer"
        format = "int32"
        nullable = false
        desc = "The time the instance took to finish (in milliseconds)."/>

    <@lib.property
        name = "canceled"
        type = "boolean"
        defaultValue = "false"
        desc = "If `true`, this activity instance is canceled."/>

    <@lib.property
        name = "completeScope"
        type = "boolean"
        defaultValue = "false"
        desc = "If `true`, this activity instance did complete a BPMN 2.0 scope."/>

    <@lib.property
        name = "tenantId"
        type = "string"
        desc = "The tenant id of the activity instance."/>

    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The time after which the activity instance should be removed by the History Cleanup job. Default format* `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."/>

    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        last = true
        desc = "The process instance id of the root process instance that initiated the process containing this activity instance."/>

</@lib.dto>