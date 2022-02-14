<#macro dto_macro docsUrl="">
<@lib.dto
    desc = "A historic decision instance query which defines a list of historic decision instances" >
    
    <#assign last = false >
    <#include "/lib/commons/historic-decision-instance-query-params.ftl" >
    <@lib.properties params />
    <#include "/lib/commons/historic-decision-instance-single-query-params.ftl" >
    <@lib.properties object=params last=true />

</@lib.dto>
</#macro>