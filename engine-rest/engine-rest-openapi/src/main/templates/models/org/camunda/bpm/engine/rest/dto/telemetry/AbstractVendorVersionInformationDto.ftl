<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "vendor"
        type = "string"
        desc = "Information about the vendor."/>

    <@lib.property
        name = "version"
        type = "string"
        last = true
        desc = "Information about the version."/>

</@lib.dto>

</#macro>