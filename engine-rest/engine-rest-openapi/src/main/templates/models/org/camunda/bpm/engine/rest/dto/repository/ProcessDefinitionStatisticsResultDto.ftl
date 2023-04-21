<#macro dto_macro docsUrl="">
<@lib.dto>

    <#assign last = false >
    <#assign instanceType = "process definition" >
    <#include "/lib/commons/statistics-result-props.ftl" >

    <@lib.property
        name = "definition"
        type = "ref"
        dto = "ProcessDefinitionDto"
        last = true
        desc = "The process definition."/>

</@lib.dto>
</#macro>
