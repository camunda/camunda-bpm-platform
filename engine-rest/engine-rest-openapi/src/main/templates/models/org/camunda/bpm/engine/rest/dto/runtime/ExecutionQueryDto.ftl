<#macro dto_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/post-query/index.html -->
<@lib.dto desc = "A Execution instance query which defines a list of Execution instances">
    
<#assign requestMethod="POST">
    <#include "/lib/commons/execution-query-params.ftl">
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