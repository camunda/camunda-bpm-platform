<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/job-log/post-job-log-query/index.html -->
<#macro dto_macro docsUrl="">
<@lib.dto desc = "A Historic Job Log instance query which defines a list of Historic Job Log instances">
    
    <#include "/lib/commons/history-job-log-params.ftl">
    <@lib.properties params/>    
    "sorting": {
       "type": "array",
       "nullable": true,
       "description": "An array of criteria to sort the result by. Each element of the array is
                       an object that specifies one ordering. The position in the array
                       identifies the rank of an ordering, i.e., whether it is primary, secondary,
                       etc. Sorting has no effect for `count` endpoints",
       "items":
         <#assign last = true>
         <#include "/lib/commons/sort-props.ftl">
    }

</@lib.dto>
</#macro>