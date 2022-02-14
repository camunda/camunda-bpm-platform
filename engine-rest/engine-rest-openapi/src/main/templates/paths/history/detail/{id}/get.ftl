<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/detail/get-detail/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "historicDetail"
      tag = "Historic Detail"
      summary = "Get Historic Detail"
      desc = "Retrieves a historic detail by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the detail."
      />

      <@lib.parameter
          name = "deserializeValue"
          location = "query"
          type = "boolean"
          desc = "Determines whether serializable variable values (typically
                  variables that store custom Java objects) should be deserialized
                  on server side (default `true`).

                  If set to `true`, a serializable variable will be deserialized on
                  server side and transformed to JSON using
                  [Jackson's](https://github.com/FasterXML/jackson) POJO/bean
                  property introspection feature. Note that this requires the Java
                  classes of the variable value to be on the REST API's classpath.

                  If set to `false`, a serializable variable will be returned in its
                  serialized format. For example, a variable that is serialized as
                  XML will be returned as a JSON string containing XML.

                  **Note:** While `true` is the default value for reasons of
                  backward compatibility, we recommend setting this parameter to
                  `false` when developing web applications that are independent of
                  the Java process applications deployed to the engine."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricDetailDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/detail/3cd79390-001a-11e7-8c6b-34f39ab71d4e`",
                       "description": "GET `/history/detail/3cd79390-001a-11e7-8c6b-34f39ab71d4e`",
                       "value": {
                         "type": "variableUpdate",
                         "id": "3cd79390-001a-11e7-8c6b-34f39ab71d4e",
                         "processDefinitionKey": "invoice",
                         "processDefinitionId": "invoice:1:3c59899b-001a-11e7-8c6b-34f39ab71d4e",
                         "processInstanceId": "3cd597b7-001a-11e7-8c6b-34f39ab71d4e",
                         "activityInstanceId": "StartEvent_1:3cd7456e-001a-11e7-8c6b-34f39ab71d4e",
                         "executionId": "3cd597b7-001a-11e7-8c6b-34f39ab71d4e",
                         "caseDefinitionKey": null,
                         "caseDefinitionId": null,
                         "caseInstanceId": null,
                         "caseExecutionId": null,
                         "taskId": null,
                         "tenantId": null,
                         "userOperationId": "3cd76c7f-001a-11e7-8c6b-34f39ab71d4e",
                         "time": "2017-03-03T15:03:54.000+0200",
                         "variableName": "amount",
                         "variableInstanceId": "3cd65b08-001a-11e7-8c6b-34f39ab71d4e",
                         "variableType": "Double",
                         "value": 30.0,
                         "valueInfo": {},
                         "revision": 0,
                         "errorMessage": null,
                         "removalTime": "2018-02-10T14:33:19.000+0200",
                         "rootProcessInstanceId": "aRootProcessInstanceId",
                         "initial": true
                       }
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Variable with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>