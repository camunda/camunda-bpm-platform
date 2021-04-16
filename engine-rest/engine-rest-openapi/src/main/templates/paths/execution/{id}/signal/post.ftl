<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/post-signal/index.html -->
{
  <@lib.endpointInfo
      id = "signalExecution"
      tag = "Execution"
      summary = "Trigger Execution"
      desc = "Signals an execution by id. Can for example be used to explicitly skip user tasks
              or signal asynchronous continuations."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the execution to signal."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ExecutionTriggerDto"
      examples = ['"example-1": {
                     "summary": "POST `/execution/{id}/signal`",
                     "value": {
                       "variables": {
                         "myVariable": {
                           "value": "camunda",
                           "type": "String"
                         },
                         "mySecondVariable": {
                           "value": 124,
                           "type": "Integer"
                         }
                       }
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The variable value or type is invalid, for example if the value could not be parsed
                to an Integer value or the passed variable type is not supported.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."
        last = true
    />

  }

}
</#macro>