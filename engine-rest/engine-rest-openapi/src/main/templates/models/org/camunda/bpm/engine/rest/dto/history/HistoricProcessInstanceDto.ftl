<#macro dto_macro docsUrl="">
<@lib.dto>
  <#assign dateFormatDescription = "Default [format](${docsUrl}/reference/rest/overview/date-format/) `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."/>

  <@lib.property
      name = "id"
      type = "string"
      desc = "The id of the process instance."/>

  <@lib.property
      name = "rootProcessInstanceId"
      type = "string"
      desc = "The process instance id of the root process instance that initiated the process."/>

  <@lib.property
      name = "superProcessInstanceId"
      type = "string"
      desc = "The id of the parent process instance, if it exists."/>

  <@lib.property
      name = "superCaseInstanceId"
      type = "string"
      desc = "The id of the parent case instance, if it exists."/>

  <@lib.property
      name = "caseInstanceId"
      type = "string"
      desc = "The id of the parent case instance, if it exists."/>

  <@lib.property
      name = "processDefinitionName"
      type = "string"
      desc = "The name of the process definition that this process instance belongs to."/>

  <@lib.property
      name = "processDefinitionKey"
      type = "string"
      desc = "The key of the process definition that this process instance belongs to."/>

  <@lib.property
      name = "processDefinitionVersion"
      type = "integer"
      format = "int32"
      desc = "The version of the process definition that this process instance belongs to."/>

  <@lib.property
      name = "processDefinitionId"
      type = "string"
      desc = "The id of the process definition that this process instance belongs to."/>

  <@lib.property
      name = "businessKey"
      type = "string"
      desc = "The business key of the process instance."/>

  <@lib.property
      name = "startTime"
      type = "string"
      format = "date-time"
      desc = "The time the instance was started. ${dateFormatDescription}"/>

  <@lib.property
      name = "endTime"
      type = "string"
      format = "date-time"
      desc = "The time the instance ended. ${dateFormatDescription}"/>

  <@lib.property
      name = "removalTime"
      type = "string"
      format = "date-time"
      desc = "The time after which the instance should be removed by the History Cleanup job. ${dateFormatDescription}"/>

  <@lib.property
      name = "durationInMillis"
      type = "integer"
      format = "int64"
      desc = "The time the instance took to finish (in milliseconds)."/>

  <@lib.property
      name = "startUserId"
      type = "string"
      desc = "The id of the user who started the process instance."/>

  <@lib.property
      name = "startActivityId"
      type = "string"
      desc = "The id of the initial activity that was executed (e.g., a start event)."/>

  <@lib.property
      name = "deleteReason"
      type = "string"
      desc = "The provided delete reason in case the process instance was canceled during execution."/>

  <@lib.property
      name = "tenantId"
      type = "string"
      desc = "The tenant id of the process instance."/>

  <@lib.property
      name = "state"
      type = "string"
      enumValues = ["ACTIVE", "SUSPENDED", "COMPLETED", "EXTERNALLY_TERMINATED", "INTERNALLY_TERMINATED"]
      desc = "Last state of the process instance, possible values are:

              `ACTIVE` - running process instance

              `SUSPENDED` - suspended process instances

              `COMPLETED` - completed through normal end event

              `EXTERNALLY_TERMINATED` - terminated externally, for instance through REST API

              `INTERNALLY_TERMINATED` - terminated internally, for instance by terminating boundary event"/>

  <@lib.property
      name = "restartedProcessInstanceId"
      type = "string"
      desc = "The id of the original process instance which was restarted."
      last = true />

</@lib.dto>
</#macro>