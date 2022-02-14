<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/batch/get-query/index.html -->

<#assign sortByValues = [
  '"batchId"',
  '"startTime"',
  '"endTime"',
  '"tenantId"'
]>

            
<#assign params = {
  "batchId": {
    "type": "string",
    "desc": "Filter by batch id."
  },
  "type": {
    "type": "string",
    "desc": "Filter by batch type. See the
             [User Guide](${docsUrl}/user-guide/process-engine/batch/#creating-a-batch)
             for more information about batch types."
  },
  "completed": {
    "type": "boolean",
    "desc": "
             Filter completed or not completed batches. If the value is
             `true`, only completed batches, i.e., end time is set, are
             returned. Otherwise, if the value is `false`, only running
             batches, i.e., end time is null, are returned."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a comma-separated list of tenant ids. A batch matches if it has one of the given
             tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include batches which belong to no tenant. Value can effectively only be `true`, as `false` is the default behavior."
  }
}>
