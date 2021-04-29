<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/user-operation-log/get-user-operation-log-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryUserOperationCount"
      tag = "Historic User Operation Log"
      summary = "Get User Operation Log Count"
      desc = "Queries for the number of user operation log entries that fulfill the given parameters.
              Takes the same parameters as the
              [Get User Operation Log (Historic)](${docsUrl}/reference/rest/history/user-operation-log/get-user-operation-log-query/)
              method."
  />

  "parameters" : [

    <#assign last = true >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/user-operation-log-query-params.ftl" >
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
                       "summary": "GET `/history/user-operation?operationType=Claim&userId=demo`",
                       "description": "GET `/history/user-operation?operationType=Claim&userId=demo`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>