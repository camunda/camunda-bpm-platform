<@lib.dto
    desc = "A group instance query which defines a list of group instances" >

    <#include "/lib/commons/group-query-params.ftl" >
    <@lib.properties params />

    "sorting": {
      "type": "array",
      "description": "Apply sorting of the result",
      "items":
        <#assign last = true >
        <#include "/lib/commons/sort-props.ftl" >

    }

</@lib.dto>