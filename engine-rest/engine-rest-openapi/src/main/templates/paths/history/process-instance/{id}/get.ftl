<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getHistoricProcessInstance"
      tag = "Historic Process Instance"
      summary = "Get"
      desc = "Retrieves a historic process instance by id, according to the `HistoricProcessInstance` interface in the engine." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the historic process instance to be retrieved." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "HistoricProcessInstanceDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/process-instance/7c80cc8f-ef95-11e6-b6e6-34f39ab71d4e`",
                       "value": {
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
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Not Found
                Historic process instance with given id does not exist.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

  }
}

</#macro>