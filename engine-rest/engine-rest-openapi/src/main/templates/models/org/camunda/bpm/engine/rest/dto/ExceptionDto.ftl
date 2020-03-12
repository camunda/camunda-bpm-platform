{
  "title": "ExceptionDto",
  "type": "object",
  "properties": {

    <@lib.property
        name = "type"
        type = "string"
        desc = "An exception class indicating the occurred error." />

    <@lib.property
        name = "message"
        type = "string"
        last = true
        desc = "A detailed message of the error." />

  }
}