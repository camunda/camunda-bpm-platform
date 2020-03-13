{
  "title": "FormDto",
  "type": "object",
  "properties": {

    <@lib.property
        name = "key"
        type = "string"
        desc = "The form key for the task." />

    <@lib.property
        name = "contextPath"
        type = "string"
        last = true
        desc = "The process application's context path the task belongs to. If the task does not
                belong to a process application deployment or a process definition at all, this
                property is not set." />

  }
}