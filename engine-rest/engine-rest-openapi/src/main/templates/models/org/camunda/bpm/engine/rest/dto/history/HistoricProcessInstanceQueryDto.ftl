<#macro dto_macro docsUrl="">
<@lib.dto
    desc = "A historic process instance query which defines a group of historic process instances" >

    <#assign requestMethod="POST"/>
    <#include "/lib/commons/history-process-instance.ftl" >
    <@lib.properties params />

    "sorting": {
      "type": "array",
      "nullable": true,
      "description": "Apply sorting of the result",
      "items":

        <#assign last = true >
        <#include "/lib/commons/sort-props.ftl" >

    }

</@lib.dto>
</#macro>