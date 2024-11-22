<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/job-log/get-job-log-stacktrace/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getStacktraceHistoricJobLog"
      tag = "Historic Job Log"
      summary = "Get Job Log Exception Stacktrace"
      desc = "Retrieves the corresponding exception stacktrace to the passed historic job log by
              id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the historic job log to get the exception stacktrace for."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        mediaType= "text/plain"
        desc = "Request successful."
        contentDesc = "Returns the stacktrace of the exception for the historic job with the given ID."
        examples = ['"example-1": {
                       "summary": "response",
                       "description": "GET `history/job-log/someId/stacktrace`

                                       The result is the corresponding stacktrace as plain text.",
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
        desc = "Historic job log with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>