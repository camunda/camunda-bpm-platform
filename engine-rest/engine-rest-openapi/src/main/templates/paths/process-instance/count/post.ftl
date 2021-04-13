<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryProcessInstancesCount"
      tag = "Process Instance"
      summary = "Get List Count (POST)"
      desc = "Queries for the number of process instances that fulfill the given parameters.
              This method takes the same message body as the Get Instances (POST) method and
              therefore it is slightly more powerful than the Get Instance Count method." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessInstanceQueryDto"
      examples = [
                  '"example-1": {
                     "summary": "POST `/process-instance/count` Request Body 1",
                     "value": {
                       "variables":
                       [{
                           "name": "myVariable",
                           "operator": "eq",
                           "value": "camunda"
                         }, {
                           "name": "mySecondVariable",
                           "operator": "neq",
                           "value": 124
                         }
                       ],
                       "processDefinitionId": "aProcessDefinitionId"
                     }
                   }'
                ] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response 1",
                       "value": {
                         "count": 1
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid, for example if an invalid operator for variable comparison is used."/>

  }
}
</#macro>