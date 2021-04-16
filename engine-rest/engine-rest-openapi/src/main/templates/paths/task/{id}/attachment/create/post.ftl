<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "addAttachment"
      tag = "Task Attachment"
      summary = "Create"
      desc = "Creates an attachment for a task." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to add the attachment to."/>

  ],

  <@lib.requestBody
      mediaType = "multipart/form-data"
      dto = "MultiFormAttachmentDto"
      examples = ['"example-1": {
                     "summary": "POST /task/aTaskId/attachment/create",
                     "description": "Post data for a new task attachment.",
                     "value": "------------------------------925df49a954b
                        Content-Disposition: form-data; name=\\"url\\"

                        http://my-attachment-content-url.de
                        ------------------------------925df49a954b
                        Content-Disposition: form-data; name=\\"attachment-name\\"

                        attachmentName
                        ------------------------------925df49a954b
                        Content-Disposition: form-data; name=\\"attachment-description\\"

                        attachmentDescription
                        ------------------------------925df49a954b
                        Content-Disposition: form-data; name=\\"attachment-type\\"

                        attachmentType
                        ------------------------------925df49a954b--"
                   }'] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "AttachmentDto"
        desc = "Request successful."
        examples = ['"example-1": {
                 "summary": "Status 200 Response",
                 "value": {
                   "id": "attachmentId",
                   "name": "attachmentName",
                   "taskId": "aTaskId",
                   "description": "attachmentDescription",
                   "type": "attachmentType",
                   "url": "http://my-attachment-content-url.de",
                   "createTime":"2017-02-10T14:33:19.000+0200",
                   "removalTime":"2018-02-10T14:33:19.000+0200",
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
        code = "400"
        dto = "ExceptionDto"
        desc = "The task does not exists or task id is null. No content or url to remote content exists. See the
                [Introduction](/reference/rest/overview/#error-handling) for the error response format." />

    <@lib.response
        code = "403"
        dto = "AuthorizationExceptionDto"
        last = true
        desc = "The history of the engine is disabled. See the [Introduction](/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>