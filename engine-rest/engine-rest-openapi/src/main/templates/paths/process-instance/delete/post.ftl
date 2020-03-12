{
  <@lib.endpointInfo
      id = "deleteProcessInstancesAsync"
      tag = "Process instance"
      desc = "Deletes multiple process instances asynchronously (batch)." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "DeleteProcessInstancesDto"
      requestDesc = "**Unallowed property**: `historicProcessInstanceQuery`" />

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
                Returned if some of the query parameters are invalid, i.e., neither processInstanceIds, nor processInstanceQuery is present"/>

  }
}