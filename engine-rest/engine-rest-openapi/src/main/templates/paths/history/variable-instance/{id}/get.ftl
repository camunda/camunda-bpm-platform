<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/variable-instance/get-variable-instance/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricVariableInstance"
      tag = "Historic Variable Instance"
      summary = "Get Variable Instance"
      desc = "Retrieves a historic variable by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the variable instance."
      />

      <@lib.parameter
          name = "deserializeValue"
          location = "query"
          type = "boolean"
          desc = "
                  Determines whether serializable variable values (typically
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
        dto = "HistoricVariableInstanceDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "GET `/history/variable-instance/someId`",
                       "value": {
                         "id": "someId",
                         "name": "amount",
                         "type": "Integer",
                         "value": 5,
                         "valueInfo": {},
                         "processDefinitionKey": "aProcessDefinitionKey",
                         "processDefinitionId": "aProcessDefinitionId",
                         "processInstanceId": "aProcessInstanceId",
                         "executionId": "aExecutionId",
                         "activityInstanceId": "Task_1:b68b71ca-e310-11e2-beb0-f0def1557726",
                         "caseDefinitionKey": null,
                         "caseDefinitionId": null,
                         "caseInstanceId": null,
                         "caseExecutionId": null,
                         "taskId": null,
                         "tenantId": null,
                         "errorMessage": null,
                         "state": "CREATED",
                         "createTime": "2017-02-10T14:33:19.000+0200",
                         "removalTime": "2018-02-10T14:33:19.000+0200",
                         "rootProcessInstanceId": "aRootProcessInstanceId"
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
