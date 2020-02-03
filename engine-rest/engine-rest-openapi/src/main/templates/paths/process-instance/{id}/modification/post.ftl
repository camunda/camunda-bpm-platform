{
  "operationId" : "modifyProcessInstance",
  "description": "Submits a list of modification instructions to change a process instance's execution state. A modification instruction is one of the following:\n\nStarting execution before an activity\nStarting execution after an activity on its single outgoing sequence flow\nStarting execution on a specific sequence flow\nCancelling an activity instance, transition instance, or all instances (activity or transition) for an activity\nInstructions are executed immediately and in the order they are provided in this request's body. Variables can be provided with every starting instruction.\n\nThe exact semantics of modification can be read about in the user guide(https://docs.camunda.org/manual/develop/user-guide/process-engine/process-instance-modification/).",
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
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "At least one modification instruction misses required parameters."/>

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "The modification cannot be performed, for example because it starts a failing activity."/>

   }
}