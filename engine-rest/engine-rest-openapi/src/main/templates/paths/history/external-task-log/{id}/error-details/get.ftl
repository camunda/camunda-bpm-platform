<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/external-task-log/get-external-task-log-error-details/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getErrorDetailsHistoricExternalTaskLog"
      tag = "Historic External Task Log"
      summary = "Get External Task Log Error Details"
      desc = "Retrieves the corresponding error details of the passed historic external task log
              by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the historic external task log to get the error details for."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        mediaType= "text/plain"
        desc = "Request successful."
        contentDesc = "Returns the error details of the historic external task log with the given ID."
        examples = ['"example-1": {
                       "summary": "GET `history/external-task-log/someId/error-details`",
                       "description": "GET `history/external-task-log/someId/error-details`",
                       "value": "java.lang.RuntimeException: A exception message!
                                  at org.camunda.bpm.pa.service.FailingDelegate.execute(FailingDelegate.java:10)
                                  at org.camunda.bpm.engine.impl.delegate.JavaDelegateInvocation.invoke(JavaDelegateInvocation.java:34)
                                  at org.camunda.bpm.engine.impl.delegate.DelegateInvocation.proceed(DelegateInvocation.java:37)
                                  ..."
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Historic external task log with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>