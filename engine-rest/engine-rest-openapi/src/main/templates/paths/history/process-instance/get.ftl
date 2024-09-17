<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricProcessInstances"
      tag = "Historic Process Instance"
      summary = "Get List"
      desc = "Queries for historic process instances that fulfill the given parameters.
              The size of the result set can be retrieved by using the
              [Get Process Instance Count](${docsUrl}/reference/rest/history/process-instance/get-process-instance-query-count/) method." />

  "parameters" : [

    <#assign requestMethod="GET"/>
    <#include "/lib/commons/history-process-instance.ftl" >

    <#assign last = false >
    <#include "/lib/commons/sort-params.ftl" >

    <#include "/lib/commons/pagination-params.ftl" >

    <@lib.parameters
        object = params
        skip = ["orQueries"] <#-- OR Queries not available in GET -->
        last = true />

  ],
  "responses" : {
    <@lib.response
        code = "200"
        dto = "HistoricProcessInstanceDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response for GET `/history/process-instance?finishedAfter=2013-01-01T00:00:00.000%2b0200&finishedBefore=2013-04-01T23:59:59.000%2b0200&executedActivityAfter=2013-03-23T13:42:44.000%2b0200`",
                       "value": [
                                  {
                                    "id":"7c80cc8f-ef95-11e6-b6e6-34f39ab71d4e",
                                    "businessKey":null,
                                    "processDefinitionId":"invoice:1:7bf79f13-ef95-11e6-b6e6-34f39ab71d4e",
                                    "processDefinitionKey":"invoice",
                                    "processDefinitionName":"Invoice Receipt",
                                    "processDefinitionVersion":1,
                                    "startTime":"2017-02-10T14:33:19.000+0200",
                                    "endTime":null,
                                    "removalTime": null,
                                    "durationInMillis":null,
                                    "startUserId":null,
                                    "startActivityId":"StartEvent_1",
                                    "deleteReason":null,
                                    "rootProcessInstanceId": "f8259e5d-ab9d-11e8-8449-e4a7a094a9d6",
                                    "superProcessInstanceId":null,
                                    "superCaseInstanceId":null,
                                    "caseInstanceId":null,
                                    "tenantId":null,
                                    "state":"ACTIVE",
                                    "restartedProcessInstanceId":"2bef365d-3406-11ef-bd73-0a0027000003"
                                  }
                                ]
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid, for example if a sortOrder parameter is supplied, but no sortBy.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>
  }
}
</#macro>