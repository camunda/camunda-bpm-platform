<#macro dto_macro docsUrl="">
<@lib.dto
    desc = "A group instance query which defines a list of group instances" >

    <#assign requestMethod="POST"/>
    <#include "/lib/commons/group-query-params.ftl" >
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