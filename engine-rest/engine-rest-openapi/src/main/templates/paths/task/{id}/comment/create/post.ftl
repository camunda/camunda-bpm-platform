<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "createComment"
      tag = "Task Comment"
      summary = "Create"
      desc = "Creates a comment for a task by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to add the comment to." />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "CommentDto"
      requestDesc = "**Note:** Only the `message` and `processInstanceId` properties will be used. Every other property passed to this endpoint will be ignored."
      examples = ['"example-1": {
                       "summary": "POST /task/aTaskId/comment/create",
                       "value": {
                         "message": "a task comment",
                         "processInstanceId": "96dc383f-23eb-11e6-8e4a-f6aefe19b687"
                       }
                     }'] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CommentDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "value": {
                         "links": [
                           {
                             "method": "GET",
                             "href": "http://localhost:38080/rest-test/task/aTaskId/comment/aTaskCommentId",
                             "rel": "self"
                           }
                         ],
                         "id": "aTaskCommentId",
                         "userId": "userId",
                         "taskId": "aTaskId",
                         "processInstanceId": "96dc383f-23eb-11e6-8e4a-f6aefe19b687",
                         "time": "2013-01-02T21:37:03.887+0200",
                         "message": "comment message",
                         "removalTime": "2018-02-10T14:33:19.000+0200",
                         "rootProcessInstanceId": "aRootProcessInstanceId"
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The task does not exist or no comment message was submitted. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "403"
        dto = "AuthorizationExceptionDto"
        last = true
        desc = "The history of the engine is disabled. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>
