<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/variable-instance/post-query-count/index.html -->
{
  <@lib.endpointInfo
      id = "queryVariableInstancesCount"
      tag = "Variable Instance"
      summary = "Get Variable Instance Count (POST)"
      desc = "Query for the number of variable instances that fulfill given parameters. This
              method takes the same message body as the
              [Get Variable Instances POST](${docsUrl}/reference/rest/variable-
              instance/post-query/) method and therefore it is slightly more
              powerful than the [Get Variable Instance
              Count](${docsUrl}/reference/rest/variable-instance/get-query-count/)
              method."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "VariableInstanceQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/variable-instance/count`",
                     "value": {
                       "variableValues": [
                         {
                           "name": "amount",
                           "operator": "gteq",
                           "value": "5"
                         },
                         {
                           "name": "amount",
                           "operator": "lteq",
                           "value": 200
                         }
                       ],
                       "processInstanceIdIn": [
                         "aProcessInstanceId",
                         "anotherProcessInstanceId"
                       ]
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "POST `/variable-instance/count`",
                       "value": {
                         "count": 3
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if an invalid operator for variable
                comparison is used. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}

</#macro>