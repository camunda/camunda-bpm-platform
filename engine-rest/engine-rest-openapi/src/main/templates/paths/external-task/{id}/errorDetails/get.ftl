<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getExternalTaskErrorDetails"
      tag = "External Task"
      summary = "Get Error Details"
      desc = "Retrieves the error details in the context of a running external task by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the external task for which the error details should be retrieved."/>

  ],

  "responses" : {

    <@lib.response
    code = "200"
    mediaType= "text/plain"
    desc = "Request successful."
    contentDesc = "The error details for the external task."
    examples = ['"example-1": {
                       "summary": "GET `external-task/someId/errorDetails`",
                       "description": "GET `external-task/someId/errorDetails`",
                       "value": "org.apache.ibatis.jdbc.RuntimeSqlException: org.apache.ibatis.jdbc.RuntimeSqlException: test cause
                                      at org.camunda.bpm.engine.test.api.externaltask.ExternalTaskServiceTest.testHandleFailureWithErrorDetails(ExternalTaskServiceTest.java:1424)
                                      at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
                                      at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
                                      at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
                                      ..."
                     }']/>


  <@lib.response
      code = "204"
      desc = "Request successful. In case the external task has no error details." />


    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "An external task with the given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>