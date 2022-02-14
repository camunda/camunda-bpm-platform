<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getRenderedForm"
      tag = "Task"
      summary = "Get Rendered Form"
      desc = "Retrieves the rendered form for a task. This method can be used to get the HTML
              rendering of a
              [Generated Task Form](${docsUrl}/user-guide/task-forms/#generated-task-forms)." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to get the rendered form for."/>

  ],

  "responses" : {

    <@lib.response
        code = "200"
        mediaType = "application/xhtml+xml"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "description": "A `/task/anId/rendered-form` HTML GET response body providing the rendered (generated) form content.",
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
        last = true
        desc = "The task with the given id does not exist or has no form field metadata defined for
                this task. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>