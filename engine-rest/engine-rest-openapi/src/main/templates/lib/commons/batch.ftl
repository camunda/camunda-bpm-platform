<#assign sortByValues = [
  '"batchId"',
  '"tenantId"'
]>

<#if requestMethod == "GET">
  <#assign listTypeDescription = "Filter by a comma-separated list of `Strings`." />
<#elseif requestMethod == "POST">
  <#assign listTypeDescription = "Must be a JSON array of `Strings`">
</#if>

<#assign params = {
  "batchId": {
    "type": "string",
    "desc": "Filter by batch id."
  },

  "type": {
    "type": "string",
    "desc": "Filter by batch type.
             See the [User Guide](${docsUrl}/user-guide/process-engine/batch/#creating-a-batch)
             for more information about batch types."
  },

  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "${listTypeDescription} A batch matches if it has one of the given tenant ids."
  },

  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include batches which belong to no tenant.
             Value can effectively only be `true`, as `false` is the default behavior."
  },

  "suspended": {
    "type": "boolean",
    "desc": "A `Boolean` value which indicates whether only active or suspended batches should be included.
             When the value is set to `true`, only suspended batches will be returned and
             when the value is set to `false`, only active batches will be returned."
  },

  "createdBy": {
    "type": "string",
    "desc": "Only include batches that were started by this user id."
  },

  "startedBefore": {
    "type": "string",
    "format": "date-time",
    "desc": "Only include batches that were started before the given date. By [default](${docsUrl}/reference/rest/overview/date-format/),
             the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },

  "startedAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Only include batches that were started after the given date. By [default](${docsUrl}/reference/rest/overview/date-format/),
             the date must have the format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g., `2013-01-23T14:42:45.000+0200`."
  },

  "withFailures": {
    "type": "boolean",
    "desc": "Only include batches having jobs with failures.
             Value can only be `true`."
  },

  "withoutFailures": {
    "type": "boolean",
    "desc": "Only include batches having jobs without failures.
             Value can only be `true`."
  }
}>