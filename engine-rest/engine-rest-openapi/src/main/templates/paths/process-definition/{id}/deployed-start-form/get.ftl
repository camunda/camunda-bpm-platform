<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getDeployedStartForm"
      tag = "Process Definition"
      summary = "Get Deployed Start Form"
      desc = "Retrieves the deployed form that can be referenced from a start event.
      For further information please refer to [User Guide](${docsUrl}/user-guide/task-forms/#embedded-task-forms)." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process definition to get the deployed start form for."/>
  ],

  "responses" : {

    <@lib.multiTypeResponse
        code = "200"
        desc = "Request successful."
        types = [
          {
            "mediaType": "application/xhtml+xml",
            "examples": ['"example-1": {
                       "summary": "Status 200 Response",
                       "description": "Resonse for GET `/process-definition/processDefinitionId/deployed-start-form`",
                       "value": "<form role=\\"form\\" name=\\"invoiceForm\\"
                                      class=\\"form-horizontal\\">

                                  <div class=\\"form-group\\">
                                    <label class=\\"control-label col-md-4\\"
                                           for=\\"creditor\\">Creditor</label>
                                    <div class=\\"col-md-8\\">
                                      <input cam-variable-name=\\"creditor\\"
                                             cam-variable-type=\\"String\\"
                                             id=\\"creditor\\"
                                             class=\\"form-control\\"
                                             type=\\"text\\"
                                             required />
                                      <div class=\\"help\\">
                                        (e.g. &quot;Great Pizza for Everyone Inc.&quot;)
                                      </div>
                                    </div>
                                  </div>

                                </form>"
                     }']
          },
          {
            "mediaType": "application/json",
            "flatType": "string"
          }
        ] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The form key has wrong format.  See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "403"
        dto = "AuthorizationExceptionDto"
        desc = "The deployed start form cannot be retrieved due to missing permissions on process definition resource.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "No deployed start form for a given process definition exists. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>