<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/get/index.html -->
{
  <@lib.endpointInfo
      id = "getExecution"
      tag = "Execution"
      summary = "Get Execution"
      desc = "Retrieves an execution by id, according to the `Execution` interface in the
              engine."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the execution to be retrieved."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "ExecutionDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "GET `/execution/anExecutionId`",
                       "value": {
                         "id": "anExecutionId",
                         "processInstanceId": "aProcInstId",
                         "ended": false,
                         "tenantId": null
                       }
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Execution with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>