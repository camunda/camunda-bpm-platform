<#-- Generated From File: camunda-docs-manual/public/reference/rest/job-definition/get-query/index.html -->

<#assign sortByValues = [
  '"jobDefinitionId"',
  '"activityId"',
  '"processDefinitionId"',
  '"processDefinitionKey"',
  '"jobType"',
  '"jobConfiguration"',
  '"tenantId"'
]>

<#if requestMethod == "GET">
  <#assign listSeparator = " and comma-separated">
<#elseif requestMethod == "POST">
  <#assign listSeparator = "">
</#if>
            
<#assign params = {
  "jobDefinitionId": {
    "type": "string",
    "desc": "Filter by job definition id."
  },
  "activityIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include job definitions which belong to one of the passed${listSeparator} activity ids."
  },
  "processDefinitionId": {
    "type": "string",
    "desc": "Only include job definitions which exist for the given process definition id."
  },
  "processDefinitionKey": {
    "type": "string",
    "desc": "Only include job definitions which exist for the given process definition key."
  },
  "jobType": {
    "type": "string",
    "desc": "Only include job definitions which exist for the given job type. See the
             [User Guide](${docsUrl}/user-guide/process-engine/the-job-executor/#job-creation)
             for more information about job types."
  },
  "jobConfiguration": {
    "type": "string",
    "desc": "Only include job definitions which exist for the given job configuration. For example: for
             timer jobs it is the timer configuration."
  },
  "active": {
    "type": "boolean",
    "desc": "Only include active job definitions. Value may only be `true`, as `false` is the default
             behavior."
  },
  "suspended": {
    "type": "boolean",
    "desc": "Only include suspended job definitions. Value may only be `true`, as `false` is the
             default behavior."
  },
  "withOverridingJobPriority": {
    "type": "boolean",
    "desc": "Only include job definitions that have an overriding job priority defined. The only
             effective value is `true`. If set to `false`, this filter is not applied."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Only include job definitions which belong to one of the passed${listSeparator} tenant
             ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include job definitions which belong to no tenant. Value may only be `true`, as
             `false` is the default behavior."
  },
  "includeJobDefinitionsWithoutTenantId": {
    "type": "boolean",
    "desc": "Include job definitions which belong to no tenant. Can be used in combination with
             `tenantIdIn`. Value may only be `true`, as `false` is the default behavior."
  }
}>
