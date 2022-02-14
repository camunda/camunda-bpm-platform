<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getAttachment"
      tag = "Task Attachment"
      summary = "Get"
      desc = "Retrieves a task attachment by task id and attachment id." />

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
        desc = "The id of the attachment to be retrieved." />
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "AttachmentDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /task/aTaskId/attachment/aTaskAttachmentId",
                       "value": {
                         "id": "attachmentId",
                         "name": "attachmentName",
                         "taskId": "aTaskId",
                         "description": "attachmentDescription",
                         "type": "attachmentType",
                         "url": "http://my-attachment-content-url.de",
                         "createTime": "2017-02-10T14:33:19.000+0200",
                         "removalTime": "2018-02-10T14:33:19.000+0200",
                         "rootProcessInstanceId": "aRootProcessInstanceId",
                         "links": [
                           {
                             "method": "GET",
                             "href": "http://localhost:38080/rest-test/task/aTaskId/attachment/aTaskAttachmentId",
                             "rel": "self"
                           }
                         ]
                       }
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "The attachment for the given task and attachment id does not exist or the history of the engine is
                disabled.

                See the [Introduction](/reference/rest/overview/#error-handling) for the error response format." />

  }
}

</#macro>