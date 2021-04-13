<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getAttachments"
      tag = "Task Attachment"
      summary = "Get List"
      desc = "Gets the attachments for a task." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to retrieve the attachments for." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "AttachmentDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /task/aTaskId/attachment",
                       "value": [
                         {
                           "id": "attachmentId",
                           "name": "attachmentName",
                           "taskId": "aTaskId",
                           "description": "attachmentDescription",
                           "type": "attachmentType",
                           "url": "http://my-attachment-content-url.de",
                           "createTime": "2017-02-10T14:33:19.000+0200",
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId"
                         },
                         {
                           "id": "anotherAttachmentId",
                           "name": "anotherAttachmentName",
                           "taskId": "aTaskId",
                           "description": "anotherAttachmentDescription",
                           "type": "anotherAttachmentType",
                           "url": "http://my-another-attachment-content-url.de",
                           "createTime": "2017-02-10T14:33:19.000+0200",
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId"
                         },
                         {
                           "id": "yetAnotherAttachmentId",
                           "name": "yetAnotherAttachmentName",
                           "taskId": "aTaskId",
                           "description": "yetAnotherAttachmentDescription",
                           "type": "yetAnotherAttachmentType",
                           "url": "http://yet-another-attachment-content-url.de",
                           "createTime": "2017-02-10T14:33:19.000+0200",
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId"
                         }
                       ]
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "No task exists for the given task id. See the [Introduction](/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>