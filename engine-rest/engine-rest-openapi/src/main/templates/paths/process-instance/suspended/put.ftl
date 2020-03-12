{
  <@lib.endpointInfo
      id = "updateSuspensionState"
      tag = "Process instance"
      desc = "Activates or suspends process instances by providing certain criteria:

              # Activate/Suspend Process Instance By Process Definition Id
              * `suspend`
              * `processDefinitionId`
              
              # Activate/Suspend Process Instance By Process Definition Key
              
              * `suspend`
              * `processDefinitionKey`
              * `processDefinitionTenantId`
              * `processDefinitionWithoutTenantId`
              
              # Activate/Suspend Process Instance In Group
              * `suspend`
              * `processInstanceIds`
              * `processInstanceQuery`
              * `historicProcessInstanceQuery`" />


  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessInstanceSuspensionStateDto" />

  "responses" : {

    <@lib.response
        code = "204"
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