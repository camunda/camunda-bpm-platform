<#macro dto_macro docsUrl="">
<@lib.dto
    title = ""
    required = [ "workerId", "maxTasks" ] >

    <@lib.property
        name = "workerId"
        type = "string"
        nullable = false
        desc = "**Mandatory.** The id of the worker on which behalf tasks are fetched. The returned tasks are locked for
                that worker and can only be completed when providing the same worker id." />

    <@lib.property
        name = "maxTasks"
        type = "integer"
        format = "int32"
        desc = "**Mandatory.** The maximum number of tasks to return." />

    <@lib.property
        name = "usePriority"
        type = "boolean"
        desc = "A `boolean` value, which indicates whether the task should be fetched based on its priority
                or arbitrarily." />

    <@lib.property
        name = "asyncResponseTimeout"
        type = "integer"
        format = "int64"
        desc = "The [Long Polling](${docsUrl}/user-guide/process-engine/external-tasks/#long-polling-to-fetch-and-lock-external-tasks)
                timeout in milliseconds.

                **Note:** The value cannot be set larger than 1.800.000 milliseconds (corresponds to 30 minutes)." />

    <@lib.property
        name = "topics"
        type = "array"
        dto = "FetchExternalTaskTopicDto"
        desc = "A JSON array of topic objects for which external tasks should be fetched. The returned tasks may be
                arbitrarily distributed among these topics. Each topic object has the following properties:" />

    "sorting": {
      "type": "array",
      "nullable": true,
      "description": "Apply sorting of the result",
      "items":

        <#assign last = true >
        <#assign sortByValues = [ '"createTime"' ] >
        <#include "/lib/commons/sort-props.ftl" >
    }

</@lib.dto>

</#macro>