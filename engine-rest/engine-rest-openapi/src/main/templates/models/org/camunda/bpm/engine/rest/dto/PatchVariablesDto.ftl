{
  "type" : "object",
  "properties" : {

    <@lib.property
        name = "modifications"
        type = "object"
        additionalProperties = true
        dto = "VariableValueDto"
        description = "A JSON object containing variable key-value pairs." />

    <@lib.property
        name = "deletions"
        type = "array"
        itemType = "string"
        last = true
        description = "An array of String keys of variables to be deleted."/>
  }
}