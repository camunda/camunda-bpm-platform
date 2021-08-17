<#macro dto_macro docsUrl="">
<@lib.dto
    desc = "A historic detail query which defines a group of historic details." >

  <#assign requestMethod="POST"/>
  <#include "/lib/commons/history-detail-query-params.ftl" >
  <@lib.properties params />
  "sorting": {
    "type": "array",
    "nullable": true,
    "description": "A JSON array of criteria to sort the result by. Each element of the array is
                    a JSON object that specifies one ordering. The position in the array
                    identifies the rank of an ordering, i.e., whether it is primary, secondary,
                    etc. Does not have an effect for the `count` endpoint.",
    "items":
      <#assign last = true>
      <#include "/lib/commons/sort-props.ftl">
  }

</@lib.dto>
</#macro>