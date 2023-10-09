<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/variable-instance/get-query-count/index.html -->
{
  <@lib.endpointInfo
      id = "getVariableInstancesCount"
      tag = "Variable Instance"
      summary = "Get Variable Instance Count"
      desc = "Query for the number of variable instances that fulfill given parameters. Takes the
              same parameters as the [Get Variable
              Instances](${docsUrl}/reference/rest/variable-instance/get-query/)
              method."
  />

  "parameters" : [

    <#assign requestMethod="GET"/>
    <#assign last = false >
    <#include "/lib/commons/variable-instance-query-params.ftl" >
    <#assign last = true >
    <@lib.parameters
        object = params
        last = last
    />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "GET `/variable-instance/count?processInstanceIdIn=aProcessInstanceId,anotherProcessInstanceId&variableValues=amount_gteq_5,amount_lteq_200`",
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