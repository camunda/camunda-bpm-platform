<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "links"
        type = "array"
        dto = "AtomLink"
        last = true
        desc = "The links associated to this resource, with `method`, `href` and `rel`." />

</@lib.dto>
</#macro>