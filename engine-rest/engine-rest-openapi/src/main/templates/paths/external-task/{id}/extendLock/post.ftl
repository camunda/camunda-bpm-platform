<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "extendLock"
      tag = "External Task"
      summary = "Extend Lock"
      desc = "Extends the timeout of the lock by a given amount of time." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the external task."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ExtendLockOnExternalTaskDto"
      examples = ['"example-1": {
                     "summary": "POST /external-task/anId/extendLock",
                     "value": {
                       "workerId": "anId",
                       "newDuration": 100000
                     }
                   }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "In case the new lock duration is negative or the external task is not locked by the given worker or not 
                locked at all, an exception of type `InvalidRequestException` is returned. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the task does not exist. This could indicate a wrong task id as well as a cancelled task,
                e.g., due to a caught BPMN boundary event. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>