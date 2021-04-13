<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getRenderedStartFormByKeyAndTenantId"
      tag = "Process Definition"
      summary = "Get Rendered Start Form"
      desc = "Retrieves  the rendered form for the latest version of the process definition for a tenant.
              This method can be used to get the HTML rendering of a
              [Generated Task Form](${docsUrl}/user-guide/task-forms/#generated-task-forms)." />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        desc = "The key of the process definition (the latest version thereof) to be retrieved."/>

    <@lib.parameter
        name = "tenant-id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the tenant the process definition belongs to."/>

  ],

  "responses" : {

    <@lib.response
        code = "200"
        mediaType = "application/xhtml+xml"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "description": "A `/process-definition/key/anKey/tenand-id/aTenantId/rendered-form` HTML
                                       GET response body providing the rendered (generated) form content.",
                       "value": "<form class=\\"form-horizontal\\">
                                  <div class=\\"control-group\\">
                                    <label class=\\"control-label\\">Customer ID</label>
                                    <div class=\\"controls\\">
                                      <input form-field type=\\"string\\" name=\\"customerId\\"></input>
                                    </div>
                                  </div>
                                  <div class=\\"control-group\\">
                                    <label class=\\"control-label\\">Amount</label>
                                    <div class=\\"controls\\">
                                      <input form-field type=\\"number\\" name=\\"amount\\"></input>
                                    </div>
                                  </div>
                                </form>"
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Process definition has no form field metadata defined. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Process definition with given key does not exist.  See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>