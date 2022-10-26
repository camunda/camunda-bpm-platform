<#macro dto_macro docsUrl="">
<@lib.dto>

  <@lib.property
      name = "groupName"
      type = "string"
      desc = "The name of the candidate group. If there are tasks without a group name, the value will be `null`"/>

  <@lib.property
      name = "taskCount"
      type = "integer"
      format = "int32"
      last = true
      desc = "The number of tasks which have the group name as candidate group."/>

</@lib.dto>
</#macro>