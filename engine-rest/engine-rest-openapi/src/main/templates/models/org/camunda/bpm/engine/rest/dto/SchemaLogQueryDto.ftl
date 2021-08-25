<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "version"
        type = "string"
        desc = "The version of the schema."/>

    "sorting": {
      "type": "array",
      "nullable": true,
      "description": "A JSON array of criteria to sort the result by. Each element of the array is
                      a JSON object that specifies one ordering. The position in the array
                      identifies the rank of an ordering, i.e., whether it is primary, secondary,
                      etc. ",
      "items":
        <#assign last = true >
        <#assign sortByValues = ['"timestamp"']>
        <#include "/lib/commons/sort-props.ftl" >
    }

</@lib.dto>

</#macro>