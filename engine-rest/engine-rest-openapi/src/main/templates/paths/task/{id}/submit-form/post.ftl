<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "submit"
      tag = "Task"
      summary = "Submit Form"
      desc = "Completes a task and updates process variables using a form submit. There are two
              difference between this method and the `complete` method:

              * If the task is in state `PENDING` - i.e., has been delegated before, it is not
                completed but resolved. Otherwise it will be completed.
              * If the task has Form Field Metadata defined, the process engine will perform backend
                validation for any form fields which have validators defined.
                See the
                [Generated Task Forms](${docsUrl}/user-guide/task-forms/_index/#generated-task-forms)
                section of the [User Guide](${docsUrl}/user-guide/) for more information." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to submit the form for."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "CompleteTaskDto"
      examples = ['"example-1": {
                     "summary": "Request Body 1",
                     "description": "POST `/task/anId/submit-form`",
                     "value": {
                       "variables": {
                         "aVariable": {
                           "value": "aStringValue"
                         },
                         "anotherVariable": {
                           "value": 42
                         },
                         "aThirdVariable": {
                           "value": true
                         },
                         "aFileVariable": {
                           "value": "TG9yZW0gaXBzdW0=",
                           "type": "File",
                           "valueInfo": {
                             "filename": "myFile.txt"
                           }
                         }
                       }
                     }
                   }',
                   '"example-2": {
                     "summary": "Request Body 2",
                     "description": "POST `/task/anId/complete`",
                     "value":     {
                       "variables": {
                         "aVariable": {
                           "value": "aStringValue"
                         },
                         "anotherVariable": {
                           "value": 42
                         },
                         "aThirdVariable": {
                           "value": true
                         }
                       },
                       "withVariablesInReturn": true
                     }
                   }'
      ] />

  "responses" : {

    <@lib.response
        code = "200"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "Request successful. The response contains the process variables."
        examples = ['"example-1": {
                       "summary": "Response Body",
                       "description": "Response of a submitted task form with variables in return",
                       "value": {
                         "aVariable": {
                           "value": "aStringValue",
                           "type": "String",
                           "valueInfo": {}
                         },
                         "anotherVariable": {
                           "value": 42,
                           "type": "Integer",
                           "valueInfo": {}
                         },
                         "aThirdVariable": {
                           "value": true,
                           "type": "Boolean",
                           "valueInfo": {}
                         }
                       }
                    }'
        ]/>

    <@lib.response
        code = "204"
        desc = "Request successful. The response contains no variables." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The variable value or type is invalid, for example if the value could not be parsed to an Integer value
                or the passed variable type is not supported.

                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for
                the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "If the task does not exist or the corresponding process instance could not be resumed successfully.

                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for
                the error response format." />

  }
}

</#macro>