{
  "type": "object",
  "properties": {

    <@lib.property
        name = "rel"
        type = "string"
        description = "The relation of the link to the object that belogs to." />

    <@lib.property
        name = "href"
        type = "string"
        description = "The url of the link." />

    <@lib.property
        name = "method"
        type = "string"
        last = true
        description = "The http method." />

  }
}