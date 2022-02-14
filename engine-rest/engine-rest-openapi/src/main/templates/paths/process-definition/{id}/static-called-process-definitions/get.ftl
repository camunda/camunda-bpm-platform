<#-- Generated From File: camunda-docs-manual/public/reference/rest/process-definition/get-static-called-process-definitions/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getStaticCalledProcessDefinitions"
      tag = "Process Definition"
      summary = "Get Static Called Process Definitions"
      desc = "For the given process, returns a list of called process definitions corresponding
              to
              the `CalledProcessDefinition` interface in the engine. The list
              contains all process definitions
              that are referenced statically by call activities in the given
              process. This endpoint does not
              resolve process definitions that are referenced with expressions. Each
              called process definition
              contains a list of call activity ids, which specifies the call
              activities that are calling that
              process. This endpoint does not resolve references to case
              definitions."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the process definition."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "CalledProcessDefinitionDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/process-definition/aProcessDefinitionId/static-called-process-definitions`",
                       "description": "GET `/process-definition/aProcessDefinitionId/static-called-process-definitions`",
                       "value": [
                         {
                           "id": "ACalledProcess:1:1bbd4e83-f8f1-11eb-9344",
                           "key": "ACalledProcess",
                           "category": "http://www.omg.org/spec/BPMN/20100524/MODEL",
                           "description": null,
                           "name": "ACalledProcess",
                           "version": 1,
                           "resource": "called-process.bpmn",
                           "deploymentId": "1baa3baf-f8f1-11eb-9344-0e0bbdd53e42",
                           "diagram": null,
                           "suspended": false,
                           "tenantId": null,
                           "versionTag": null,
                           "historyTimeToLive": null,
                           "calledFromActivityIds": [
                             "aCallActivityId"
                           ],
                           "callingProcessDefinitionId": "aProcessDefinitionId",
                           "startableInTasklist": true
                         },
                         {
                           "id": "AnotherCalledProcess:2:1bc2f3d5-f8f1-11eb-9344",
                           "key": "AnotherCalledProcess",
                           "category": "http://www.omg.org/spec/BPMN/20100524/MODEL",
                           "description": null,
                           "name": "AnotherCalledProcess",
                           "version": 2,
                           "resource": "another-called-process.bpmn",
                           "deploymentId": "1baa3baf-f8f1-11eb-9344-0e0bbdd53e42",
                           "diagram": null,
                           "suspended": false,
                           "tenantId": null,
                           "versionTag": null,
                           "historyTimeToLive": null,
                           "calledFromActivityIds": [
                             "aSecondCallActivityId",
                             "aThirdCallActivityId"
                           ],
                           "callingProcessDefinitionId": "aProcessDefinitionId",
                           "startableInTasklist": true
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Process definition with given key does not exist.
                        See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>