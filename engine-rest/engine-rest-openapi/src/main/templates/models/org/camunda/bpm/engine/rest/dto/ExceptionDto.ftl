{
  "title": "ExceptionDto",
  "type": "object",
  "properties": {

    <@lib.property
        name = "type"
        type = "string"
        description = "An exception class indicating the occurred error." />

    <@lib.property
        name = "message"
        type = "string"
        last = true
        description = "A detailed message of the error." />

  }
}