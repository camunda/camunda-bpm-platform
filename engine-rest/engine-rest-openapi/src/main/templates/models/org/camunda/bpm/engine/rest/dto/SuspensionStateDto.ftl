<#macro dto_macro docsUrl="">
<@lib.dto>

    <#-- NOTE: When adding any changes, if necessary, to:
         * ProcessDefinitionSuspensionStateDto or JobSuspensionStateDto or create a new file
         * ... -->
    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "A Boolean value which indicates whether to activate or suspend a given instance 
                (e.g. process instance, job, job definition, or batch). When the value is set to true, 
                the given instance will be suspended and when the value is set to false, 
                the given instance will be activated."
        last = true />

</@lib.dto>

</#macro>