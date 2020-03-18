{
  "allOf": [
    {
      "$ref": "#/components/schemas/VariableValueDto"
    },
    <@lib.dto
        type = "object" >

        <@lib.property
            name = "local"
            type = "boolean"
            last = true
            desc = "Indicates whether the variable should be a local variable or not.
                    If set to true, the variable becomes a local variable of the execution entering the target activity." />

    </@lib.dto>
  ]
}