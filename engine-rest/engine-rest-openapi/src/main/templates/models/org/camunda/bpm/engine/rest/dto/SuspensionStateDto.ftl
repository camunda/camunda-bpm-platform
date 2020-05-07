<@lib.dto>

    <#-- NOTE: Please adding any changes, if necessary, to:
         * ProcessDefinitionSuspensionStateDto
         * ProcessInstanceSuspensionStateAsyncDto
         * ProcessInstanceSuspensionStateDto
         * ... -->
    <@lib.property
        name = "suspended"
        type = "boolean"
        nullable = false
        desc = "A Boolean value which indicates whether to activate or suspend a given process instance.
                When the value is set to true, the given process instance will be suspended and when the value is set to false,
                the given process instance will be activated."
        last = true />

</@lib.dto>
