<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "deleteAttachment"
      tag = "Task Attachment"
      summary = "Delete"
      desc = "Removes an attachment from a task by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the task." />

    <@lib.parameter
        name = "attachmentId"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the attachment to be removed." />

  ],

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "403"
        dto = "AuthorizationExceptionDto"
        desc = "The history of the engine is disabled. See the [Introduction](/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "A Task Attachment for the given task id and attachment id does not exist. See the
                [Introduction](/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>