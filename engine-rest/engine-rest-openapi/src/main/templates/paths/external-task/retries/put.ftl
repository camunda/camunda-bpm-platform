<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "setExternalTaskRetries"
      tag = "External Task"
      summary = "Set Retries Sync"
      desc = "Sets the number of retries left to execute external tasks by id synchronously. If retries are set to 0, 
              an incident is created." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "SetRetriesForExternalTasksDto"
      examples = ['"example-1": {
                       "summary": "PUT /external-task/retries",
                       "value": {
                         "retries": 123,
                         "externalTaskIds": [
                           "anExternalTask",
                           "anotherExternalTask"
                         ]
                       }
                     }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "In case the number of retries is negative or null, an exception of type `InvalidRequestException` is
                returned. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
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