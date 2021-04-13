<#macro dto_macro docsUrl="">
<@lib.dto
    extends = "VariableValueDto" >

        <@lib.property
            name = "local"
            type = "boolean"
            last = true
            desc = "Indicates whether the variable should be a local variable or not.
                    If set to true, the variable becomes a local variable of the execution
                    entering the target activity." />

</@lib.dto>
</#macro>