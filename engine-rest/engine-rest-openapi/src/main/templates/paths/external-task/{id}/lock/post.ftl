<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "lock"
      tag = "External Task"
      desc = "Lock an external task by a given id for a specified worker and amount of time." />

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
      dto = "LockExternalTaskDto"
      examples = ['"example-1": {
                     "summary": "POST /external-task/anId/lock",
                     "value": {
                       "workerId": "anId",
                       "lockDuration": 100000
                     }
                   }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "In case the lock duration is negative or the external task is already locked by
                a different worker, an exception of type `InvalidRequestException` is returned. See the
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