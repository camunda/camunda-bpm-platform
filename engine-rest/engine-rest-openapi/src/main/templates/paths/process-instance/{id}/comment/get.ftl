<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getProcessInstanceComments"
      tag = "Process Instance Comment"
      summary = "Get Process Instance Comments"
      desc = "Gets the comments for a process instance by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process instance to retrieve the comments for." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CommentDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /process-instance/aProcessInstanceId/comment",
                       "value": [
                         {
                           "id": "commentId",
                           "userId": "userId",
                           "taskId": "aTaskId",
                           "processInstanceId": "aProcessInstanceId",
                           "time": "2013-01-02T21:37:03.764+0200",
                           "message": "message",
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId"
                         },
                         {
                           "id": "anotherCommentId",
                           "userId": "anotherUserId",
                           "taskId": "aTaskId",
                           "processInstanceId": "aProcessInstanceId",
                           "time": "2013-02-23T20:37:43.975+0200",
                           "message": "anotherMessage",
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId"
                         },
                         {
                           "id": "yetAnotherCommentId",
                           "userId": "yetAnotherUserId",
                           "taskId": "aTaskId",
                           "processInstanceId": "aProcessInstanceId",
                           "time": "2013-04-21T10:15:23.764+0200",
                           "message": "yetAnotherMessage",
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "rootProcessInstanceId": "aRootProcessInstanceId"
                         }
                       ]
                     }'
        ] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "No process instance exists for the given process instance id. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>
