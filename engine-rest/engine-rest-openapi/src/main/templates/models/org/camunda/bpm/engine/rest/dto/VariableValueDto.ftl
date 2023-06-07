<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "value"
        type = "ref"
        dto = "AnyValue"
        desc = "The variable's value. Value differs depending on the variable's type and on the deserializeValues parameter.
                For variables of type `File` the value has to be submitted as Base64 encoded string."/>

    <@lib.property
        name = "type"
        type = "string"
        desc = "The value type of the variable."/>

    <@lib.property
        name = "valueInfo"
        type = "object"
        last = true
        addProperty = "\"additionalProperties\": true"
        desc = "A JSON object containing additional, value-type-dependent properties.
                For serialized variables of type Object, the following properties can be provided:

                * `objectTypeName`: A string representation of the object's type name.
                * `serializationDataFormat`: The serialization format used to store the variable.

                For serialized variables of type File, the following properties can be provided:

                * `filename`: The name of the file. This is not the variable name but the name that will be used when downloading the file again.
                * `mimetype`: The MIME type of the file that is being uploaded.
                * `encoding`: The encoding of the file that is being uploaded.

                The following property can be provided for all value types:

                * `transient`: Indicates whether the variable should be transient or
                not. See [documentation](${docsUrl}/user-guide/process-engine/variables#transient-variables) for more informations.
                (Not applicable for `decision-definition`, ` /process-instance/variables-async`, and `/migration/executeAsync` endpoints)"
    />





</@lib.dto>
</#macro>
