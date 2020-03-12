{
  <@lib.endpointInfo
      id = "getEngineNames"
      tag = "Engine"
      desc = "Retrieves the names of all process engines available on your platform.
              Note: You cannot prepend /engine/{name} to this method." />

  "responses" : {
    <@lib.response
        code = "200"
        dto = "ProcessEngineDto"
        array = true
        last=true
        desc = "Request successful." />
  }
}