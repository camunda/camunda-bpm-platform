<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getComment"
      tag = "Task Comment"
      summary = "Get"
      desc = "Retrieves a task comment by task id and comment id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the task." />

    <@lib.parameter
        name = "commentId"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the comment to be retrieved." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CommentDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /task/aTaskId/comment",
                       "value": {
                         "id": "aTaskCommentId",
                         "userId": "userId",
                         "taskId": "aTaskId",
                         "processInstanceId": "96dc383f-23eb-11e6-8e4a-f6aefe19b687",
                         "time": "2013-01-02T21:37:03.664+0200",
                         "message": "comment content",
                         "removalTime": "2018-02-10T14:33:19.000+0200",
                         "rootProcessInstanceId": "aRootProcessInstanceId"
                       }
                     }'
        ] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "The task or comment with given task and comment id does not exist, or the history of
                the engine is disabled. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>