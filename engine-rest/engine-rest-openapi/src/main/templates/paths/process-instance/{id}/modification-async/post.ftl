{
  "operationId" : "modifyProcessInstanceAsync",
  "description": "Submits a list of modification instructions to change a process instance's execution state async. A modification instruction is one of the following:\n* Starting execution before an activity\n* Starting execution after an activity on its single outgoing sequence flow\n* Starting execution on a specific sequence flow\n* Cancelling an activity instance, transition instance, or all instances (activity or transition) for an activity\nInstructions are executed asynchronous and in the order they are provided in this request's body. Variables can be provided with every starting instruction.\nThe exact semantics of modification can be read about in the User guide (https://docs.camunda.org/manual/${docsVersion}/user-guide/process-engine/process-instance-modification/).",
  "tags": [
    "Process instance"
  ],
  "parameters" : [
    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        description = "The id of the process instance to modify."/>
  ],

  <@lib.requestBody
      dto = "ProcessInstanceModificationDto" />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Bad Request\n\nAt least one modification instruction misses required parameters."/>

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "Forbidden\n\nIf the user is not allowed to execute batches. See the Introduction for the error response format."/>

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "The modification cannot be performed, for example because it starts a failing activity."/>

   }
}