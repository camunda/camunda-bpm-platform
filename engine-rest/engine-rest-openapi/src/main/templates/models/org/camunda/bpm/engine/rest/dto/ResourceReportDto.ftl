{
  "type" : "object",
  "properties" : {

    <@lib.property
        name = "errors"
        type = "array"
        dto = "ProblemDto"
        description = "A list of errors occurred during parsing."/>

    <@lib.property
        name = "warnings"
        type = "array"
        dto = "ProblemDto"
        last = true
        description = "A list of warnings occurred during parsing."/>

  }
}