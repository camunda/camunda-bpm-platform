<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "handleFailure"
      tag = "External Task"
      summary = "Handle Failure"
      desc = "Reports a failure to execute an external task by id. A number of retries and a timeout until the task can
              be retried can be specified. If retries are set to 0, an incident for this task is created." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the external task to report a failure for."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ExternalTaskFailureDto"
      examples = ['"example-1": {
                     "summary": "POST /external-task/anId/failure",
                     "value": {
                       "workerId": "aWorker",
                       "errorMessage": "Does not compute",
                       "retries": 3,
                       "retryTimeout": 60000
                       }
                   }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if the task's most recent lock was not acquired by the provided worker. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Returned if the task does not exist. This could indicate a wrong task id as well as a cancelled task,
                e.g., due to a caught BPMN boundary event. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the corresponding process instance could not be resumed successfully. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />
  }
}

</#macro>