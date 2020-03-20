{
  <@lib.endpointInfo
      id = "deleteAsyncHistoricQueryBased"
      tag = "Process instance"
      desc = "Deletes a set of process instances asynchronously (batch) based on a historic process instance query." />


  <@lib.requestBody
      mediaType = "application/json"
      dto = "DeleteProcessInstancesDto"
      requestDesc = "**Unallowed property**: `processInstanceQuery`" />

  "responses": {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid, i.e., neither processInstanceIds, nor historicProcessInstanceQuery is present"/>

  }
}