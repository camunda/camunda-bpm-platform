<#macro dto_macro docsUrl="">
<@lib.dto>

  <@lib.property
      name = "historicProcessInstanceIds"
      type = "array"
      itemType = "string"
      desc = "A list historic process instance ids to delete."/>

  <@lib.property
      name = "historicProcessInstanceQuery"
      type = "ref"
      dto = "HistoricProcessInstanceQueryDto"
      desc = "A historic process instance query like the request body described by
              [POST /history/process-instance](${docsUrl}/reference/rest/history/process-instance/post-process-instance-query/)."/>

  <@lib.property
      name = "deleteReason"
      type = "string"
      desc = "A string with delete reason."/>

  <@lib.property
      name = "failIfNotExists"
      type = "boolean"
      last = true
      desc = "If set to `false`, the request will still be successful if one ore more of the process ids are not found."/>

</@lib.dto>
</#macro>