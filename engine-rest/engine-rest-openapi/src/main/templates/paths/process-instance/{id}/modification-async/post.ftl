{

  <@lib.endpointInfo
      id = "modifyProcessInstanceAsyncOperation"
      tag = "Process instance"
      desc = "Submits a list of modification instructions to change a process instance's execution state async.
              A modification instruction is one of the following:
      
              * Starting execution before an activity
              * Starting execution after an activity on its single outgoing sequence flow
              * Starting execution on a specific sequence flow
              * Cancelling an activity instance, transition instance, or all instances (activity or transition) for an activity
      
              Instructions are executed asynchronous and in the order they are provided in this request's body.
              Variables can be provided with every starting instruction.
      
              The exact semantics of modification can be read about in the
              [User guide](${docsUrl}/user-guide/process-engine/process-instance-modification/)." />

  "parameters" : [
    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process instance to modify."/>
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessInstanceModificationDto" />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Bad Request
        At least one modification instruction misses required parameters."/>

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "Forbidden
                If the user is not allowed to execute batches. See the Introduction for the error response format."/>

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "The modification cannot be performed, for example because it starts a failing activity."/>

   }
}