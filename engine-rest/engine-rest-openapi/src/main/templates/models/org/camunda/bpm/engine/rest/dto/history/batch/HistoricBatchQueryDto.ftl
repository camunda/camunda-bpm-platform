<#macro dto_macro docsUrl="">
<@lib.dto
    desc = "Query for the historic batches to set the removal time for." >

    <#include "/lib/commons/historic-batch-params.ftl" >

    <@lib.properties params/>
    "sorting": {
       "type": "array",
       "nullable": true,
       "description": "An array of criteria to sort the result by. Each element of the array is
                       an object that specifies one ordering. The position in the array
                       identifies the rank of an ordering, i.e., whether it is primary, secondary,
                       etc. Has no effect for the `/count` endpoint",
       "items":
         <#assign last = true>
         <#include "/lib/commons/sort-props.ftl">
    }

</@lib.dto>
</#macro>