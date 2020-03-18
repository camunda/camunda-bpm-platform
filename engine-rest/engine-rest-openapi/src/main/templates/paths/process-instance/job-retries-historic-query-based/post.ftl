{
  <@lib.endpointInfo
      id = "setRetriesByProcessHistoricQueryBased"
      tag = "Process instance"
      desc = "Create a batch to set retries of jobs asynchronously based on a historic process instance query." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "SetJobRetriesByProcessDto"
      requestDesc = "Please note that if both processInstances and historicProcessInstanceQuery are provided,
                     then the resulting execution will be performed on the union of these sets.
                     **Unallowed property**: `processInstanceQuery`" />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid, for example if neither processInstanceIds, nor historicProcessInstanceQuery is present.
                Or if the retry count is not specified."/>

  }
}