{
  "type": "object",
  "properties": {

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the task comment." />

    <@lib.property
        name = "userId"
        type = "string"
        desc = "The id of the user who created the comment." />

    <@lib.property
        name = "taskId"
        type = "string"
        desc = "The id of the task to which the comment belongs." />

    <@lib.property
        name = "time"
        type = "string"
        format = "date-time"
        desc = "The time when the comment was created." />

    <@lib.property
        name = "message"
        type = "string"
        desc = "The content of the comment." />

    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The time after which the comment should be removed by the History Cleanup job.
                [Default format]($(docsUrl)/reference/rest/overview/date-format/)
                `yyyy-MM-dd'T'HH:mm:ss.SSSZ`." />

    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        desc = "The process instance id of the root process instance that initiated the process
                containing the task." />

    <@lib.property
        name = "links"
        type = "array"
        dto = "AtomLink"
        last = true
        desc = "Link to the newly created task comment with `method`, `href` and `rel`." />

  }
}