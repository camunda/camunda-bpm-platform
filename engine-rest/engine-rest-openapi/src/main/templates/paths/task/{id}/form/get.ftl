<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getForm"
      tag = "Task"
      summary = "Get Form Key"
      desc = "Retrieves the form key for a task. The form key corresponds to the `FormData#formKey`
              property in the engine. This key can be used to do task-specific form rendering in
              client applications. Additionally, the context path of the containing process
              application is returned." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to retrieve the form for." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "FormDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "description": "GET `/task/anId/form`",
                       "value": {
                         "key":"aFormKey",
                         "contextPath":"http://localhost:8080/my-process-application/"
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Task with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>