<@lib.dto>

    <#-- NOTE: Please adding any changes, if necessary, to:
         * ProcessDefinitionSuspensionStateDto or JobSuspensionStateDto or create a new file
         * ... -->
    <@lib.property
        name = "suspended"
        type = "boolean"
        desc = "A Boolean value which indicates whether to activate or suspend a given instance (e.g. process instance, job, or batch).
                When the value is set to true, the given instance will be suspended and when the value is set to false,
                the given instance will be activated."
        last = true />

</@lib.dto>
