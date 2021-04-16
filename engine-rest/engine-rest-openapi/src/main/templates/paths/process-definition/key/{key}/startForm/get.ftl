<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getStartFormByKey"
      tag = "Process Definition"
      summary = "Get Start Form Key"
      desc = "Retrieves the key of the start form for the latest version of the process definition
              which belongs to no tenant.
              The form key corresponds to the `FormData#formKey` property in the engine." />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The key of the process definition (the latest version thereof) for which the form key is to be retrieved."/>
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "FormDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "description": "GET `process-definition/key/aKey/startForm`",
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
        desc = "Process definition with given key does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>