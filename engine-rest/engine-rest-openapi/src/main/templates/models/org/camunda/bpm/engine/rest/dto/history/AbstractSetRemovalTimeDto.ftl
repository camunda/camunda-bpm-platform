<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "absoluteRemovalTime"
        type = "string"
        format = "date-time"
        desc = "The date for which the instances shall be removed. Value may not be `null`.

                **Note:** Cannot be set in conjunction with `clearedRemovalTime` or `calculatedRemovalTime`."/>

    <@lib.property
        name = "clearedRemovalTime"
        type = "boolean"
        desc = "Sets the removal time to `null`. Value may only be `true`, as `false` is the default behavior.

                **Note:** Cannot be set in conjunction with `absoluteRemovalTime` or `calculatedRemovalTime`."/>

    <@lib.property
        name = "calculatedRemovalTime"
        type = "boolean"
        last = true
        desc = "The removal time is calculated based on the engine's configuration settings. Value may only be `true`, as `false` is the default behavior.

                **Note:** Cannot be set in conjunction with `absoluteRemovalTime` or `clearedRemovalTime`."/>
</@lib.dto>
</#macro>