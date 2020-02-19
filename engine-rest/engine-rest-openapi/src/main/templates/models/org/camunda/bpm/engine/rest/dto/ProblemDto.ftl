{
  "type" : "object",
  "properties" : {

    <@lib.property
        name = "message"
        type = "string"
        description = "The message of the problem."/>

    <@lib.property
        name = "line"
        type = "integer"
        format = "int32"
        description = "The line where the problem occurred."/>


    <@lib.property
        name = "column"
        type = "integer"
        format = "int32"
        description = "The column where the problem occurred."/>

    <@lib.property
        name = "mainElementId"
        type = "string"
        description = "The main element id where the problem occurred."/>


    <@lib.property
        name = "elementIds"
        type = "array"
        itemType = "string"
        last = true
        description = "A list of element id affected by the problem."/>

  }
}