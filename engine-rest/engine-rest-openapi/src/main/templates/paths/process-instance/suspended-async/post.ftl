{
  <@lib.endpointInfo
      id = "updateSuspensionStateAsyncOperation"
      tag = "Process instance"
      desc = "Activates or suspends process instances asynchronously with a list of process instance ids,
              a process instance query, and/or a historical process instance query." />


  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessInstanceSuspensionStateAsyncDto" />

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
                Returned if some of the request parameters are invalid,
                for example if the provided processDefinitionId or processDefinitionKey parameter is null."/>
  }
}