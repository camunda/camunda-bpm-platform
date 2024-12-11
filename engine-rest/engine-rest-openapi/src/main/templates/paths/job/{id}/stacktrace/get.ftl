<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/job/get-exception-stacktrace/index.html -->
{
  <@lib.endpointInfo
      id = "getStacktrace"
      tag = "Job"
      summary = "Get Exception Stacktrace"
      desc = "Retrieves the exception stacktrace corresponding to the passed job id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the job to get the exception stacktrace for."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        mediaType= "text/plain"
        desc = "Request successful."
        contentDesc = "Returns the stacktrace of the exception for the job with the given ID."
        examples = ['"example-1": {
                       "description": "GET `/job/aJobId/stacktrace`",
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
        desc = "Job with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>