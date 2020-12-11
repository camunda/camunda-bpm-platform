<@lib.dto
    desc = "A variable instance query which defines a list of variable instances" >

    <#include "/lib/commons/variable-instance-query-params.ftl" >
    <@lib.properties params />

    "sorting": {
      "type": "array",
      "description": "Apply sorting of the result",
      "items":

        <#assign last = true >
        <#include "/lib/commons/sort-props.ftl" >

    }

</@lib.dto>