<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getStartForm"
      tag = "Process Definition"
      summary = "Get Start Form Key"
      desc = "Retrieves the key of the start form for a process definition.
              The form key corresponds to the `FormData#formKey` property in the engine." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process definition to get the start form key for."/>
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "FormDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "description": "GET `process-definition/anId/startForm`",
                       "value": {
                         "key":"aFormKey",
                         "contextPath":"http://localhost:8080/my-process-application/"
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Process definition has no start form defined. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Process definition with given id does not exist.  See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>